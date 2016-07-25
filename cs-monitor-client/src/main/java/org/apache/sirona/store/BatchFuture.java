package org.apache.sirona.store;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
/**
 * Created by kong on 2016/1/24.
 */
public class BatchFuture {
    private final ScheduledExecutorService executor;
    private final ScheduledFuture<?> task;

    public BatchFuture(ScheduledExecutorService ses, ScheduledFuture<?> future) {
        this.executor = ses;
        this.task = future;
    }

    public void done() {
        try {
            this.executor.shutdown();
            this.task.cancel(false);
            this.executor.awaitTermination(1L, TimeUnit.MINUTES);
            if(!this.task.isDone()) {
                this.task.cancel(true);
            }
        } catch (InterruptedException var2) {
            ;
        }

    }
}