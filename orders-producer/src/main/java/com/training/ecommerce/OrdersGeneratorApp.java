package com.training.ecommerce;

import com.training.ecommerce.helper.PropertiesLoader;
import com.training.ecommerce.solution.producer.DlqProducer;
import com.training.ecommerce.solution.producer.OrdersProducer;
import com.training.ecommerce.solution.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.text.DecimalFormat;
import java.util.*;

@SpringBootApplication
public class OrdersGeneratorApp implements CommandLineRunner {

    private final DecimalFormat df = new DecimalFormat("#.##");

    private static final String ORDERS_TOPIC = "demo.ecommerce.orders";
    private static final String DLQ_TOPIC = "demo.ecommerce.orders-dlq";
    private static final Logger logger = LoggerFactory.getLogger(OrdersGeneratorApp.class);

    private final ConfigurableApplicationContext applicationContext;

    public OrdersGeneratorApp(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(OrdersGeneratorApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Properties props = PropertiesLoader.loadProperties("configuration.properties");

        final OrdersProducer ordersProducer = new OrdersProducer(props, ORDERS_TOPIC);
        final DlqProducer dlqProducer = new DlqProducer(props, DLQ_TOPIC);
        final OrdersService ordersService = new OrdersService(ordersProducer, dlqProducer);
        ordersService.execute();
    }

}
