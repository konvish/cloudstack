package com.kong.monitor;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.AggregatedGaugeDataStoreAdapter;
import org.apache.sirona.store.gauge.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class LoggerGaugeDataStore extends AggregatedGaugeDataStoreAdapter {
    public static final Logger logger = LoggerFactory.getLogger(LoggerGaugeDataStore.class.getName());
    private static final Logger SPEC_LOGGER = LoggerFactory.getLogger("GAUGE-LOGGER");
    private static final char SEP = '-';
    private static final String GAUGE_PREFIX = "gauge-";

    public LoggerGaugeDataStore() {
    }

    protected void pushAggregatedGauges(Map<Role, Value> gauges) {
        try {
            long e = System.currentTimeMillis();
            Iterator i$ = gauges.entrySet().iterator();

            while(i$.hasNext()) {
                Entry gauge = (Entry)i$.next();
                SPEC_LOGGER.error("gauge-" + ((Role)gauge.getKey()).getName() + '-' + ((Value)gauge.getValue()).getMean() + '-' + e);
            }
        } catch (Exception var6) {
            logger.error("error exception", var6);
        }

    }
}