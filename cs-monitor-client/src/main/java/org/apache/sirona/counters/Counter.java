package org.apache.sirona.counters;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
/**
 * Created by kong on 2016/1/24.
 */
public interface Counter {
    Counter.Key getKey();

    void reset();

    void add(double var1);

    void add(double var1, Unit var3);

    AtomicInteger currentConcurrency();

    void updateConcurrency(int var1);

    int getMaxConcurrency();

    double getMax();

    double getMin();

    long getHits();

    double getSum();

    double getStandardDeviation();

    double getVariance();

    double getMean();

    double getSecondMoment();

    public static class Key implements Serializable {
        private final String name;
        private final Role role;
        private int hash = -2147483648;

        public Key(Role role, String name) {
            this.role = role;
            this.name = name;
        }

        public String toString() {
            return "name=" + this.name;
        }

        public boolean equals(Object o) {
            if(this == o) {
                return true;
            } else if(o != null && this.getClass() == o.getClass()) {
                Counter.Key key = (Counter.Key)o;
                return this.name.equals(key.name) && this.role.equals(key.role);
            } else {
                return false;
            }
        }

        public int hashCode() {
            if(this.hash == -2147483648) {
                this.hash = this.name.hashCode();
                this.hash = 31 * this.hash + this.role.hashCode();
            }

            return this.hash;
        }

        public String getName() {
            return this.name;
        }

        public Role getRole() {
            return this.role;
        }
    }
}