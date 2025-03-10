package com.training.ecommerce.solution.service;

import com.training.ecommerce.model.Customer;
import com.training.ecommerce.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbService {

    private static final String jdbcUrl = "jdbc:mysql://<db-hostname>:3306/ecommerce";

    private static final String username = "<DB_USERNAME>";

    private static final String password = "DB_PASSWORD";

    public static List<Product> collectProducts() throws ClassNotFoundException {
        List<Product> products = new ArrayList<>();

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("select * from products;")) {
                while (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getString("id"));
                    product.setProductName(resultSet.getString("product_name"));
                    product.setProductCost(resultSet.getDouble("product_cost"));

                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public static List<Customer> collectCustomers() throws ClassNotFoundException {
        List<Customer> customers = new ArrayList<>();

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("select * from customers;")) {
                while (resultSet.next()) {
                    Customer customer = new Customer();
                    customer.setId(resultSet.getString("id"));
                    customer.setCustomerName(resultSet.getString("customer_name"));
                    customer.setCustomerAddress(resultSet.getString("customer_address"));
                    customer.setCustomerEmail(resultSet.getString("customer_email"));
                    customer.setCardNumber(resultSet.getString("card_number"));
                    customer.setLevel(resultSet.getString("level"));
                    customers.add(customer);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

}
