package com.kong.cloudstack.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * Created by kong on 2016/1/22.
 */
public class ConfigLoader {
    public static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    public static final String APP_MAINCONF_FILE = "/config/main-conf.properties";
    public static final String APP_MAINCONF_FILE_DOUBLECHECK = "config/main-conf.properties";
    private static final String DEFAULT_CONFIG_FILE_KEY = "configFile";
    private static Properties properties = null;

    private ConfigLoader() {
    }

    public static ConfigLoader getInstance() {
        return ConfigLoader.ConfigLoaderHolder.instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    static {
        properties = new Properties();

        try {
            String e = System.getProperty("configFile");
            if(Strings.isNullOrEmpty(e)) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/config/main-conf.properties");
                if(is == null) {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/main-conf.properties");
                }

                properties.load(is);
            } else {
                properties.load(new FileInputStream(new File(e)));
            }
        } catch (IOException var2) {
            logger.error("load properties {} error", "configFile", var2);
            System.exit(-1);
        }

    }

    private static class ConfigLoaderHolder {
        private static final ConfigLoader instance = new ConfigLoader();

        private ConfigLoaderHolder() {
        }
    }
}