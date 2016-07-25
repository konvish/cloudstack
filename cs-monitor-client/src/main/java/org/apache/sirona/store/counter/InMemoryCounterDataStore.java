package org.apache.sirona.store.counter;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.DefaultCounter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.counters.jmx.CounterJMX;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.counter.CounterGauge;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CounterDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class InMemoryCounterDataStore implements CounterDataStore {
    protected final boolean gauged = Configuration.is("org.apache.org.apache.sirona.counter.with-gauge", false);
    protected final boolean jmx = Configuration.is("org.apache.org.apache.sirona.counter.with-jmx", false);
    protected final ConcurrentMap<Key, Counter> counters = this.newCounterMap();
    protected final ConcurrentMap<Key, Collection<Gauge>> gauges = new ConcurrentHashMap();
    protected final ReadWriteLock stateLock = new ReentrantReadWriteLock();

    public InMemoryCounterDataStore() {
    }

    protected ConcurrentMap<Key, Counter> newCounterMap() {
        return new ConcurrentHashMap(50);
    }

    protected Counter newCounter(Key key) {
        return new DefaultCounter(key, this);
    }

    public Counter getOrCreateCounter(Key key) {
        Counter counter = (Counter)this.counters.get(key);
        if(counter == null) {
            Lock lock = this.stateLock.readLock();
            lock.lock();

            try {
                counter = this.newCounter(key);
                Counter previous = (Counter)this.counters.putIfAbsent(key, counter);
                if(previous != null) {
                    counter = previous;
                } else {
                    if(this.gauged) {
                        InMemoryCounterDataStore.Values server = new InMemoryCounterDataStore.Values(counter);
                        ArrayList e = new ArrayList(3);
                        e.add(new InMemoryCounterDataStore.SyncCounterGauge(counter, MetricData.Sum, server));
                        e.add(new InMemoryCounterDataStore.SyncCounterGauge(counter, MetricData.Max, server));
                        e.add(new InMemoryCounterDataStore.SyncCounterGauge(counter, MetricData.Hits, server));
                        Iterator i$ = e.iterator();

                        while(i$.hasNext()) {
                            Gauge gauge = (Gauge)i$.next();
                            Repository.INSTANCE.addGauge(gauge);
                        }

                        this.gauges.putIfAbsent(key, e);
                    }

                    if(this.jmx) {
                        MBeanServer server1 = ManagementFactory.getPlatformMBeanServer();

                        try {
                            ObjectName e1 = new ObjectName("org.apache.org.apache.sirona.counter:role=" + escapeJmx(key.getRole().getName()) + ",name=" + escapeJmx(key.getName()));
                            ((DefaultCounter)DefaultCounter.class.cast(counter)).setJmx(e1);
                            if(!server1.isRegistered(e1)) {
                                server1.registerMBean(new CounterJMX(counter), e1);
                            }
                        } catch (Exception var12) {
                            ;
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return counter;
    }

    private static String escapeJmx(String name) {
        return name.replace('=', '_').replace(',', '_');
    }

    @Destroying
    public void cleanUp() {
        this.clearCounters();
    }

    public void clearCounters() {
        Lock lock = this.stateLock.writeLock();
        lock.lock();

        try {
            Iterator i$;
            if(this.jmx) {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                i$ = this.counters.values().iterator();

                while(i$.hasNext()) {
                    Counter list = (Counter)i$.next();

                    try {
                        server.unregisterMBean(((DefaultCounter)DefaultCounter.class.cast(list)).getJmx());
                    } catch (Exception var12) {
                        ;
                    }
                }
            }

            this.counters.clear();
            ConcurrentMap server1 = this.gauges;
            synchronized(this.gauges) {
                i$ = this.gauges.values().iterator();

                while(i$.hasNext()) {
                    Collection list1 = (Collection)i$.next();
                    Iterator i$1 = list1.iterator();

                    while(i$1.hasNext()) {
                        Gauge g = (Gauge)i$1.next();
                        Repository.INSTANCE.stopGauge(g);
                    }

                    list1.clear();
                }

                this.gauges.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    public Collection<Counter> getCounters() {
        return this.counters.values();
    }

    public void addToCounter(Counter counter, double delta) {
        if(!DefaultCounter.class.isInstance(counter)) {
            throw new IllegalArgumentException(this.getClass().getName() + " only supports " + DefaultCounter.class.getName());
        } else {
            DefaultCounter defaultCounter = (DefaultCounter)DefaultCounter.class.cast(counter);
            Lock lock = defaultCounter.getLock().writeLock();
            lock.lock();

            try {
                defaultCounter.addInternal(delta);
            } finally {
                lock.unlock();
            }

        }
    }

    private static class Values {
        private double max;
        private double sum;
        private double hits;
        private int called;
        private final Counter counter;

        private Values(Counter counter) {
            this.called = -1;
            this.counter = counter;
        }

        public synchronized void take() {
            if(this.called == 3 || this.called == -1) {
                DefaultCounter defaultCounter = (DefaultCounter)DefaultCounter.class.cast(this.counter);
                Lock lock = defaultCounter.getLock().writeLock();
                lock.lock();

                try {
                    OptimizedStatistics statistics = defaultCounter.getStatistics();
                    this.max = statistics.getMax();
                    this.sum = statistics.getSum();
                    this.hits = (double)statistics.getN();
                    this.counter.reset();
                } finally {
                    lock.unlock();
                }

                this.called = 0;
            }

            ++this.called;
        }

        public double getMax() {
            return this.max;
        }

        public double getSum() {
            return this.sum;
        }

        public double getHits() {
            return this.hits;
        }
    }

    private static class SyncCounterGauge extends CounterGauge {
        private final InMemoryCounterDataStore.Values values;

        private SyncCounterGauge(Counter counter, MetricData metric, InMemoryCounterDataStore.Values values) {
            super(counter, metric);
            this.values = values;
        }

        public double value() {
            this.values.take();
            if(MetricData.Hits == this.metric) {
                return this.values.getHits();
            } else if(MetricData.Sum == this.metric) {
                return this.values.getSum();
            } else if(MetricData.Max == this.metric) {
                return this.values.getMax();
            } else {
                throw new IllegalArgumentException(this.metric.name());
            }
        }
    }
}
