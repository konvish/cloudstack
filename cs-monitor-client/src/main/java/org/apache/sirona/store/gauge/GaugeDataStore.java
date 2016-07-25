package org.apache.sirona.store.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public interface GaugeDataStore extends CommonGaugeDataStore {
    void createOrNoopGauge(Role var1);

    void addToGauge(Role var1, long var2, double var4);
}
