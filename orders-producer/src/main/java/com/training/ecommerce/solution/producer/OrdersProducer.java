package com.training.ecommerce.solution.producer;

import com.training.ecommerce.model.Order;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class OrdersProducer implements IProducer<String, Order> {

    private final String topicName;

    private final Producer<String, Order> producer;

    public OrdersProducer(Properties properties, String topicName) {
        this.producer = new KafkaProducer<>(properties);
        this.topicName = topicName;
    }


    @Override
    public void send(String key, Order order) {
        final ProducerRecord<String, Order> producerRecord = new ProducerRecord<>(topicName, key, order);
        producer.send(producerRecord);
    }

    @Override
    public void close() {
        producer.close();
    }
}
