package org.apache.sirona.store.counter;

import java.util.Collection;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
/**
 * Created by kong on 2016/1/24.
 */
public interface CounterDataStore {
    Counter getOrCreateCounter(Key var1);

    void clearCounters();

    Collection<Counter> getCounters();

    void addToCounter(Counter var1, double var2);
}
