package org.ideaslabut.aws.lambda.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ApplicationPropertiesService {
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String APPLICATION_PROPERTIES_LOCAL_FILE = "application.local.properties";
    private static ApplicationPropertiesService INSTANCE = null;

    private final Properties properties;

    public static ApplicationPropertiesService getInstance() {
        if (INSTANCE == null) {
            synchronized (ApplicationPropertiesService.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildInstance();
                }
            }
        }
        return INSTANCE;
    }

    private static ApplicationPropertiesService buildInstance() {
        var properties = new Properties();
        try {
            properties.load(ApplicationPropertiesService.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
            properties.load(ApplicationPropertiesService.class.getResourceAsStream(APPLICATION_PROPERTIES_LOCAL_FILE));
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        return new ApplicationPropertiesService(properties);
    }

    private ApplicationPropertiesService(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
