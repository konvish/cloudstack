package org.apache.sirona.store.tracking;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.store.tracking.InMemoryPathTrackingDataStore.Pointer;
import org.apache.sirona.util.DaemonThreadFactory;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class BatchPathTrackingDataStore extends InMemoryPathTrackingDataStore {
    private static final Logger LOGGER = Logger.getLogger(BatchPathTrackingDataStore.class.getName());
    protected BatchFuture scheduledTask;

    public BatchPathTrackingDataStore() {
    }

    @Created
    public void initBatch() {
        String name = this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("pathtrackingdatastore", "");
        long period = (long)this.getPeriod(name);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-pathtracking-schedule-"));
        ScheduledFuture future = ses.scheduleAtFixedRate(new BatchPathTrackingDataStore.PushPathTrackingTask(), period, period, TimeUnit.MILLISECONDS);
        this.scheduledTask = new BatchFuture(ses, future);
    }

    protected int getPeriod(String name) {
        int period = Configuration.getInteger("org.apache.org.apache.sirona." + name + ".pathtracking.period", Configuration.getInteger("org.apache.org.apache.sirona." + name + ".period", '\uea60'));
        return period;
    }

    protected abstract void pushEntriesByBatch(Map<String, List<Pointer>> var1);

    private class PushPathTrackingTask implements Runnable {
        private PushPathTrackingTask() {
        }

        public void run() {
            try {
                BatchPathTrackingDataStore.this.pushEntriesByBatch(BatchPathTrackingDataStore.this.getPointers());
                BatchPathTrackingDataStore.this.clearEntries();
            } catch (Exception var2) {
                BatchPathTrackingDataStore.LOGGER.log(Level.SEVERE, var2.getMessage(), var2);
            }

        }
    }
}
