package com.kong.monitor;

import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.store.memory.counter.BatchCounterDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
/**
 * Counter日志管理
 * Created by kong on 2016/1/24.
 */
public class LoggerCounterDataStore extends BatchCounterDataStore {
    public static final Logger logger = LoggerFactory.getLogger(LoggerGaugeDataStore.class.getName());
    private static final Logger SPEC_LOGGER = LoggerFactory.getLogger("COUNT-LOGGER");
    private static final String COUNTER_PREFIX = "counter-";
    private static final char SEP = '-';

    public LoggerCounterDataStore() {
    }

    /**
     * 批量添加Counter事件
     * @param instances C<Counter>
     */
    protected synchronized void pushCountersByBatch(Collection<Counter> instances) {
        try {
            long e = System.currentTimeMillis();

            for (Counter counter : instances) {
                Key key = counter.getKey();
                String prefix = COUNTER_PREFIX + key.getRole().getName() + SEP + key.getName() + SEP;
                MetricData[] metricData = MetricData.values();

                for (MetricData data : metricData) {
                    SPEC_LOGGER.error(prefix + data.name() + SEP + data.value(counter) + SEP + e);
                }
            }
        } catch (Exception var12) {
            logger.error("logger exception", var12);
        }

    }
}
