package com.kong.monitor;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.DelegateDataStoreFactory;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.store.status.EmptyStatuses;
/**
 * 日志管理工厂
 * Created by kong on 2016/1/24.
 */
public class LoggerDataStoreFactory extends DelegateDataStoreFactory {
    public LoggerDataStoreFactory() {
        super((CounterDataStore)IoCs.processInstance(new LoggerCounterDataStore()), (CommonGaugeDataStore)IoCs.processInstance(new LoggerGaugeDataStore()), new EmptyStatuses(), new InMemoryPathTrackingDataStore());
    }
}
