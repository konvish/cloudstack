package org.apache.sirona.configuration;

import java.util.Properties;

/**
 * Created by kong on 2016/1/24.
 */
public interface ConfigurationProvider {
    int ordinal();

    Properties configuration();
}
