package org.apache.sirona.store.gauge;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;
import org.apache.sirona.store.gauge.Value;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter.Measure;
import org.apache.sirona.util.DaemonThreadFactory;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class AggregatedGaugeDataStoreAdapter extends BatchGaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(AggregatedGaugeDataStoreAdapter.class.getName());
    private final ConcurrentMap<Role, OptimizedStatistics> gauges = new ConcurrentHashMap();
    private BatchFuture scheduledAggregatedTask;

    public AggregatedGaugeDataStoreAdapter() {
    }

    protected abstract void pushAggregatedGauges(Map<Role, Value> var1);

    @Created
    public void initAggregated() {
        String name = this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("gaugedatastore", "") + ".aggregated";
        long period = (long)this.getPeriod(name);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-aggregated-gauge-schedule-"));
        ScheduledFuture future = ses.scheduleAtFixedRate(new AggregatedGaugeDataStoreAdapter.PushGaugesTask(), period, period, TimeUnit.MILLISECONDS);
        this.scheduledAggregatedTask = new BatchFuture(ses, future);
    }

    @Destroying
    public void shutdown() {
        this.scheduledAggregatedTask.done();
    }

    protected void pushGauges(Map<Role, Measure> gauges) {
    }

    public void gaugeStopped(Role gauge) {
        this.gauges.remove(gauge);
        super.gaugeStopped(gauge);
    }

    public void addToGauge(Role role, long time, double value) {
        OptimizedStatistics stat = (OptimizedStatistics)this.gauges.get(role);
        if(stat == null) {
            stat = new OptimizedStatistics();
            OptimizedStatistics existing = (OptimizedStatistics)this.gauges.putIfAbsent(role, stat);
            if(existing != null) {
                stat = existing;
            }
        }

        stat.addValue(value);
    }

    private ConcurrentMap<Role, Value> copyAndClearGauges() {
        ConcurrentHashMap copy = new ConcurrentHashMap();
        copy.putAll(this.gauges);
        this.gauges.clear();
        ConcurrentHashMap toPush = new ConcurrentHashMap();
        Iterator i$ = copy.entrySet().iterator();

        while(i$.hasNext()) {
            Entry entry = (Entry)i$.next();
            toPush.put(entry.getKey(), new AggregatedGaugeDataStoreAdapter.ValueImpl((OptimizedStatistics)entry.getValue()));
        }

        return toPush;
    }

    private static class ValueImpl implements Value {
        private final OptimizedStatistics delegate;

        public ValueImpl(OptimizedStatistics value) {
            this.delegate = value;
        }

        public double getMean() {
            return this.delegate.getMean();
        }

        public double getMax() {
            return this.delegate.getMax();
        }

        public double getMin() {
            return this.delegate.getMin();
        }

        public long getN() {
            return this.delegate.getN();
        }

        public double getSum() {
            return this.delegate.getSum();
        }

        public String toString() {
            return "ValueImpl{delegate=" + this.delegate + '}';
        }
    }

    private class PushGaugesTask implements Runnable {
        private PushGaugesTask() {
        }

        public void run() {
            try {
                AggregatedGaugeDataStoreAdapter.this.pushAggregatedGauges(AggregatedGaugeDataStoreAdapter.this.copyAndClearGauges());
            } catch (Exception var2) {
                AggregatedGaugeDataStoreAdapter.LOGGER.log(Level.SEVERE, var2.getMessage(), var2);
            }

        }
    }
}