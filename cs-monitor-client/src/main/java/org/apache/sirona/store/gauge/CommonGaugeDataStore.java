package org.apache.sirona.store.gauge;

import java.util.Collection;
import java.util.SortedMap;
import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
/**
 * Created by kong on 2016/1/24.
 */
public interface CommonGaugeDataStore {
    SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest var1);

    Collection<Role> gauges();

    Role findGaugeRole(String var1);

    void gaugeStopped(Role var1);
}