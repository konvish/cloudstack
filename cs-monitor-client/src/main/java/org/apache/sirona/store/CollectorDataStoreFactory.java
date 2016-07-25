package org.apache.sirona.store;

import org.apache.sirona.store.DelegateDataStoreFactory;
import org.apache.sirona.store.counter.InMemoryCollectorCounterStore;
import org.apache.sirona.store.gauge.DelegatedCollectorGaugeDataStore;
import org.apache.sirona.store.status.InMemoryCollectorNodeStatusDataStore;
import org.apache.sirona.store.tracking.DelegatedCollectorPathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class CollectorDataStoreFactory extends DelegateDataStoreFactory {
    public CollectorDataStoreFactory() {
        super(new InMemoryCollectorCounterStore(), new DelegatedCollectorGaugeDataStore(), new InMemoryCollectorNodeStatusDataStore(), new DelegatedCollectorPathTrackingDataStore());
    }
}