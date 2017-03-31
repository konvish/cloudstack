package com.kong.monitor.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 带缓存的写日志工具。解决高tps项，统计日志量太大的问题
 * 对统计日志在内存中作缓冲合并，定时刷出。
 * add(key1(统计目标),key2(group),key3(flag),timeuse)
 *
 * 内存中及刷出后的结构为：
 * appName   method      flag    count(sum)time(sum) min         max
 * ju        get         执行成功  执行次数   响应时间   最小响应时间 最大响应时间
 * sns       set         执行失败  执行次数   响应时间   最小响应时间 最大响应时间
 *
 * 最后由日志解析工具生成的报表可能是：
 * appName method 成功次数  成功平均响应时间 成功最小响应时间 成功最大响应时间  失败次数  失败平均响应时间 失败最小响应时间 失败最大响应时间
 *
 * key太多的问题：
 * 用定长map，当map满时，刷出执行次数最小的1/3数据。这样的好处是不用每次get/put都排序。不会频繁刷出。
 * 既能拦截绝大部分热点key的流量，又相当于对非热点的key做了批量写入。
 *
 * 副作用：
 * 因为累加了一段时间内的执行次数和响应时间，可以同时作为时间片方式的实时监控报警。但是报警的间隔时间可能要求更小
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

    /**
     * 日志书写者，设置每一行的目标格式
     */
    private static LogWriter logWriter = new LogWriter() {
        private void addLine(StringBuilder sb, Object key, Object group, Object flag, StatCounter sc, String time) {
            sb.append(key).append(logFieldSep).append(group).append(logFieldSep).append(flag).append(logFieldSep).append(typeCount).append(logFieldSep).append(sc.getCount()).append(logFieldSep)
                    .append(sc.getValue()).append(logFieldSep).append(time).append(linesep);
            sb.append(key).append(logFieldSep).append(group).append(logFieldSep).append(flag).append(logFieldSep).append(typeTime).append(logFieldSep).append(sc.getMin()).append(logFieldSep).append(
                    sc.getMax()).append(logFieldSep).append(time).append(linesep);
        }

        public void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> map) {
            statlog.debug(Thread.currentThread().getName() + "[writeLog]map.size()=" + map.size() + linesep);
            StringBuilder sb = new StringBuilder();
            String time = df.format(new Date());
            for (Map.Entry<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> e0 : map.entrySet()) {
                for (Map.Entry<Object, ConcurrentHashMap<Object, StatCounter>> e1 : e0.getValue().entrySet()) {
                    for (Map.Entry<Object, StatCounter> e2 : e1.getValue().entrySet()) {
                        StatCounter sc = e2.getValue();
                        addLine(sb, e0.getKey(), e1.getKey(), e2.getKey(), sc, time);
                    }
                }
            }
            statlog.warn(sb.toString());
        }
    };

    /**
     * 自定义logWriter格式
     * @param logWriter logWriter
     */
    public static void setLogWriter(LogWriter logWriter) {
        BizStatLogger.logWriter = logWriter;
    }

    /**
     * 日志书写者接口
     */
    public static interface LogWriter {
        void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> map);
    }

    /**
     * 日志统计类
     * 多线程下保证操作的原子性
     */
    static class StatCounter {
        private final AtomicLong count = new AtomicLong(0L);
        private final AtomicLong value = new AtomicLong(0L);
        private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);

        /**
         * 加上count，value
         * 并记下value的最小值与最大值
         * @param c count
         * @param v value
         */
        public void add(long c, long v) {
            this.count.addAndGet(c);
            this.value.addAndGet(v);
            while (true) {
                long vmin = min.get();
                if (v < vmin) {
                    if (min.compareAndSet(vmin, v)) {
                        break;
                    }
                    continue;
                }
                break;
            }
            while (true) {
                long vmax = max.get();
                if (v > vmax) {
                    if (max.compareAndSet(vmax, v)) {
                        break;
                    }
                    continue;
                }
                break;
            }
        }

        /**
         * 复原
         */
        public synchronized void reset() {
            this.count.set(0L);
            this.value.set(0L);
            this.min.set(Long.MAX_VALUE);
            this.max.set(Long.MIN_VALUE);
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
            return new long[] { this.count.get(), this.value.get(), this.min.get(), this.max.get() };
        }
    }

    /**
     * 设置表的大小，key为1024，表的大小0.75f，操作线程数32
     */
    private static ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> keys = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>(
            maxkeysize, 0.75f, 32);

    /**
     * 添加BizStat日志
     * @param key app名称
     * @param group 组别
     * @param flag 方法
     * @param count 计算器
     * @param timeuse 时间
     */
    public static void add(Object key, Object group, Object flag, long count, long timeuse) {
        ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> oldkeys = keys;
        ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> groups = oldkeys.get(key);
        /**不存在组就添加进去*/
        if (groups == null) {
            ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> newGroups = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>();
            groups = oldkeys.putIfAbsent(key, newGroups);
            if (groups == null) {
                groups = newGroups;
                insureSize();
            }
        }
        /**组中的方法，不存在添加进去*/
        ConcurrentHashMap<Object, StatCounter> flags = groups.get(group);
        if (flags == null) {
            ConcurrentHashMap<Object, StatCounter> newFlags = new ConcurrentHashMap<Object, StatCounter>();
            flags = groups.putIfAbsent(group, newFlags);
            if (flags == null) {
                flags = newFlags;
            }
        }
        /**方法的StatCounter，不存在添加进去*/
        StatCounter counter = flags.get(flag);
        if (counter == null) {
            StatCounter newCounter = new StatCounter();
            counter = flags.putIfAbsent(flag, newCounter);
            if (counter == null) {
                counter = newCounter;
            }
        }
        counter.add(count, timeuse);
    }

    private static Lock lock = new ReentrantLock();
    private static volatile boolean isInFlushing = false;
    private static ExecutorService flushExecutor = Executors.newSingleThreadExecutor();
    private static final Thread fullDumpThread;

    static {

        fullDumpThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(dumpInterval * 1000);
                    } catch (InterruptedException e) {
                    }
                    submitFlush(true);
                }
            }
        });
        fullDumpThread.start();
    }

    /**
     * 保证key的数量不超过1024
     */
    private static void insureSize() {
        if (keys.size() < maxkeysize) {
            return;
        }
        submitFlush(false);
    }

    /**
     * 提交日志
     * @param isFlushAll 是否key满1024
     * @return boolean
     */
    private static boolean submitFlush(final boolean isFlushAll) {
        /**不是正在提交状态，加锁*/
        if (!isInFlushing && lock.tryLock()) {
            try {
                //更新为提交状态
                isInFlushing = true;
                flushExecutor.execute(new Runnable() {
                    public void run() {
                        try {
                            if (isFlushAll) {
                                flushAll();
                            } else {
                                flushLRU();
                            }
                        } finally {
                            isInFlushing = false;
                        }
                    }
                });
            } finally {
                lock.unlock();
            }
            return true;
        }
        return false;
    }

    private static void flushAll() {
        Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> res = keys;
        keys = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>(maxkeysize);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }
        statlog.info("[flushAll]size=" + res.size() + linesep);
        logWriter.writeLog(res);
        res = null;
    }

    private static final Comparator<Object[]> countsComparator = new Comparator<Object[]>() {
        public int compare(Object[] keycount1, Object[] keycount2) {
            Long v1 = (Long) keycount1[1];
            Long v2 = (Long) keycount2[1];
            return v1.compareTo(v2);
        }
    };

    /**
     *
     */
    private static void flushLRU() {
        List<Object[]> counts = new ArrayList<Object[]>();
        for (Map.Entry<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> e0 : keys.entrySet()) {
            long count = 0;
            for (Map.Entry<Object, ConcurrentHashMap<Object, StatCounter>> e1 : e0.getValue().entrySet()) {
                for (Map.Entry<Object, StatCounter> e2 : e1.getValue().entrySet()) {
                    count += e2.getValue().getCount();
                }
            }
            counts.add(new Object[] { e0.getKey(), count });
        }
        statlog.debug("sortedSize=" + counts.size() + ",keys.size=" + keys.size() + linesep);// sortedSize=1135,keys.size=1169
        Collections.sort(counts, countsComparator);
        int i = 0;
        int remain = maxkeysize * 2 / 3;
        int flush = keys.size() - remain;
        Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> flushed = new HashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>();
        for (Object[] keycount : counts) {
            Object key = keycount[0];
            ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> removed = keys.remove(key);
            if (removed != null) {
                flushed.put(key, removed);
                i++;
            } else {
                statlog.warn("-------------- Should not happen!!! ------------");
            }
            if (i >= flush) {
                if (keys.size() <= remain)
                    break;
            }
        }
        statlog.info("[flushLRU]flushedSize=" + flushed.size() + ",keys.size=" + keys.size() + linesep);
        logWriter.writeLog(flushed);
        flushed = null;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            BizStatLogger.add("appName1", "namespace1", "methodName1", 1, 10L);
            BizStatLogger.add("appName2", "namespace2", "methodName2", 2, 20L);
            BizStatLogger.add("appName3", "namespace3", "methodName3", 3, 30L);
            BizStatLogger.add("appName4", "namespace4", "methodName4", 4, 40L);
            if (i % 10 == 0) {
                Thread.sleep(1);
            }
        }
        BizStatLogger.flushLRU();
        Thread.sleep(10000);
    }
}
