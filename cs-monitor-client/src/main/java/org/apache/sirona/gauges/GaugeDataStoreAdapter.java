package org.apache.sirona.gauges;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.sirona.Role;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeAware;
import org.apache.sirona.store.gauge.GaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;

public class GaugeDataStoreAdapter implements GaugeDataStore, GaugeAware {
    protected final Map<Role, Gauge> gauges = new ConcurrentHashMap();

    public GaugeDataStoreAdapter() {
    }

    public SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest gaugeValuesRequest) {
        return new TreeMap();
    }

    public void createOrNoopGauge(Role role) {
    }

    public void addToGauge(Role role, long time, double value) {
    }

    public Collection<Role> gauges() {
        return this.gauges.keySet();
    }

    public Role findGaugeRole(String name) {
        return null;
    }

    public void gaugeStopped(Role gauge) {
        this.gauges.remove(gauge);
    }

    public void addGauge(Gauge gauge) {
        this.gauges.put(gauge.role(), gauge);
    }

    public Collection<Gauge> getGauges() {
        return this.gauges.values();
    }
}
