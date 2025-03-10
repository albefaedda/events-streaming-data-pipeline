package com.training.ecommerce.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(fileName);
        envProps.load(is);
        if (is != null) is.close();

        System.out.println("Props size: " + envProps.size());
        System.out.println("Properties: " + getPropertyAsString(envProps));

        return envProps;
    }

    public static String getPropertyAsString(Properties prop) throws IOException {
        StringWriter writer = new StringWriter();
        prop.store(new PrintWriter(writer), "");
        return writer.getBuffer().toString();
    }
}
