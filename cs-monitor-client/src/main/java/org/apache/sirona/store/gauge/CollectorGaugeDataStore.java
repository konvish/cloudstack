package org.apache.sirona.store.gauge;

import java.util.Collection;
import java.util.SortedMap;
import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
/**
 * Created by kong on 2016/1/24.
 */
public interface CollectorGaugeDataStore extends CommonGaugeDataStore {
    SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest var1, String var2);

    void createOrNoopGauge(Role var1, String var2);

    void addToGauge(Role var1, long var2, double var4, String var6);

    Collection<String> markers();
}