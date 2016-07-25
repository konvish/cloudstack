package org.apache.sirona.store.counter;

import java.util.concurrent.locks.Lock;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.CollectorCounter;
/**
 * Created by kong on 2016/1/24.
 */
public class LeafCollectorCounter extends CollectorCounter {
    public LeafCollectorCounter(Key key) {
        super(key);
    }

    public void update(M2AwareStatisticalSummary newStats, int newConcurrency) {
        Lock workLock = this.lock.writeLock();
        workLock.lock();

        try {
            this.concurrency.set(newConcurrency);
            this.updateConcurrency(newConcurrency);
            this.statistics = newStats;
        } finally {
            workLock.unlock();
        }

    }
}