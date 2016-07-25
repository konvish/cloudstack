package com.kong.monitor;

import com.kong.monitor.LoggerCounterDataStore;
import com.kong.monitor.LoggerGaugeDataStore;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.DelegateDataStoreFactory;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.status.EmptyStatuses;
import org.apache.sirona.store.tracking.InMemoryPathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class LoggerDataStoreFactory extends DelegateDataStoreFactory {
    public LoggerDataStoreFactory() {
        super((CounterDataStore)IoCs.processInstance(new LoggerCounterDataStore()), (CommonGaugeDataStore)IoCs.processInstance(new LoggerGaugeDataStore()), new EmptyStatuses(), new InMemoryPathTrackingDataStore());
    }
}
