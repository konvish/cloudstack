package org.apache.sirona.store.status;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.NodeStatusReporter;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.util.DaemonThreadFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class PeriodicNodeStatusDataStore implements NodeStatusDataStore {
    private static final Logger LOGGER = Logger.getLogger(PeriodicNodeStatusDataStore.class.getName());
    private final AtomicReference<BatchFuture> scheduledTask = new AtomicReference();
    protected final AtomicReference<NodeStatus> status = new AtomicReference();
    protected final HashMap<String, NodeStatus> statusAsMap = new HashMap();
    protected final NodeStatusReporter nodeStatusReporter = this.newNodeStatusReporter();

    public PeriodicNodeStatusDataStore() {
    }

    @Created
    public void run() {
        this.reload();
    }

    protected NodeStatusReporter newNodeStatusReporter() {
        return new NodeStatusReporter();
    }

    @Destroying
    public void shutdown() {
        BatchFuture task = (BatchFuture)this.scheduledTask.get();
        if(task != null) {
            task.done();
            this.scheduledTask.set((Object)null);
        }

        this.status.set((Object)null);
    }

    public synchronized void reset() {
        this.shutdown();
        this.reload();
    }

    private void reload() {
        String name = this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("nodestatusdatastore", "");
        long period = (long)this.getPeriod(name);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-status-schedule-"));
        ScheduledFuture future = ses.scheduleAtFixedRate(new PeriodicNodeStatusDataStore.ReportStatusTask(this.nodeStatusReporter), period, period, TimeUnit.MILLISECONDS);
        this.scheduledTask.set(new BatchFuture(ses, future));
    }

    protected int getPeriod(String name) {
        return Configuration.getInteger("org.apache.org.apache.sirona." + name + ".status.period", Configuration.getInteger("org.apache.org.apache.sirona." + name + ".period", '\uea60'));
    }

    protected void reportStatus(NodeStatus nodeStatus) {
    }

    public Map<String, NodeStatus> statuses() {
        if(this.status.get() != null) {
            this.statusAsMap.put("local", this.status.get());
        } else {
            this.statusAsMap.clear();
        }

        return this.statusAsMap;
    }

    private class ReportStatusTask implements Runnable {
        private final NodeStatusReporter reporter;

        public ReportStatusTask(NodeStatusReporter nodeStatusReporter) {
            this.reporter = nodeStatusReporter;
        }

        public void run() {
            NodeStatus nodeStatus = this.reporter.computeStatus();

            try {
                PeriodicNodeStatusDataStore.this.status.set(nodeStatus);
                PeriodicNodeStatusDataStore.this.reportStatus(nodeStatus);
            } catch (Exception var3) {
                PeriodicNodeStatusDataStore.LOGGER.log(Level.SEVERE, var3.getMessage(), var3);
            }

        }
    }
}