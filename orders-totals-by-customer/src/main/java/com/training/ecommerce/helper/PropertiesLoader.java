package com.training.ecommerce.helper;

import com.training.ecommerce.OrdersTotalApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class PropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties streamProps = new Properties();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(fileName);
        streamProps.load(is);
        if (is != null) is.close();

        logger.info("Props size: " + streamProps.size());
        logger.info("Properties: " + getPropertyAsString(streamProps));

        return streamProps;
    }

    public static String getPropertyAsString(Properties prop) throws IOException {
        StringWriter writer = new StringWriter();
        prop.store(new PrintWriter(writer), "");
        return writer.getBuffer().toString();
    }
}
