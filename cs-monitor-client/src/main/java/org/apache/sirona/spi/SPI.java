package org.apache.sirona.spi;

import org.apache.sirona.configuration.ioc.IoCs;
/**
 * Created by kong on 2016/1/24.
 */
public interface SPI {
    SPI INSTANCE = (SPI)IoCs.findOrCreateInstance(SPI.class);

    <T> Iterable<T> find(Class<T> var1, ClassLoader var2);
}
