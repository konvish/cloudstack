package org.apache.sirona.counters.jmx;

/**
 * Created by kong on 2016/1/24.
 */
public interface CounterJMXMBean {
    double getMax();

    double getMin();

    long getHits();

    double getSum();

    double getStandardDeviation();

    double getMean();

    String getRole();

    String getName();
}