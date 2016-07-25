package com.kong.monitor.biz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class BizStatLogger {
    public static final Logger statlog = LoggerFactory.getLogger("SPEC-BIZSTAT-LOGGER");
    public static final String logFieldSep = "#@#";
    public static final String typeCount = "count";
    public static final String typeTime = "time";
    public static final String linesep = System.getProperty("line.separator");
    public static volatile int maxkeysize = 1024;
    public static volatile int dumpInterval = 300;
    public static final SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss:SSS");
    private static BizStatLogger.LogWriter logWriter = new BizStatLogger.LogWriter() {
        private void addLine(StringBuilder sb, Object key, Object group, Object flag, BizStatLogger.StatCounter sc, String time) {
            sb.append(key).append("#@#").append(group).append("#@#").append(flag).append("#@#").append("count").append("#@#").append(sc.getCount()).append("#@#").append(sc.getValue()).append("#@#").append(time).append(BizStatLogger.linesep);
            sb.append(key).append("#@#").append(group).append("#@#").append(flag).append("#@#").append("time").append("#@#").append(sc.getMin()).append("#@#").append(sc.getMax()).append("#@#").append(time).append(BizStatLogger.linesep);
        }

        public void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, BizStatLogger.StatCounter>>> map) {
            BizStatLogger.statlog.debug(Thread.currentThread().getName() + "[writeLog]map.size()=" + map.size() + BizStatLogger.linesep);
            StringBuilder sb = new StringBuilder();
            String time = BizStatLogger.df.format(new Date());
            Iterator i$ = map.entrySet().iterator();

            while(i$.hasNext()) {
                Entry e0 = (Entry)i$.next();
                Iterator i$1 = ((ConcurrentHashMap)e0.getValue()).entrySet().iterator();

                while(i$1.hasNext()) {
                    Entry e1 = (Entry)i$1.next();
                    Iterator i$2 = ((ConcurrentHashMap)e1.getValue()).entrySet().iterator();

                    while(i$2.hasNext()) {
                        Entry e2 = (Entry)i$2.next();
                        BizStatLogger.StatCounter sc = (BizStatLogger.StatCounter)e2.getValue();
                        this.addLine(sb, e0.getKey(), e1.getKey(), e2.getKey(), sc, time);
                    }
                }
            }

            BizStatLogger.statlog.warn(sb.toString());
        }
    };
    private static ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, BizStatLogger.StatCounter>>> keys;
    private static Lock lock;
    private static volatile boolean isInFlushing;
    private static ExecutorService flushExecutor;
    private static final Thread fullDumpThread;
    private static final Comparator<Object[]> countsComparator;

    public BizStatLogger() {
    }

    public static void setLogWriter(BizStatLogger.LogWriter logWriter) {
        logWriter = logWriter;
    }

    public static void add(Object key, Object group, Object flag, long count, long timeuse) {
        ConcurrentHashMap oldkeys = keys;
        ConcurrentHashMap groups = (ConcurrentHashMap)oldkeys.get(key);
        ConcurrentHashMap flags;
        if(groups == null) {
            flags = new ConcurrentHashMap();
            groups = (ConcurrentHashMap)oldkeys.putIfAbsent(key, flags);
            if(groups == null) {
                groups = flags;
                insureSize();
            }
        }

        flags = (ConcurrentHashMap)groups.get(group);
        if(flags == null) {
            ConcurrentHashMap counter = new ConcurrentHashMap();
            flags = (ConcurrentHashMap)groups.putIfAbsent(group, counter);
            if(flags == null) {
                flags = counter;
            }
        }

        BizStatLogger.StatCounter counter1 = (BizStatLogger.StatCounter)flags.get(flag);
        if(counter1 == null) {
            BizStatLogger.StatCounter newCounter = new BizStatLogger.StatCounter();
            counter1 = (BizStatLogger.StatCounter)flags.putIfAbsent(flag, newCounter);
            if(counter1 == null) {
                counter1 = newCounter;
            }
        }

        counter1.add(count, timeuse);
    }

    private static void insureSize() {
        if(keys.size() >= maxkeysize) {
            submitFlush(false);
        }
    }

    private static boolean submitFlush(final boolean isFlushAll) {
        if(!isInFlushing && lock.tryLock()) {
            try {
                isInFlushing = true;
                flushExecutor.execute(new Runnable() {
                    public void run() {
                        try {
                            if(isFlushAll) {
                                BizStatLogger.flushAll();
                            } else {
                                BizStatLogger.flushLRU();
                            }
                        } finally {
                            BizStatLogger.isInFlushing = false;
                        }

                    }
                });
            } finally {
                lock.unlock();
            }

            return true;
        } else {
            return false;
        }
    }

    private static void flushAll() {
        ConcurrentHashMap res = keys;
        keys = new ConcurrentHashMap(maxkeysize);

        try {
            Thread.sleep(5L);
        } catch (InterruptedException var2) {
            ;
        }

        statlog.info("[flushAll]size=" + res.size() + linesep);
        logWriter.writeLog(res);
        res = null;
    }

    private static void flushLRU() {
        ArrayList counts = new ArrayList();
        Iterator i = keys.entrySet().iterator();

        Iterator i$;
        while(i.hasNext()) {
            Entry remain = (Entry)i.next();
            long flush = 0L;
            i$ = ((ConcurrentHashMap)remain.getValue()).entrySet().iterator();

            while(i$.hasNext()) {
                Entry keycount = (Entry)i$.next();

                Entry removed;
                for(Iterator key = ((ConcurrentHashMap)keycount.getValue()).entrySet().iterator(); key.hasNext(); flush += ((BizStatLogger.StatCounter)removed.getValue()).getCount()) {
                    removed = (Entry)key.next();
                }
            }

            counts.add(new Object[]{remain.getKey(), Long.valueOf(flush)});
        }

        statlog.debug("sortedSize=" + counts.size() + ",keys.size=" + keys.size() + linesep);
        Collections.sort(counts, countsComparator);
        int var9 = 0;
        int var10 = maxkeysize * 2 / 3;
        int var11 = keys.size() - var10;
        HashMap flushed = new HashMap();
        i$ = counts.iterator();

        while(i$.hasNext()) {
            Object[] var12 = (Object[])i$.next();
            Object var13 = var12[0];
            ConcurrentHashMap var14 = (ConcurrentHashMap)keys.remove(var13);
            if(var14 != null) {
                flushed.put(var13, var14);
                ++var9;
            } else {
                statlog.warn("-------------- Should not happen!!! ------------");
            }

            if(var9 >= var11 && keys.size() <= var10) {
                break;
            }
        }

        statlog.info("[flushLRU]flushedSize=" + flushed.size() + ",keys.size=" + keys.size() + linesep);
        logWriter.writeLog(flushed);
        flushed = null;
    }

    public static void main(String[] args) throws InterruptedException {
        for(int i = 0; i < 5000; ++i) {
            add("appName1", "namespace1", "methodName1", 1L, 10L);
            add("appName2", "namespace2", "methodName2", 2L, 20L);
            add("appName3", "namespace3", "methodName3", 3L, 30L);
            add("appName4", "namespace4", "methodName4", 4L, 40L);
            if(i % 10 == 0) {
                Thread.sleep(1L);
            }
        }

        flushLRU();
        Thread.sleep(10000L);
    }

    static {
        keys = new ConcurrentHashMap(maxkeysize, 0.75F, 32);
        lock = new ReentrantLock();
        isInFlushing = false;
        flushExecutor = Executors.newSingleThreadExecutor();
        fullDumpThread = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep((long)(BizStatLogger.dumpInterval * 1000));
                    } catch (InterruptedException var2) {
                        ;
                    }

                    BizStatLogger.submitFlush(true);
                }
            }
        });
        fullDumpThread.start();
        countsComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return 0;
            }

            public int compare(Object[] keycount1, Object[] keycount2) {
                Long v1 = (Long)keycount1[1];
                Long v2 = (Long)keycount2[1];
                return v1.compareTo(v2);
            }
        };
    }

    static class StatCounter {
        private final AtomicLong count = new AtomicLong(0L);
        private final AtomicLong value = new AtomicLong(0L);
        private final AtomicLong min = new AtomicLong(9223372036854775807L);
        private final AtomicLong max = new AtomicLong(-9223372036854775808L);

        StatCounter() {
        }

        public void add(long c, long v) {
            this.count.addAndGet(c);
            this.value.addAndGet(v);

            long vmax;
            do {
                vmax = this.min.get();
            } while(v < vmax && !this.min.compareAndSet(vmax, v));

            do {
                vmax = this.max.get();
            } while(v > vmax && !this.max.compareAndSet(vmax, v));

        }

        public synchronized void reset() {
            this.count.set(0L);
            this.value.set(0L);
            this.min.set(9223372036854775807L);
            this.max.set(-9223372036854775808L);
        }

        public long getCount() {
            return this.count.get();
        }

        public long getValue() {
            return this.value.get();
        }

        public long getMin() {
            return this.min.get();
        }

        public long getMax() {
            return this.max.get();
        }

        public long[] get() {
            return new long[]{this.count.get(), this.value.get(), this.min.get(), this.max.get()};
        }
    }

    public interface LogWriter {
        void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, BizStatLogger.StatCounter>>> var1);
    }
}
