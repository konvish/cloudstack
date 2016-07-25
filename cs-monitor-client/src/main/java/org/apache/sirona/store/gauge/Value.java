package org.apache.sirona.store.gauge;

/**
 * Created by kong on 2016/1/24.
 */
public interface Value {
    double getMean();

    double getMax();

    double getMin();

    long getN();

    double getSum();
}
