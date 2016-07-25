package org.apache.sirona.counters;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.ObjectName;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.store.counter.CounterDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class DefaultCounter implements Counter {
    private final AtomicInteger concurrency;
    private final Key key;
    private final CounterDataStore dataStore;
    private volatile int maxConcurrency;
    protected final OptimizedStatistics statistics;
    protected final ReadWriteLock lock;
    private ObjectName jmx;

    public DefaultCounter(Key key, CounterDataStore store) {
        this(key, store, new OptimizedStatistics());
    }

    public DefaultCounter(Key key, CounterDataStore store, OptimizedStatistics statistics) {
        this.concurrency = new AtomicInteger(0);
        this.maxConcurrency = 0;
        this.lock = new ReentrantReadWriteLock();
        this.jmx = null;
        this.key = key;
        this.dataStore = store;
        this.statistics = statistics;
    }

    public void addInternal(double delta) {
        this.statistics.addValue(delta);
    }

    public void updateConcurrency(int concurrency) {
        if(concurrency > this.maxConcurrency) {
            this.maxConcurrency = concurrency;
        }

    }

    public int getMaxConcurrency() {
        return this.maxConcurrency;
    }

    public AtomicInteger currentConcurrency() {
        return this.concurrency;
    }

    public Key getKey() {
        return this.key;
    }

    public void reset() {
        this.statistics.clear();
        this.concurrency.set(0);
    }

    public void add(double delta) {
        this.dataStore.addToCounter(this, delta);
    }

    public void add(double delta, Unit deltaUnit) {
        this.add(this.key.getRole().getUnit().convert(delta, deltaUnit));
    }

    public double getMax() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getMax();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getMin() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getMin();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getSum() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getSum();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getStandardDeviation() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getStandardDeviation();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getVariance() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getVariance();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getMean() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getMean();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public double getSecondMoment() {
        Lock rl = this.lock.readLock();
        rl.lock();

        double var2;
        try {
            var2 = this.statistics.getSecondMoment();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public long getHits() {
        Lock rl = this.lock.readLock();
        rl.lock();

        long var2;
        try {
            var2 = this.statistics.getN();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public OptimizedStatistics getStatistics() {
        Lock rl = this.lock.readLock();
        rl.lock();

        OptimizedStatistics var2;
        try {
            var2 = this.statistics.copy();
        } finally {
            rl.unlock();
        }

        return var2;
    }

    public ReadWriteLock getLock() {
        return this.lock;
    }

    public void setJmx(ObjectName jmx) {
        this.jmx = jmx;
    }

    public ObjectName getJmx() {
        return this.jmx;
    }

    public String toString() {
        return "DefaultCounter{concurrency=" + this.concurrency + ", key=" + this.key + ", dataStore=" + this.dataStore + ", maxConcurrency=" + this.maxConcurrency + ", statistics=" + this.statistics + '}';
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!Counter.class.isInstance(o)) {
            return false;
        } else {
            Counter that = (Counter)Counter.class.cast(o);
            return this.key.equals(that.getKey());
        }
    }

    public int hashCode() {
        return this.key.hashCode();
    }
}