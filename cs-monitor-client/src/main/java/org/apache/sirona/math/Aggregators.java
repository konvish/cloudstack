package org.apache.sirona.math;

import java.util.Collection;
import java.util.Iterator;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.LeafCollectorCounter;
/**
 * Created by kong on 2016/1/24.
 */
public class Aggregators {
    public static M2AwareStatisticalSummary aggregate(Collection<LeafCollectorCounter> statistics) {
        if(statistics == null) {
            return null;
        } else {
            Iterator iterator = statistics.iterator();
            if(!iterator.hasNext()) {
                return null;
            } else {
                LeafCollectorCounter current = (LeafCollectorCounter)iterator.next();
                long n = current.getHits();
                double min = current.getMin();
                double sum = current.getSum();
                double max = current.getMax();
                double m2 = current.getSecondMoment();

                double curN;
                double meanDiff;
                double mean;
                double variance;
                for(mean = current.getMean(); iterator.hasNext(); m2 = m2 + current.getSecondMoment() + meanDiff * meanDiff * variance * curN / (double)n) {
                    current = (LeafCollectorCounter)iterator.next();
                    if(current.getMin() < min || Double.isNaN(min)) {
                        min = current.getMin();
                    }

                    if(current.getMax() > max || Double.isNaN(max)) {
                        max = current.getMax();
                    }

                    sum += current.getSum();
                    variance = (double)n;
                    curN = (double)current.getHits();
                    n = (long)((double)n + curN);
                    meanDiff = current.getMean() - mean;
                    mean = sum / (double)n;
                }

                if(n == 0L) {
                    variance = 0.0D / 0.0;
                } else if(n == 1L) {
                    variance = 0.0D;
                } else {
                    variance = m2 / (double)(n - 1L);
                }

                return new M2AwareStatisticalSummary(mean, variance, n, max, min, sum, m2);
            }
        }
    }

    private Aggregators() {
    }
}