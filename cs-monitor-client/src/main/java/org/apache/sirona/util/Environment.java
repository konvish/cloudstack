package org.apache.sirona.util;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CollectorCounterStore;
/**
 * Created by kong on 2016/1/24.
 */
public final class Environment {
    private Environment() {
    }

    public static boolean isCollector() {
        IoCs.findOrCreateInstance(Repository.class);
        return IoCs.getInstance(CollectorCounterStore.class) != null;
    }
}
