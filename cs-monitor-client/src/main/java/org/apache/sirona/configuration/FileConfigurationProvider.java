package org.apache.sirona.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.configuration.ConfigurationProvider;
/**
 * Created by kong on 2016/1/24.
 */
public class FileConfigurationProvider implements ConfigurationProvider {
    private static final Logger LOGGER = Logger.getLogger(FileConfigurationProvider.class.getName());
    private final String name;

    public FileConfigurationProvider(String name) {
        this.name = name;
    }

    public int ordinal() {
        return 50;
    }

    public Properties configuration() {
        Properties properties = new Properties();
        String filename = System.getProperty("org.apache.org.apache.sirona.configuration." + this.name, this.name);
        if((new File(filename)).exists()) {
            FileInputStream classLoader = null;

            try {
                classLoader = new FileInputStream(filename);
                properties.load(classLoader);
            } catch (IOException var8) {
                LOGGER.log(Level.SEVERE, var8.getMessage(), var8);
            } finally {
                this.closeQuietly(classLoader);
            }
        } else {
            ClassLoader classLoader1 = FileConfigurationProvider.class.getClassLoader();
            if(classLoader1 == null) {
                classLoader1 = ClassLoader.getSystemClassLoader();
            }

            this.load(properties, filename, classLoader1);
        }

        return properties;
    }

    private boolean load(Properties properties, String filename, ClassLoader classLoader) {
        InputStream stream = classLoader.getResourceAsStream(filename);
        if(stream != null) {
            try {
                properties.load(stream);
            } catch (IOException var6) {
                LOGGER.log(Level.SEVERE, var6.getMessage(), var6);
            }
        }

        return stream != null;
    }

    private void closeQuietly(InputStream inputStream) {
        try {
            if(inputStream != null) {
                inputStream.close();
            }
        } catch (IOException var3) {
            LOGGER.log(Level.WARNING, "fail to close inputStream: " + var3.getMessage(), var3);
        }

    }
}
