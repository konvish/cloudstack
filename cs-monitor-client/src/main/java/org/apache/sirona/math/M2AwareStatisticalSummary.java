package org.apache.sirona.math;

import java.io.Serializable;
import java.util.Map;
/**
 * Created by kong on 2016/1/24.
 */
public class M2AwareStatisticalSummary implements Serializable {
    private final double mean;
    private final double variance;
    private final long n;
    private final double max;
    private final double min;
    private final double sum;
    private final double m2;

    public M2AwareStatisticalSummary(double mean, double variance, long n, double max, double min, double sum, double m2) {
        this.mean = mean;
        this.variance = variance;
        this.n = n;
        this.max = max;
        this.min = min;
        this.sum = sum;
        this.m2 = m2;
    }

    public M2AwareStatisticalSummary(Map<String, Object> data) {
        this(toDouble(data.get("mean")), toDouble(data.get("variance")), toLong(data.get("hits")), toDouble(data.get("max")), toDouble(data.get("min")), toDouble(data.get("sum")), toDouble(data.get("m2")));
    }

    private static double toDouble(Object mean) {
        if(Number.class.isInstance(mean)) {
            return ((Number)Number.class.cast(mean)).doubleValue();
        } else if(String.class.isInstance(mean)) {
            return Double.parseDouble((String)String.class.cast(mean));
        } else {
            throw new IllegalArgumentException(mean + " not supported");
        }
    }

    private static long toLong(Object mean) {
        if(Number.class.isInstance(mean)) {
            return ((Number)Number.class.cast(mean)).longValue();
        } else if(String.class.isInstance(mean)) {
            return Long.parseLong((String)String.class.cast(mean));
        } else {
            throw new IllegalArgumentException(mean + " not supported");
        }
    }

    public double getSecondMoment() {
        return this.m2;
    }

    public double getMean() {
        return this.mean;
    }

    public double getVariance() {
        return this.variance;
    }

    public long getN() {
        return this.n;
    }

    public double getMax() {
        return this.max;
    }

    public double getMin() {
        return this.min;
    }

    public double getSum() {
        return this.sum;
    }
}