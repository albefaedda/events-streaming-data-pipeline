package com.training.ecommerce.solution.topology;

import com.training.ecommerce.model.EnrichedOrder;
import com.training.ecommerce.model.OrdersTotalByCustomer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.*;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG;

public class OrderTotalsTopology {

    private static final String ENRICHED_ORDERS_TOPIC = "demo.ecommerce.orders-enriched";

    private static final String ORDERS_TOTAL_TOPIC = "demo.ecommerce.orders-total";

    private static final String ORDERS_TOTAL_REPARTITION_TOPIC = "demo.ecommerce.orders-total-repartition";

    private static final String ORDERS_TOTAL_CHANGELOG_TOPIC = "demo.ecommerce.orders-total-changelog";

    private static final Logger logger = LoggerFactory.getLogger(OrderTotalsTopology.class);
    final private Properties properties;

    public OrderTotalsTopology(Properties properties) {
        this.properties = properties;
    }


    public Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        // read the source stream
        final KStream<String, EnrichedOrder> ordersStream = builder.stream(ENRICHED_ORDERS_TOPIC, Consumed.with(Serdes.String(), getEnrichedOrderSerDe()));

        KTable<String, OrdersTotalByCustomer> aggregate = ordersStream
                .groupBy((id, enrichedOrder) -> String.valueOf(enrichedOrder.getCustomerId()),
                        Grouped.with(ORDERS_TOTAL_REPARTITION_TOPIC, Serdes.String(), getEnrichedOrderSerDe()))
                .aggregate(OrdersTotalByCustomer::new,  // initialize the aggregator
                        (customerId, enrichedOrder, ordersTotal) -> addToTotals(enrichedOrder, ordersTotal),    // sum the totals with the new order total
                        Materialized    /*  .as(ORDERS_TOTAL_CHANGELOG_TOPIC) */
                                .with(Serdes.String(), getOrdersTotalByCustomerSerDe()));   // materialize is needed to provide the State Store with the SerDes.


        // the aggregate operation returned a KTable, we transform it back to a stream before sending it out to a topic
        aggregate.toStream()
                // peek allows to print out the current record being processed
                .peek((key, value) -> System.out.println("Outgoing record - key " +key))
                .to(ORDERS_TOTAL_TOPIC, Produced.with(Serdes.String(), getOrdersTotalByCustomerSerDe()));

        return builder.build();
    }

    private OrdersTotalByCustomer addToTotals(final EnrichedOrder order, final OrdersTotalByCustomer totals) {
        return new OrdersTotalByCustomer(order.getCustomerId(), order.getCustomerName(),
                (totals == null ? 1 : totals.getNumOrders() + 1),
                (totals == null ? 0D : totals.getTotalAmount()) + order.getTotalAmount());
    }

    private Map<String, String> getSerDesConfig() {
        // create and configure the SpecificAvroSerdes required in this example
        final Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, properties.getProperty(SCHEMA_REGISTRY_URL_CONFIG));
        serdeConfig.put(BASIC_AUTH_CREDENTIALS_SOURCE, properties.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE));
        serdeConfig.put(USER_INFO_CONFIG, properties.getProperty(USER_INFO_CONFIG));
        return serdeConfig;
    }

    private SpecificAvroSerde<EnrichedOrder> getEnrichedOrderSerDe() {
        final SpecificAvroSerde<EnrichedOrder> orderSerde = new SpecificAvroSerde<>();
        orderSerde.configure(getSerDesConfig(), false);
        return orderSerde;
    }

    private SpecificAvroSerde<OrdersTotalByCustomer> getOrdersTotalByCustomerSerDe() {
        final SpecificAvroSerde<OrdersTotalByCustomer> customerSerde = new SpecificAvroSerde<>();
        customerSerde.configure(getSerDesConfig(), false);
        return customerSerde;
    }
}
