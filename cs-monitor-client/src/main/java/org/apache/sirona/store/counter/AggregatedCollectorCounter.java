package org.apache.sirona.store.counter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import org.apache.sirona.counters.AggregatedCounter;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.math.Aggregators;
import org.apache.sirona.store.counter.CollectorCounter;
import org.apache.sirona.store.counter.LeafCollectorCounter;
/**
 * Created by kong on 2016/1/24.
 */
public class AggregatedCollectorCounter extends CollectorCounter implements AggregatedCounter {
    private final ConcurrentMap<String, LeafCollectorCounter> aggregation = new ConcurrentHashMap(50);

    public AggregatedCollectorCounter(Key key) {
        super(key);
    }

    public AggregatedCollectorCounter(Key key, Map<String, LeafCollectorCounter> counters) {
        super(key);
        this.aggregation.putAll(counters);
        this.update();
    }

    public void update() {
        Lock workLock = this.lock.writeLock();
        workLock.lock();

        try {
            Collection counters = this.aggregation.values();
            this.statistics = Aggregators.aggregate(counters);
            this.concurrency.set(computeConcurrency(counters));
            this.updateConcurrency(this.concurrency.get());
        } finally {
            workLock.unlock();
        }

    }

    public void addIfMissing(String marker, LeafCollectorCounter counter) {
        this.aggregation.putIfAbsent(marker, counter);
    }

    private static int computeConcurrency(Collection<LeafCollectorCounter> counters) {
        int i = 0;

        LeafCollectorCounter counter;
        for(Iterator i$ = counters.iterator(); i$.hasNext(); i += counter.currentConcurrency().get()) {
            counter = (LeafCollectorCounter)i$.next();
        }

        return i;
    }

    public Map<String, ? extends Counter> aggregated() {
        return this.aggregation;
    }
}