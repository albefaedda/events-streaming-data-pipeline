package com.training.ecommerce;

import com.training.ecommerce.helper.PropertiesLoader;
import com.training.ecommerce.solution.topology.OrderDetailsTopology;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class OrderEnrichmentApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrderEnrichmentApp.class);

    private final ConfigurableApplicationContext applicationContext;

    public OrderEnrichmentApp(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(OrderEnrichmentApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Properties streamProperties = PropertiesLoader.loadProperties("configuration.properties");

        try(KafkaStreams streams = buildStream(streamProperties)) {

            // Define handler in case of unmanaged exception
            streams.setUncaughtExceptionHandler( e -> {
                logger.error("Uncaught exception occurred in Kafka Streams. Application will shutdown ! ", e);

                // Consider REPLACE_THREAD if the exception is retriable
                // Consider SHUTDOWN_APPLICATION if the exception may propagate to other instances after rebalance
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
            });

            // Hook the main application to Kafka Stream Lifecycle, to avoid zombie stream application
            streams.setStateListener(((newState, oldState) -> {
                if (newState == KafkaStreams.State.PENDING_ERROR) {
                    //Stop the app in case of error
                    if (applicationContext.isActive()) {
                        SpringApplication.exit(applicationContext, () -> 1);
                    }
                }
            }));

            streams.cleanUp();

            // Start stream execution
            streams.start();

            //printStoresKeys(streams, PRODUCT_STORE);
            //printStoresKeys(streams, CUSTOMER_STORE);

            // Ensure your app respond gracefully to external shutdown signal
            final CountDownLatch latch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                streams.close();
                latch.countDown();
            }));

            latch.await();
        }
    }


    protected KafkaStreams buildStream(Properties streamsConfiguration) {
        OrderDetailsTopology orderDetailsTopology = new OrderDetailsTopology(streamsConfiguration);
        logger.info("Streams topology created");
        return new KafkaStreams(orderDetailsTopology.buildTopology(), streamsConfiguration);
    }

    private void printStoresKeys(KafkaStreams streams, String storeName) {

        ReadOnlyKeyValueStore<Object, Object> store = streams.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
        KeyValueIterator<Object, Object> allItems = store.all();
        logger.info("Printing keys of store " + storeName);
        while (allItems.hasNext()) {
            KeyValue<Object, Object> entry = allItems.next();
            logger.info("id " +  entry.key);
        }
        allItems.close();
    }
}
