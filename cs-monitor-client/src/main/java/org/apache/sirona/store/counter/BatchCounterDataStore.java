package org.apache.sirona.store.counter;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.counter.InMemoryCounterDataStore;
import org.apache.sirona.util.DaemonThreadFactory;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class BatchCounterDataStore extends InMemoryCounterDataStore {
    private static final Logger LOGGER = Logger.getLogger(BatchCounterDataStore.class.getName());
    protected final BatchFuture scheduledTask;
    protected final boolean clearAfterCollect;

    protected BatchCounterDataStore() {
        String name = this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("counterdatastore", "");
        String prefix = "org.apache.org.apache.sirona." + name;
        long period = (long)this.getPeriod(prefix);
        this.clearAfterCollect = this.isClearAfterCollect(prefix);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-counter-schedule-"));
        ScheduledFuture future = ses.scheduleAtFixedRate(new BatchCounterDataStore.BatchPushCountersTask(), period, period, TimeUnit.MILLISECONDS);
        this.scheduledTask = new BatchFuture(ses, future);
    }

    protected boolean isClearAfterCollect(String prefix) {
        return Configuration.is(prefix + ".counter.clearOnCollect", false);
    }

    protected int getPeriod(String prefix) {
        return Configuration.getInteger(prefix + ".counter.period", Configuration.getInteger(prefix + ".period", '\uea60'));
    }

    @Destroying
    public void shutdown() {
        this.scheduledTask.done();
    }

    protected void clearCountersIfNeeded(Repository instance) {
        if(this.clearAfterCollect) {
            instance.clearCounters();
        }

    }

    protected abstract void pushCountersByBatch(Collection<Counter> var1);

    private class BatchPushCountersTask implements Runnable {
        private BatchPushCountersTask() {
        }

        public void run() {
            try {
                Repository e = Repository.INSTANCE;
                BatchCounterDataStore.this.pushCountersByBatch(e.counters());
                BatchCounterDataStore.this.clearCountersIfNeeded(e);
            } catch (Exception var2) {
                BatchCounterDataStore.LOGGER.log(Level.SEVERE, var2.getMessage(), var2);
            }

        }
    }
}