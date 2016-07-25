package org.apache.sirona.store.gauge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeDataStoreAdapter;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.util.DaemonThreadFactory;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class BatchGaugeDataStoreAdapter extends GaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(BatchGaugeDataStoreAdapter.class.getName());
    protected BatchFuture scheduledTask;

    public BatchGaugeDataStoreAdapter() {
    }

    @Created
    public void initBatch() {
        String name = this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("gaugedatastore", "");
        long period = (long)this.getPeriod(name);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-gauge-schedule-"));
        ScheduledFuture future = ses.scheduleAtFixedRate(new BatchGaugeDataStoreAdapter.PushGaugesTask(), period, period, TimeUnit.MILLISECONDS);
        this.scheduledTask = new BatchFuture(ses, future);
    }

    protected int getPeriod(String name) {
        return Configuration.getInteger("org.apache.org.apache.sirona." + name + ".gauge.period", Configuration.getInteger("org.apache.org.apache.sirona." + name + ".period", '\uea60'));
    }

    @Destroying
    public void shutdown() {
        this.scheduledTask.done();
    }

    protected abstract void pushGauges(Map<Role, BatchGaugeDataStoreAdapter.Measure> var1);

    protected Map<Role, BatchGaugeDataStoreAdapter.Measure> snapshot() {
        long ts = System.currentTimeMillis();
        HashMap snapshot = new HashMap();
        Iterator i$ = this.gauges.values().iterator();

        while(i$.hasNext()) {
            Gauge gauge = (Gauge)i$.next();
            Role role = gauge.role();
            double value = gauge.value();
            this.addToGauge(role, ts, value);
            snapshot.put(role, new BatchGaugeDataStoreAdapter.Measure(ts, value));
        }

        return snapshot;
    }

    public static class Measure {
        private long time;
        private double value;

        private Measure(long time, double value) {
            this.time = time;
            this.value = value;
        }

        public long getTime() {
            return this.time;
        }

        public double getValue() {
            return this.value;
        }

        public String toString() {
            return "Measure{time=" + this.time + ", value=" + this.value + '}';
        }
    }

    private class PushGaugesTask implements Runnable {
        private PushGaugesTask() {
        }

        public void run() {
            try {
                BatchGaugeDataStoreAdapter.this.pushGauges(BatchGaugeDataStoreAdapter.this.snapshot());
            } catch (Exception var2) {
                BatchGaugeDataStoreAdapter.LOGGER.log(Level.SEVERE, var2.getMessage(), var2);
            }

        }
    }
}