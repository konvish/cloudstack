package org.apache.sirona.store.counter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.math.M2AwareStatisticalSummary;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class CollectorCounter implements Counter, Serializable {
    protected final Key key;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected volatile int maxConcurrency = 0;
    protected volatile AtomicInteger concurrency = new AtomicInteger(0);
    protected M2AwareStatisticalSummary statistics;

    public CollectorCounter(Key key) {
        this.key = key;
        this.reset();
    }

    public Key getKey() {
        return this.key;
    }

    public void reset() {
        Lock workLock = this.lock.writeLock();
        workLock.lock();

        try {
            this.statistics = new M2AwareStatisticalSummary(0.0D / 0.0, 0.0D / 0.0, 0L, 0.0D / 0.0, 0.0D / 0.0, 0.0D / 0.0, 0.0D / 0.0);
        } finally {
            workLock.unlock();
        }

    }

    public void add(double delta) {
    }

    public void add(double delta, Unit unit) {
        this.add(this.key.getRole().getUnit().convert(delta, unit));
    }

    public AtomicInteger currentConcurrency() {
        return this.concurrency;
    }

    public void updateConcurrency(int concurrency) {
        if(concurrency > this.maxConcurrency) {
            Lock workLock = this.lock.writeLock();
            workLock.lock();

            try {
                this.maxConcurrency = concurrency;
            } finally {
                workLock.unlock();
            }
        }

    }

    public int getMaxConcurrency() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        int var2;
        try {
            var2 = this.maxConcurrency;
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getMax() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getMax();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getMin() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getMin();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public long getHits() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        long var2;
        try {
            var2 = this.statistics.getN();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getSum() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getSum();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getStandardDeviation() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = Math.sqrt(this.statistics.getVariance());
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getVariance() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getVariance();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getMean() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getMean();
        } finally {
            workLock.unlock();
        }

        return var2;
    }

    public double getSecondMoment() {
        Lock workLock = this.lock.readLock();
        workLock.lock();

        double var2;
        try {
            var2 = this.statistics.getSecondMoment();
        } finally {
            workLock.unlock();
        }

        return var2;
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