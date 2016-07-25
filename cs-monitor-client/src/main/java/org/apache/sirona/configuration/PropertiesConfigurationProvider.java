package org.apache.sirona.configuration;

import java.util.Properties;
import org.apache.sirona.configuration.ConfigurationProvider;
/**
 * Created by kong on 2016/1/24.
 */
public class PropertiesConfigurationProvider implements ConfigurationProvider {
    private final Properties properties;

    public PropertiesConfigurationProvider(Properties properties) {
        this.properties = properties;
    }

    public int ordinal() {
        return 100;
    }

    public Properties configuration() {
        return this.properties;
    }
}
