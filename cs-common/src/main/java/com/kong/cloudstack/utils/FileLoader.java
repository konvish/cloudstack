package com.kong.cloudstack.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kong on 2016/1/22.
 */
public class FileLoader {
    private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);

    public FileLoader() {
    }

    public static Properties getFile(String file) {
        Properties properties = new Properties();

        try {
            if(!Strings.isNullOrEmpty(file)) {
                InputStream e = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
                if(e == null) {
                    file = file.substring(1);
                    e = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
                }

                properties.load(e);
            }
        } catch (IOException var3) {
            logger.error("load properties {} error", file, var3);
            System.exit(-1);
        }

        return properties;
    }
}