package org.apache.sirona.store.counter;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.DefaultCounter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.counter.InMemoryCounterDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class LimitedInMemoryCounterDataStore extends InMemoryCounterDataStore {
    private static final int MAX_SIZE = Configuration.getInteger("org.apache.org.apache.sirona.counter.max-size", 1000);
    private static final boolean ONLY_EVICT_WEB_COUNTERS = Boolean.parseBoolean(Configuration.getProperty("org.apache.org.apache.sirona.counter.evict-web-only", "true"));
    private static final double EVITION_RATIO = Double.parseDouble(Configuration.getProperty("org.apache.org.apache.sirona.counter.evition.ratio", "0.25"));

    public LimitedInMemoryCounterDataStore() {
    }

    protected ConcurrentMap<Key, Counter> newCounterMap() {
        return new LimitedInMemoryCounterDataStore.FixedSizedMap();
    }

    protected Counter newCounter(Key key) {
        return (Counter)(ONLY_EVICT_WEB_COUNTERS?(Role.WEB.equals(key.getRole())?new LimitedInMemoryCounterDataStore.DefaultCounterTimestamped(key, this):super.newCounter(key)):new LimitedInMemoryCounterDataStore.DefaultCounterTimestamped(key, this));
    }

    private static class DefaultCounterTimestamped extends DefaultCounter {
        private volatile long timestamp = System.currentTimeMillis();

        public DefaultCounterTimestamped(Key key, CounterDataStore store) {
            super(key, store);
        }

        public void add(double delta) {
            super.add(delta);
            this.timestamp = System.currentTimeMillis();
        }
    }

    protected class FixedSizedMap extends ConcurrentSkipListMap<Key, Counter> {
        protected FixedSizedMap() {
            super(new Comparator() {
                public int compare(Key o1, Key o2) {
                    int role = o1.getRole().compareTo(o2.getRole());
                    return role == 0?o1.getName().compareTo(o2.getName()):role;
                }
            });
        }

        public Counter put(Key key, Counter value) {
            if(this.size() >= LimitedInMemoryCounterDataStore.MAX_SIZE) {
                this.evict();
            }

            return (Counter)super.put(key, value);
        }

        public Counter putIfAbsent(Key key, Counter value) {
            if(this.size() >= LimitedInMemoryCounterDataStore.MAX_SIZE) {
                this.evict();
            }

            return (Counter)super.putIfAbsent(key, value);
        }

        private synchronized void evict() {
            if(this.size() >= LimitedInMemoryCounterDataStore.MAX_SIZE) {
                int size = this.size();
                int toEvict = (int)((double)size * LimitedInMemoryCounterDataStore.EVITION_RATIO);
                ArrayList entries = new ArrayList(size);
                Iterator server = this.entrySet().iterator();

                while(server.hasNext()) {
                    Entry i$ = (Entry)server.next();
                    entries.add(i$);
                    if(entries.size() >= size) {
                        break;
                    }
                }

                Collections.sort(entries, new Comparator() {
                    public int compare(Entry<Key, Counter> o1, Entry<Key, Counter> o2) {
                        boolean o1HasTimestamp = LimitedInMemoryCounterDataStore.DefaultCounterTimestamped.class.isInstance(o1);
                        boolean o2hasTimestamp = LimitedInMemoryCounterDataStore.DefaultCounterTimestamped.class.isInstance(o2);
                        if(!o1HasTimestamp && !o2hasTimestamp) {
                            return ((Key)o1.getKey()).getName().compareTo(((Key)o2.getKey()).getName());
                        } else if(o1HasTimestamp && !o2hasTimestamp) {
                            return -1;
                        } else if(!o1HasTimestamp) {
                            return 1;
                        } else {
                            long hitDiff = ((LimitedInMemoryCounterDataStore.DefaultCounterTimestamped)LimitedInMemoryCounterDataStore.DefaultCounterTimestamped.class.cast(o1.getValue())).timestamp - ((LimitedInMemoryCounterDataStore.DefaultCounterTimestamped)LimitedInMemoryCounterDataStore.DefaultCounterTimestamped.class.cast(o2.getValue())).timestamp;
                            return (int)hitDiff;
                        }
                    }
                });
                MBeanServer var13 = ManagementFactory.getPlatformMBeanServer();
                Iterator var14 = entries.iterator();

                while(var14.hasNext()) {
                    Entry entry = (Entry)var14.next();
                    Key key = (Key)entry.getKey();
                    if(LimitedInMemoryCounterDataStore.DefaultCounterTimestamped.class.isInstance(entry.getValue())) {
                        boolean removed = this.remove(key) != null;
                        if(removed) {
                            if(LimitedInMemoryCounterDataStore.this.gauged) {
                                Collection e = (Collection)LimitedInMemoryCounterDataStore.this.gauges.remove(key);
                                if(e != null) {
                                    Iterator i$1 = e.iterator();

                                    while(i$1.hasNext()) {
                                        Gauge gauge = (Gauge)i$1.next();
                                        Repository.INSTANCE.stopGauge(gauge);
                                    }
                                }
                            }

                            if(LimitedInMemoryCounterDataStore.this.jmx) {
                                try {
                                    ObjectName var15 = ((DefaultCounter)DefaultCounter.class.cast(entry.getValue())).getJmx();
                                    if(var13.isRegistered(var15)) {
                                        var13.unregisterMBean(var15);
                                    }
                                } catch (Exception var12) {
                                    ;
                                }
                            }

                            if(toEvict-- <= 0) {
                                break;
                            }
                        }
                    }
                }

            }
        }
    }
}