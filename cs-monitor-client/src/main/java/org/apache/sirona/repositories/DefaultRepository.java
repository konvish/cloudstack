package org.apache.sirona.repositories;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeAware;
import org.apache.sirona.gauges.jvm.ActiveThreadGauge;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.gauges.jvm.UsedNonHeapMemoryGauge;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.stopwatches.CounterStopWatch;
import org.apache.sirona.stopwatches.StopWatch;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class DefaultRepository implements Repository {
    protected final CounterDataStore counterDataStore;
    protected final NodeStatusDataStore nodeStatusDataStore;
    protected final CommonGaugeDataStore gaugeDataStore;
    protected final PathTrackingDataStore pathTrackingDataStore;

    public DefaultRepository() {
        this(findCounterDataStore(), findGaugeDataStore(), findStatusDataStore(), findPathTrackingDataStore());
    }

    protected DefaultRepository(CounterDataStore counter, CommonGaugeDataStore gauge, NodeStatusDataStore status, PathTrackingDataStore pathTrackingDataStore) {
        this.counterDataStore = counter;
        this.gaugeDataStore = gauge;
        this.nodeStatusDataStore = status;
        this.pathTrackingDataStore = pathTrackingDataStore;
        if(CollectorCounterStore.class.isInstance(counter)) {
            IoCs.setSingletonInstance(CollectorCounterStore.class, counter);
        } else {
            IoCs.setSingletonInstance(CounterDataStore.class, counter);
        }

        if(CollectorGaugeDataStore.class.isInstance(gauge)) {
            IoCs.setSingletonInstance(CollectorGaugeDataStore.class, gauge);
        } else {
            IoCs.setSingletonInstance(GaugeDataStore.class, gauge);
        }

        if(CollectorNodeStatusDataStore.class.isInstance(status)) {
            IoCs.setSingletonInstance(CollectorNodeStatusDataStore.class, status);
        } else {
            IoCs.setSingletonInstance(NodeStatusDataStore.class, status);
        }

        if(Configuration.is("org.apache.org.apache.sirona.core.gauge.activated", true)) {
            this.addGauge(new CPUGauge());
            this.addGauge(new UsedMemoryGauge());
            this.addGauge(new UsedNonHeapMemoryGauge());
            this.addGauge(new ActiveThreadGauge());
        }

    }

    private static NodeStatusDataStore findStatusDataStore() {
        NodeStatusDataStore status = null;

        try {
            status = (NodeStatusDataStore)IoCs.findOrCreateInstance(NodeStatusDataStore.class);
        } catch (SironaException var2) {
            ;
        }

        if(status == null) {
            status = ((DataStoreFactory)IoCs.findOrCreateInstance(DataStoreFactory.class)).getNodeStatusDataStore();
        }

        return status;
    }

    private static CommonGaugeDataStore findGaugeDataStore() {
        CommonGaugeDataStore gauge = null;

        try {
            gauge = (CommonGaugeDataStore)IoCs.findOrCreateInstance(GaugeDataStore.class);
        } catch (SironaException var3) {
            ;
        }

        if(gauge == null) {
            try {
                gauge = (CommonGaugeDataStore)IoCs.findOrCreateInstance(CollectorGaugeDataStore.class);
            } catch (SironaException var2) {
                ;
            }
        }

        if(gauge == null) {
            gauge = ((DataStoreFactory)IoCs.findOrCreateInstance(DataStoreFactory.class)).getGaugeDataStore();
        }

        return gauge;
    }

    private static PathTrackingDataStore findPathTrackingDataStore() {
        PathTrackingDataStore pathTrackingDataStore = null;

        try {
            pathTrackingDataStore = (PathTrackingDataStore)IoCs.findOrCreateInstance(PathTrackingDataStore.class);
        } catch (SironaException var2) {
            ;
        }

        if(pathTrackingDataStore == null) {
            pathTrackingDataStore = ((DataStoreFactory)IoCs.findOrCreateInstance(DataStoreFactory.class)).getPathTrackingDataStore();
        }

        return pathTrackingDataStore;
    }

    private static CounterDataStore findCounterDataStore() {
        CounterDataStore counter = null;

        try {
            counter = (CounterDataStore)IoCs.findOrCreateInstance(CounterDataStore.class);
        } catch (SironaException var3) {
            ;
        }

        if(counter == null) {
            try {
                counter = (CounterDataStore)IoCs.findOrCreateInstance(CollectorCounterStore.class);
            } catch (SironaException var2) {
                ;
            }
        }

        if(counter == null) {
            counter = ((DataStoreFactory)IoCs.findOrCreateInstance(DataStoreFactory.class)).getCounterDataStore();
        }

        return counter;
    }

    public Counter getCounter(Key key) {
        return this.counterDataStore.getOrCreateCounter(key);
    }

    public Collection<Counter> counters() {
        return this.counterDataStore.getCounters();
    }

    public void clearCounters() {
        this.counterDataStore.clearCounters();
    }

    public void reset() {
        this.clearCounters();
        this.nodeStatusDataStore.reset();
        Iterator i$ = this.gauges().iterator();

        while(i$.hasNext()) {
            Role g = (Role)i$.next();
            this.gaugeDataStore.gaugeStopped(g);
        }

    }

    public StopWatch start(Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    public SortedMap<Long, Double> getGaugeValues(long start, long end, Role role) {
        return this.gaugeDataStore.getGaugeValues(new GaugeValuesRequest(start, end, role));
    }

    public Collection<Role> gauges() {
        return this.gaugeDataStore.gauges();
    }

    public Role findGaugeRole(String name) {
        return this.gaugeDataStore.findGaugeRole(name);
    }

    public void addGauge(Gauge gauge) {
        if(GaugeDataStore.class.isInstance(this.gaugeDataStore)) {
            ((GaugeDataStore)GaugeDataStore.class.cast(this.gaugeDataStore)).createOrNoopGauge(gauge.role());
        }

        if(GaugeAware.class.isInstance(this.gaugeDataStore)) {
            ((GaugeAware)GaugeAware.class.cast(this.gaugeDataStore)).addGauge(gauge);
        }

    }

    public void stopGauge(Gauge gauge) {
        if(GaugeDataStore.class.isInstance(this.gaugeDataStore)) {
            ((GaugeDataStore)GaugeDataStore.class.cast(this.gaugeDataStore)).gaugeStopped(gauge.role());
        }

    }

    public Map<String, NodeStatus> statuses() {
        return this.nodeStatusDataStore.statuses();
    }
}