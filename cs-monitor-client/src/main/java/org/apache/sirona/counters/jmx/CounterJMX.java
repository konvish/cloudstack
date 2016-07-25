package org.apache.sirona.counters.jmx;

import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.jmx.CounterJMXMBean;
/**
 * Created by kong on 2016/1/24.
 */
public class CounterJMX implements CounterJMXMBean {
    private final Counter delegate;

    public CounterJMX(Counter counter) {
        this.delegate = counter;
    }

    public double getMax() {
        return this.delegate.getMax();
    }

    public double getMin() {
        return this.delegate.getMin();
    }

    public long getHits() {
        return this.delegate.getHits();
    }

    public double getSum() {
        return this.delegate.getSum();
    }

    public double getStandardDeviation() {
        return this.delegate.getStandardDeviation();
    }

    public double getMean() {
        return this.delegate.getMean();
    }

    public String getRole() {
        return this.delegate.getKey().getRole().getName();
    }

    public String getName() {
        return this.delegate.getKey().getName();
    }
}
