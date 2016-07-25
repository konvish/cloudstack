package org.apache.sirona.configuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.configuration.ConfigurationProvider;
import org.apache.sirona.configuration.FileConfigurationProvider;
import org.apache.sirona.configuration.PropertiesConfigurationProvider;
/**
 * Created by kong on 2016/1/24.
 */
public final class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    public static final String CONFIG_PROPERTY_PREFIX = "org.apache.org.apache.sirona.";
    private static final String[] DEFAULT_CONFIGURATION_FILES = new String[]{"org.apache.sirona.properties", "collector-org.apache.sirona.properties"};
    private static final Properties PROPERTIES = new Properties();

    public static boolean is(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
    }

    public static int getInteger(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, Integer.toString(defaultValue)));
    }

    public static String getProperty(String key, String defaultValue) {
        String property = PROPERTIES.getProperty(key, defaultValue);
        return property != null && property.startsWith("${") && property.endsWith("}")?getProperty(property.substring("${".length(), property.length() - 1), defaultValue):property;
    }

    public static String[] getArray(String key, String[] defaultValue) {
        String property = PROPERTIES.getProperty(key);
        return property == null?defaultValue:property.split(";");
    }

    private Configuration() {
    }

    static {
        try {
            LinkedList e = new LinkedList();
            String[] i$ = DEFAULT_CONFIGURATION_FILES;
            int provider = i$.length;

            for(int i$1 = 0; i$1 < provider; ++i$1) {
                String source = i$[i$1];
                e.add(new FileConfigurationProvider(source));
            }

            e.add(new PropertiesConfigurationProvider(System.getProperties()));
            Iterator var6 = ServiceLoader.load(ConfigurationProvider.class, Configuration.class.getClassLoader()).iterator();

            ConfigurationProvider var7;
            while(var6.hasNext()) {
                var7 = (ConfigurationProvider)var6.next();
                e.add(var7);
            }

            Collections.sort(e, Configuration.Sorter.INSTANCE);
            var6 = e.iterator();

            while(var6.hasNext()) {
                var7 = (ConfigurationProvider)var6.next();
                PROPERTIES.putAll(var7.configuration());
            }
        } catch (Exception var5) {
            LOGGER.log(Level.SEVERE, var5.getMessage(), var5);
        }

    }

    private static class Sorter implements Comparator<ConfigurationProvider> {
        public static final Comparator<? super ConfigurationProvider> INSTANCE = new Configuration.Sorter();

        private Sorter() {
        }

        public int compare(ConfigurationProvider o1, ConfigurationProvider o2) {
            return o1.ordinal() - o2.ordinal();
        }
    }
}
