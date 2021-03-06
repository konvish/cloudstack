package com.kong.cloudstack.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 具有名称的线程工厂
 * Created by kong on 2016/1/22.
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
    private final AtomicInteger mThreadNum;
    private final String mPrefix;
    private final boolean mDaemo;
    private final ThreadGroup mGroup;

    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    /**
     * 构造方法，对线程命名
     * @param prefix 线程前缀
     * @param daemo daemo
     * s'$prefix-thread-$threadNum
     */
    public NamedThreadFactory(String prefix, boolean daemo) {
        this.mThreadNum = new AtomicInteger(1);
        this.mPrefix = prefix + "-thread-";
        this.mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        this.mGroup = s == null?Thread.currentThread().getThreadGroup():s.getThreadGroup();
    }

    /**
     * 生成命名后的线程
     * @param runnable runnable
     * @return Thread
     */
    public Thread newThread(Runnable runnable) {
        String name = this.mPrefix + this.mThreadNum.getAndIncrement();
        Thread ret = new Thread(this.mGroup, runnable, name, 0L);
        ret.setDaemon(this.mDaemo);
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return this.mGroup;
    }
}
