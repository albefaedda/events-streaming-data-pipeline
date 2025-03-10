# Event Streaming Data Pipeline making use of Kafka Connectors, Kafka Producer and Kafka Streams microservices

This repo is an example of Event Streaming data Pipeline, which follows the architecture depicted in the picture below.

![Streaming Data Pipeline](streaming-data-pipeline.png)

## Database

In this data pipeline we need to create a MySQL database from which we ingest data into the Kafka Topics. 
The `ddl.sql` and `dml.sql` files in the `database` path contain the required queries to define the database schema and insert some data into the tables.
The database requires a bin-log enabled to be read from our Change Data Capture (CDC) connectors. 

## Connectors

### Source Connectors

To ingest data into the Kafka Topics we make use of Debezium MySQL CDC Source Connector. 
We make two instances of this connector to accommodate the different Single Message Transform (SMTs) that we define for each of them. 

Both the connectors configurations have a list of SMTs to create the Kafka message key and define the namespace of the schema, the customer's connector also has an SMT to mask the credit_card number

The configurations available in the `connectors` path are for Confluent Cloud Fully-Managed connectors deployment

### Sink Connectors

At the opposite end of the Event Streaming Pipeline we sink the data to MongoDB and for this we use the MongoDb Atlas Sink Connector Fully Managed in Confluent Cloud.
This connector sinks data containing the sun of `orders` for each customer. The configuration of this connector is in the `connectors` path. 

The connector configuration includes an SMT to define the MongoDb message _id from the Kafka message key

## Java Microservices 

### orders-producer

The orders-producer application is a simple Java microservice to Produce random `orders` to a Kafka topic.
It reads data regarding `customers` and `products` from 2 database tables and then generates orders referencing this data. 

The `order` has an Avro schema and the avro-maven-plugin is used to automatically generate the Java class corresponding to this schema.
Just run `mvn install` to create the application Jar and generate the classes associated to the schemas. 

### orders-details

The orders-details microservice is a stateless Kafka Streams microservice which takes in input 3 topics: 
The `customers` and `products` topics are read as `GlobalKTable`, while the `orders` topic is read as a `KStream`. 

These 3 topics are joined together using the id of each set of data to create an order containing details of the customer and the product. 
It uses Avro schemas for the messages value. To generate the Java classes from the schema, run `mvn install`.

### orders-totals-by-customer

The orders-total-by-customer microservice is a stateful Kafka Streams microservice which groups the orders by `customer-id`, which is the message key, and aggregates them calculating the total. 
It also uses Avro schemas and, same as the other services, you need to run `mvn install` to generate the Java class from the schema.

