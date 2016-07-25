package org.apache.sirona.counters;

import org.apache.sirona.counters.Counter;
/**
 * Created by kong on 2016/1/24.
 */
public enum MetricData {
    Hits {
        public double value(Counter counter) {
            return (double)counter.getHits();
        }

        public boolean isTime() {
            return false;
        }
    },
    Max {
        public double value(Counter counter) {
            return counter.getMax();
        }

        public boolean isTime() {
            return true;
        }
    },
    Mean {
        public double value(Counter counter) {
            return counter.getMean();
        }

        public boolean isTime() {
            return true;
        }
    },
    Min {
        public double value(Counter counter) {
            return counter.getMin();
        }

        public boolean isTime() {
            return true;
        }
    },
    StandardDeviation {
        public double value(Counter counter) {
            return counter.getStandardDeviation();
        }

        public boolean isTime() {
            return false;
        }
    },
    Sum {
        public double value(Counter counter) {
            return counter.getSum();
        }

        public boolean isTime() {
            return true;
        }
    },
    Variance {
        public double value(Counter counter) {
            return counter.getVariance();
        }

        public boolean isTime() {
            return false;
        }
    },
    Value {
        public double value(Counter counter) {
            return counter.getSum();
        }

        public boolean isTime() {
            return true;
        }
    },
    Concurrency {
        public double value(Counter counter) {
            return (double)counter.currentConcurrency().get();
        }

        public boolean isTime() {
            return false;
        }
    },
    MaxConcurrency {
        public double value(Counter counter) {
            return (double)counter.getMaxConcurrency();
        }

        public boolean isTime() {
            return false;
        }
    };

    private MetricData() {
    }

    public abstract double value(Counter var1);

    public abstract boolean isTime();
}