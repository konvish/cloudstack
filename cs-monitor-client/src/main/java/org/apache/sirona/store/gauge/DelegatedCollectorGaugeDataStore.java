package org.apache.sirona.store.gauge;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.store.gauge.InMemoryGaugeDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class DelegatedCollectorGaugeDataStore implements CollectorGaugeDataStore {
    private final ConcurrentMap<String, GaugeDataStore> dataStores = new ConcurrentHashMap();
    private final Class<? extends GaugeDataStore> delegateClass;

    public DelegatedCollectorGaugeDataStore() {
        try {
            this.delegateClass = (Class)Class.class.cast(DelegatedCollectorGaugeDataStore.class.getClassLoader().loadClass(Configuration.getProperty("org.apache.org.apache.sirona.collector.gauge.store-class", InMemoryGaugeDataStore.class.getName())));
        } catch (ClassNotFoundException var2) {
            throw new SironaException(var2.getMessage(), var2);
        }
    }

    protected GaugeDataStore newStore(String marker) {
        try {
            try {
                Constructor e = this.delegateClass.getConstructor(new Class[]{String.class});
                return (GaugeDataStore)e.newInstance(new Object[]{marker});
            } catch (Exception var3) {
                return (GaugeDataStore)this.delegateClass.newInstance();
            }
        } catch (Exception var4) {
            throw new SironaException(var4);
        }
    }

    public SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest gaugeValuesRequest, String marker) {
        GaugeDataStore gaugeDataStore = (GaugeDataStore)this.dataStores.get(marker);
        return (SortedMap)(gaugeDataStore == null?new TreeMap():gaugeDataStore.getGaugeValues(gaugeValuesRequest));
    }

    public void createOrNoopGauge(Role role, String marker) {
        GaugeDataStore gaugeDataStore = (GaugeDataStore)this.dataStores.get(marker);
        if(gaugeDataStore == null) {
            gaugeDataStore = this.newStore(marker);
            GaugeDataStore existing = (GaugeDataStore)this.dataStores.putIfAbsent(marker, gaugeDataStore);
            if(existing != null) {
                gaugeDataStore = existing;
            }
        }

        gaugeDataStore.createOrNoopGauge(role);
    }

    public void addToGauge(Role role, long time, double value, String marker) {
        this.createOrNoopGauge(role, marker);
        ((GaugeDataStore)this.dataStores.get(marker)).addToGauge(role, time, value);
    }

    public Collection<String> markers() {
        return this.dataStores.keySet();
    }

    public SortedMap<Long, Double> getGaugeValues(GaugeValuesRequest gaugeValuesRequest) {
        TreeMap values = new TreeMap();
        Iterator i$ = this.dataStores.entrySet().iterator();

        while(i$.hasNext()) {
            Entry marker = (Entry)i$.next();
            SortedMap gaugeValues = ((GaugeDataStore)marker.getValue()).getGaugeValues(gaugeValuesRequest);
            Iterator i$1 = gaugeValues.entrySet().iterator();

            while(i$1.hasNext()) {
                Entry entry = (Entry)i$1.next();
                Long key = (Long)entry.getKey();
                Double value = (Double)values.get(key);
                Double thisValue = (Double)entry.getValue();
                if(value == null) {
                    values.put(key, thisValue);
                } else {
                    values.put(key, Double.valueOf(value.doubleValue() + thisValue.doubleValue()));
                }
            }
        }

        return values;
    }

    public Collection<Role> gauges() {
        HashSet roles = new HashSet();
        Iterator i$ = this.dataStores.values().iterator();

        while(i$.hasNext()) {
            GaugeDataStore store = (GaugeDataStore)i$.next();
            roles.addAll(store.gauges());
        }

        return roles;
    }

    public Role findGaugeRole(String name) {
        Iterator i$ = this.dataStores.values().iterator();

        Role role;
        do {
            if(!i$.hasNext()) {
                return null;
            }

            GaugeDataStore store = (GaugeDataStore)i$.next();
            role = store.findGaugeRole(name);
        } while(role == null);

        return role;
    }

    public void gaugeStopped(Role gauge) {
        Iterator i$ = this.dataStores.values().iterator();

        while(i$.hasNext()) {
            GaugeDataStore store = (GaugeDataStore)i$.next();
            store.gaugeStopped(gauge);
        }

    }

    public void reset() {
        this.dataStores.clear();
    }
}