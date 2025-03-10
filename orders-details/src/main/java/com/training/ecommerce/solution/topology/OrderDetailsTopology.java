package com.training.ecommerce.solution.topology;

import com.training.ecommerce.model.*;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.*;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG;

public class OrderDetailsTopology {

    private static final String CUSTOMERS_TOPIC = "demo.ecommerce.customers";
    private static final String CUSTOMER_STORE = "customer-store";
    private static final String PRODUCTS_TOPIC = "demo.ecommerce.products";
    private static final String PRODUCT_STORE = "product-store";
    private static final String ORDERS_TOPIC = "demo.ecommerce.orders";
    private static final String ENRICHED_ORDERS_TOPIC = "demo.ecommerce.orders-enriched";

    private Properties properties;

    public OrderDetailsTopology(Properties properties) {
        this.properties = properties;
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsTopology.class);

    public Topology buildTopology() {
        logger.info("Building streams topology");

        final StreamsBuilder builder = new StreamsBuilder();

        var products = builder.globalTable(PRODUCTS_TOPIC,
                Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(getProductSerDe()));
        var customers = builder.globalTable(CUSTOMERS_TOPIC,
                Materialized.<String, Customer, KeyValueStore<Bytes, byte[]>>as(CUSTOMER_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(getCustomerSerDe()));

        var ordersStream = builder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), getOrderSerDe()));

        //final double EXPENSIVE_STUFF = 1000.00;

        // Join the orders stream to the customer global table. As this is global table
        // we can use a non-key based join without needing to repartition the input stream
        KStream<String, OrderDetailsTopology.CustomerOrder> customerOrderStream = ordersStream
                //.filter((k, order) -> order.getTotalAmount() < EXPENSIVE_STUFF)
                .join(customers, (orderId, order) -> String.valueOf(order.getCustomerId()),
                        (order, customer) -> new OrderDetailsTopology.CustomerOrder(customer, order));

        // Join the enriched customer order stream with the product global table. As this is global table
        // we can use a non-key based join without needing to repartition the input stream
        KStream<String, EnrichedOrder> enrichedOrdersStream = customerOrderStream
                .peek((key, value) -> logger.info("Processing record with key " + key))
                .join(products, (orderId, customerOrder) -> String.valueOf(customerOrder.productId()),
                        (customerOrder, product) -> {
                            EnrichedOrder enrichedOrder = new EnrichedOrder();
                            enrichedOrder.setId(customerOrder.order.getId());
                            enrichedOrder.setTotalAmount(customerOrder.order.getTotalAmount());
                            enrichedOrder.setCustomerId(customerOrder.customer.getId());
                            enrichedOrder.setCustomerName(customerOrder.customer.getCustomerName());
                            enrichedOrder.setDeliveryAddress(customerOrder.customer.getCustomerAddress());
                            enrichedOrder.setPurchasedItem(new ItemDetail(
                                    product.getProductName(), customerOrder.order.getProductQuantity(), product.getProductCost()));
                            return enrichedOrder;
                        });

        enrichedOrdersStream.to(ENRICHED_ORDERS_TOPIC, Produced.with(Serdes.String(), getEnrichedOrderSerDe()));

        logger.info("Streams topology created");

        return builder.build();
    }

    private Map<String, String> getSerDesConfig() {
        // create and configure the SpecificAvroSerdes required in this example
        final Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(SCHEMA_REGISTRY_URL_CONFIG));
        serdeConfig.put(BASIC_AUTH_CREDENTIALS_SOURCE, properties.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE));
        serdeConfig.put(USER_INFO_CONFIG, properties.getProperty(USER_INFO_CONFIG));
        return serdeConfig;
    }

    private SpecificAvroSerde<Order> getOrderSerDe() {
        final SpecificAvroSerde<Order> orderSerde = new SpecificAvroSerde<>();
        orderSerde.configure(getSerDesConfig(), false);
        return orderSerde;
    }

    private SpecificAvroSerde<Customer> getCustomerSerDe() {
        final SpecificAvroSerde<Customer> customerSerde = new SpecificAvroSerde<>();
        customerSerde.configure(getSerDesConfig(), false);
        return customerSerde;
    }

    private SpecificAvroSerde<Product> getProductSerDe() {
        final SpecificAvroSerde<Product> productSerde = new SpecificAvroSerde<>();
        productSerde.configure(getSerDesConfig(), false);
        return productSerde;
    }

    private SpecificAvroSerde<EnrichedOrder> getEnrichedOrderSerDe() {
        final SpecificAvroSerde<EnrichedOrder> enrichedOrdersSerde = new SpecificAvroSerde<>();
        enrichedOrdersSerde.configure(getSerDesConfig(), false);
        return enrichedOrdersSerde;
    }

    // Helper class for intermediate join between
        // orders & customers
        private record CustomerOrder(Customer customer, Order order) {

        CharSequence productId() {
                return order.getProductId();
            }
        }
}