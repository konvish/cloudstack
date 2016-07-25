package org.apache.sirona.store.gauge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter.Measure;
/**
 * Created by kong on 2016/1/24.
 */
public class InMemoryGaugeDataStore extends BatchGaugeDataStoreAdapter {
    protected final ConcurrentMap<Role, SortedMap<Long, Double>> gauges = new ConcurrentHashMap();
    protected final Map<String, Role> roleMapping = new ConcurrentHashMap();

    public InMemoryGaugeDataStore() {
    }

    public SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest gaugeValuesRequest) {
        Map map = (Map)this.gauges.get(gaugeValuesRequest.getRole());
        if(map == null) {
            return new TreeMap();
        } else {
            HashMap copy = new HashMap(map);
            TreeMap out = new TreeMap();
            Iterator i$ = copy.entrySet().iterator();

            while(i$.hasNext()) {
                Entry entry = (Entry)i$.next();
                long time = ((Long)entry.getKey()).longValue();
                if(time >= gaugeValuesRequest.getStart() && time <= gaugeValuesRequest.getEnd()) {
                    out.put(Long.valueOf(time), entry.getValue());
                }
            }

            return out;
        }
    }

    public void createOrNoopGauge(Role gauge) {
        this.gauges.putIfAbsent(gauge, new InMemoryGaugeDataStore.FixedSizedMap());
        this.roleMapping.put(gauge.getName(), gauge);
    }

    public void addToGauge(Role role, long time, double value) {
        ((SortedMap)this.gauges.get(role)).put(Long.valueOf(time), Double.valueOf(value));
    }

    public Collection<Role> gauges() {
        return this.gauges.keySet();
    }

    public Role findGaugeRole(String name) {
        return (Role)this.roleMapping.get(name);
    }

    protected void pushGauges(Map<Role, Measure> gauges) {
        Iterator i$ = gauges.entrySet().iterator();

        while(i$.hasNext()) {
            Entry entry = (Entry)i$.next();
            Measure value = (Measure)entry.getValue();
            this.addToGauge((Role)entry.getKey(), value.getTime(), value.getValue());
        }

    }

    public void gaugeStopped(Role gauge) {
        super.gaugeStopped(gauge);
        this.roleMapping.remove(gauge.getName());
    }

    protected static class FixedSizedMap extends ConcurrentSkipListMap<Long, Double> {
        private static final int MAX_SIZE = Configuration.getInteger("org.apache.org.apache.sirona.gauge.max-size", 100);

        protected FixedSizedMap() {
        }

        public Double put(Long key, Double value) {
            if(this.size() >= MAX_SIZE) {
                this.remove(this.keySet().iterator().next());
            }

            return (Double)super.put(key, value);
        }
    }
}
