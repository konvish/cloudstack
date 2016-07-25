package org.apache.sirona.store;

import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class DelegateDataStoreFactory implements DataStoreFactory {
    private final CounterDataStore counterDataStore;
    private final CommonGaugeDataStore gaugeDataStore;
    private final NodeStatusDataStore nodeStatusDataStore;
    private final PathTrackingDataStore pathTrackingDataStore;

    public DelegateDataStoreFactory(CounterDataStore counterDataStore, CommonGaugeDataStore gaugeDataStore, NodeStatusDataStore nodeStatusDataStore, PathTrackingDataStore pathTrackingDataStore) {
        this.counterDataStore = counterDataStore;
        this.gaugeDataStore = gaugeDataStore;
        this.nodeStatusDataStore = nodeStatusDataStore;
        this.pathTrackingDataStore = pathTrackingDataStore;
    }

    public CounterDataStore getCounterDataStore() {
        return this.counterDataStore;
    }

    public CommonGaugeDataStore getGaugeDataStore() {
        return this.gaugeDataStore;
    }

    public NodeStatusDataStore getNodeStatusDataStore() {
        return this.nodeStatusDataStore;
    }

    public PathTrackingDataStore getPathTrackingDataStore() {
        return this.pathTrackingDataStore;
    }
}

