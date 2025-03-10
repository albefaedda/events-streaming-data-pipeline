package com.training.ecommerce.solution.producer;

public interface IProducer<K, V> {

    public void send(K key, V value);

    public void close();
}
