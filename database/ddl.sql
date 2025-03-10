CREATE DATABASE ecommerce;

CREATE TABLE ecommerce.products (
  id varchar(10) NOT NULL,
  product_name varchar(60),
  product_cost double,
  PRIMARY KEY (id)
)

CREATE TABLE ecommerce.products (
	id integer primary key,
    product_name varchar(60),
    product_cost double,
    product_quantity integer,
    last_update_time TIMESTAMP not null
);


CREATE TABLE ecommerce.customers (
  id varchar(20) NOT NULL,
  customer_name varchar(60),
  customer_email varchar(60),
  customer_address varchar(80),
  card_number varchar(20),
  `level` varchar(10),
  PRIMARY KEY (id)
}

show variables like 'log_bin';
show variables like 'binlog_format';