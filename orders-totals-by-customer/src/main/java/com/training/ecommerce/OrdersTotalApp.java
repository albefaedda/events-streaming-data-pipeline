package com.training.ecommerce;

import com.training.ecommerce.helper.PropertiesLoader;
import com.training.ecommerce.solution.topology.OrderTotalsTopology;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class OrdersTotalApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrdersTotalApp.class);

    private final ConfigurableApplicationContext applicationContext;

    public OrdersTotalApp(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(OrdersTotalApp.class, args);
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
        OrderTotalsTopology orderTotalsTopology = new OrderTotalsTopology(streamsConfiguration);
        return new KafkaStreams(orderTotalsTopology.buildTopology(), streamsConfiguration);
    }
}
