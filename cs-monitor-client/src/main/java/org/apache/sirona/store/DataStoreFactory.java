package org.apache.sirona.store;

import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public interface DataStoreFactory {
    CounterDataStore getCounterDataStore();

    CommonGaugeDataStore getGaugeDataStore();

    NodeStatusDataStore getNodeStatusDataStore();

    PathTrackingDataStore getPathTrackingDataStore();
}

