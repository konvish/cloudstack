package org.apache.sirona.spi;

import java.util.ServiceLoader;
import org.apache.sirona.spi.SPI;
/**
 * Created by kong on 2016/1/24.
 */
public class DefaultSPI implements SPI {
    public DefaultSPI() {
    }

    public <T> Iterable<T> find(Class<T> api, ClassLoader loader) {
        return ServiceLoader.load(api, loader);
    }
}
