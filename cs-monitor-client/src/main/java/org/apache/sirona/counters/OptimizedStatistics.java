package org.apache.sirona.counters;

/**
 * Created by kong on 2016/1/24.
 */
public class OptimizedStatistics {
    private long n = 0L;
    private double sum = 0.0D;
    private double min = 0.0D / 0.0;
    private double max = 0.0D / 0.0;
    protected double m1 = 0.0D / 0.0;
    protected double m2 = 0.0D / 0.0;

    public OptimizedStatistics() {
    }

    public OptimizedStatistics(long n, double sum, double min, double max, double m1, double m2) {
        this.n = n;
        this.sum = sum;
        this.min = min;
        this.max = max;
        this.m1 = m1;
        this.m2 = m2;
    }

    public OptimizedStatistics addValue(double value) {
        if(this.n == 0L) {
            this.m1 = 0.0D;
            this.m2 = 0.0D;
        }

        ++this.n;
        this.sum += value;
        if(value < this.min || Double.isNaN(this.min)) {
            this.min = value;
        }

        if(value > this.max || Double.isNaN(this.max)) {
            this.max = value;
        }

        double dev = value - this.m1;
        double nDev = dev / (double)this.n;
        this.m1 += nDev;
        this.m2 += dev * nDev * (double)(this.n - 1L);
        return this;
    }

    public void clear() {
        this.n = 0L;
        this.sum = 0.0D;
        this.min = 0.0D / 0.0;
        this.max = 0.0D / 0.0;
        this.m1 = 0.0D / 0.0;
        this.m2 = 0.0D / 0.0;
    }

    public double getMean() {
        return this.m1;
    }

    public double getVariance() {
        return this.n == 0L?0.0D / 0.0:(this.n == 1L?0.0D:this.m2 / (double)(this.n - 1L));
    }

    public double getStandardDeviation() {
        return this.n > 1L?Math.sqrt(this.getVariance()):(this.n == 1L?0.0D:0.0D / 0.0);
    }

    public double getMax() {
        return this.max;
    }

    public double getMin() {
        return this.min;
    }

    public long getN() {
        return this.n;
    }

    public double getSum() {
        return this.sum;
    }

    public double getSecondMoment() {
        return this.m2;
    }

    public OptimizedStatistics copy() {
        return new OptimizedStatistics(this.n, this.sum, this.min, this.max, this.m1, this.m2);
    }

    public String toString() {
        return "OptimizedStatistics{n=" + this.n + ", sum=" + this.sum + ", min=" + this.min + ", max=" + this.max + ", m1=" + this.m1 + ", m2=" + this.m2 + '}';
    }
}
