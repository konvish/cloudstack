package org.apache.sirona.store;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.DelegateDataStoreFactory;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.counter.InMemoryCounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.gauge.InMemoryGaugeDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.status.PeriodicNodeStatusDataStore;
import org.apache.sirona.store.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class DefaultDataStoreFactory extends DelegateDataStoreFactory {
    public DefaultDataStoreFactory() {
        super((CounterDataStore)IoCs.processInstance(new InMemoryCounterDataStore()), (CommonGaugeDataStore)IoCs.processInstance(new InMemoryGaugeDataStore()), (NodeStatusDataStore)IoCs.processInstance(new PeriodicNodeStatusDataStore()), (PathTrackingDataStore)IoCs.processInstance(new InMemoryPathTrackingDataStore()));
    }
}