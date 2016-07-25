package com.kong.monitor;

import com.kong.monitor.LoggerGaugeDataStore;
import java.util.Collection;
import java.util.Iterator;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.store.counter.BatchCounterDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class LoggerCounterDataStore extends BatchCounterDataStore {
    public static final Logger logger = LoggerFactory.getLogger(LoggerGaugeDataStore.class.getName());
    private static final Logger SPEC_LOGGER = LoggerFactory.getLogger("COUNT-LOGGER");
    private static final String COUNTER_PREFIX = "counter-";
    private static final char SEP = '-';

    public LoggerCounterDataStore() {
    }

    protected synchronized void pushCountersByBatch(Collection<Counter> instances) {
        try {
            long e = System.currentTimeMillis();
            Iterator i$ = instances.iterator();

            while(i$.hasNext()) {
                Counter counter = (Counter)i$.next();
                Key key = counter.getKey();
                String prefix = "counter-" + key.getRole().getName() + '-' + key.getName() + '-';
                MetricData[] arr$ = MetricData.values();
                int len$ = arr$.length;

                for(int i$1 = 0; i$1 < len$; ++i$1) {
                    MetricData data = arr$[i$1];
                    SPEC_LOGGER.error(prefix + data.name() + '-' + data.value(counter) + '-' + e);
                }
            }
        } catch (Exception var12) {
            logger.error("logger exception", var12);
        }

    }
}
