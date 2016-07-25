package org.apache.sirona.store.counter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.AggregatedCollectorCounter;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.counter.InMemoryCounterDataStore;
import org.apache.sirona.store.counter.LeafCollectorCounter;
/**
 * Created by kong on 2016/1/24.
 */
public class InMemoryCollectorCounterStore extends InMemoryCounterDataStore implements CollectorCounterStore {
    private final ConcurrentMap<String, ConcurrentMap<Key, LeafCollectorCounter>> countersByMarker = new ConcurrentHashMap();

    public InMemoryCollectorCounterStore() {
    }

    public void update(Key key, String marker, M2AwareStatisticalSummary stats, int concurrency) {
        this.getOrCreateCounter(key, marker).update(stats, concurrency);
        this.getOrCreateCounter(key).update();
    }

    public Collection<String> markers() {
        return this.countersByMarker.keySet();
    }

    public Collection<? extends LeafCollectorCounter> getCounters(String marker) {
        return ((ConcurrentMap)this.countersByMarker.get(marker)).values();
    }

    public LeafCollectorCounter getOrCreateCounter(Key key, String marker) {
        Object subCounters = (ConcurrentMap)this.countersByMarker.get(marker);
        if(subCounters == null) {
            ConcurrentHashMap counter = new ConcurrentHashMap(50);
            ConcurrentMap previous = (ConcurrentMap)this.countersByMarker.putIfAbsent(marker, counter);
            if(previous != null) {
                subCounters = previous;
            } else {
                subCounters = counter;
            }
        }

        LeafCollectorCounter counter1 = (LeafCollectorCounter)((ConcurrentMap)subCounters).get(key);
        if(counter1 == null) {
            counter1 = new LeafCollectorCounter(key);
            LeafCollectorCounter previous1 = (LeafCollectorCounter)((ConcurrentMap)subCounters).putIfAbsent(key, counter1);
            if(previous1 != null) {
                counter1 = previous1;
            }

            AggregatedCollectorCounter aggregate = (AggregatedCollectorCounter)AggregatedCollectorCounter.class.cast(super.getOrCreateCounter(key));
            aggregate.addIfMissing(marker, counter1);
        }

        return counter1;
    }

    protected Counter newCounter(Key key) {
        return new AggregatedCollectorCounter(key);
    }

    public AggregatedCollectorCounter getOrCreateCounter(Key key) {
        return (AggregatedCollectorCounter)AggregatedCollectorCounter.class.cast(super.getOrCreateCounter(key));
    }

    public void clearCounters() {
        Iterator i$ = this.countersByMarker.entrySet().iterator();

        while(i$.hasNext()) {
            Entry maps = (Entry)i$.next();
            ((ConcurrentMap)maps.getValue()).clear();
        }

        this.countersByMarker.clear();
        super.clearCounters();
    }

    public void addToCounter(Counter defaultCounter, double delta) {
        throw new UnsupportedOperationException("shouldn\'t be used");
    }
}
