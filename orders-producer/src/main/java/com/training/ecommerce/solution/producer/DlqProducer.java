package com.training.ecommerce.solution.producer;

import com.training.ecommerce.model.DlqMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class DlqProducer implements IProducer<String, DlqMessage> {

    private final String topicName;

    private final Producer<String, DlqMessage> producer;

    public DlqProducer(Properties properties, String topicName) {
        this.producer = new KafkaProducer<>(properties);
        this.topicName = topicName;
    }

    @Override
    public void send(String key, DlqMessage dlqMessage) {
        final ProducerRecord<String, DlqMessage> producerRecord = new ProducerRecord<>(topicName, key, dlqMessage);
        producer.send(producerRecord);
    }

    @Override
    public void close() {
        producer.close();
    }
}
