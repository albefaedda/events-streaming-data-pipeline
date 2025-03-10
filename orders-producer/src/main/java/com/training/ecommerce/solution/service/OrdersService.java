package com.training.ecommerce.solution.service;

import com.google.gson.Gson;
import com.training.ecommerce.model.Customer;
import com.training.ecommerce.model.DlqMessage;
import com.training.ecommerce.model.Order;
import com.training.ecommerce.model.Product;
import com.training.ecommerce.solution.producer.DlqProducer;
import com.training.ecommerce.solution.producer.OrdersProducer;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OrdersService {

    private final DecimalFormat df = new DecimalFormat("#.##");

    private final Gson gson;

    private final OrdersProducer ordersProducer;

    private final DlqProducer dlqProducer;

    public OrdersService(final OrdersProducer ordersProducer, final DlqProducer dlqProducer) {
        this.ordersProducer = ordersProducer;
        this.dlqProducer = dlqProducer;
        this.df.setRoundingMode(RoundingMode.FLOOR);
        this.gson = new Gson();
    }

    public void execute() {
        try {
            List<Product> products = DbService.collectProducts();
            List<Customer> customers = DbService.collectCustomers();

            generateOrders(products, customers);
        } catch (Exception e) {
            System.err.printf("Error accessing DB to retrieve data %s", e);
        } finally {
            ordersProducer.close();
            dlqProducer.close();
        }
    }

    private void generateOrders(List<Product> products, List<Customer> customers) {
        for (int id = 1; id <= 5000; id++) {
            Order order = null;
            try {
                String key = RandomStringUtils.randomAlphanumeric(12);
                order = buildRandomOrder(key, products, customers);
                //System.out.println("Order id: " + order.getId() + " Product id: " + order.getProductId() + " Customer id: " + order.getCustomerId() + " Order Qty: " + order.getProductQuantity() + " Order Total: " + order.getTotalAmount());

                if (order.getProductQuantity() <= 0) {
                    DlqMessage dlqMessage = buildDlqMessage(order, "Product quantity of an order cannot be equal or lower than 0");
                    dlqProducer.send(null, dlqMessage);
                } else {
                    ordersProducer.send(key, order);
                }
            } catch (Exception e) {
                System.err.printf("Error producing message %s", e);
                DlqMessage dlqMessage = buildDlqMessage(order, e.getMessage());
                dlqProducer.send(null, dlqMessage);
            }
        }
    }

    private Order buildRandomOrder(String id, List<Product> products, List<Customer> customers) {

        int numCustomers = customers.size();
        int numProducts = products.size();

        Product randomProduct = products.get(ThreadLocalRandom.current().nextInt(0, numCustomers -1));
        Customer randomCustomer = customers.get(ThreadLocalRandom.current().nextInt(0, numProducts -1));

        Order order = new Order();
        order.setId(id);
        order.setCustomerId(randomCustomer.getId());
        order.setProductId(randomProduct.getId());
        int productQty = ThreadLocalRandom.current().nextInt(-1, 6);
        order.setProductQuantity(productQty);
        order.setTotalAmount(Double.parseDouble(df.format(productQty * randomProduct.getProductCost())));
        return order;
    }

    private DlqMessage buildDlqMessage(Order order, String errorMessage) {

        return DlqMessage.newBuilder()
                .setSource("orders-producer")
                .setErrorTimestamp(System.currentTimeMillis())
                .setErrorMessage(errorMessage)
                .setOriginalKey(order != null ? order.getId() : null)
                .setOriginalPayload(order != null ? gson.toJson(order) : "NOT-AVAILABLE")
                .build();
    }

}
