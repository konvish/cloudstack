package org.apache.sirona.store.counter;

import java.util.Collection;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.CounterDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public interface CollectorCounterStore extends CounterDataStore {
    void update(Key var1, String var2, M2AwareStatisticalSummary var3, int var4);

    Collection<String> markers();

    Collection<? extends Counter> getCounters(String var1);

    Counter getOrCreateCounter(Key var1, String var2);
}