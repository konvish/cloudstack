package com.kong.monitor.model;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Created by kong on 2016/1/22.
 */
public class SFCountMonitorEvent implements Event {
    private static final String VALUE_JOIN = ",";
    private static final String KEY_JOIN = "_";
    private String name;
    private ConcurrentMap<String, SFCountMonitorEvent.SFCount> sfCountConcurrentMap = Maps.newConcurrentMap();
    Lock lock = new ReentrantLock();

    public SFCountMonitorEvent() {
        MonitorEventContainer.getInstance().addEvent(this);
    }

    public void add(String property, String value) {
    }

    public String snapshot() {
        Map snapshotMap = null;
        this.lock.lock();

        try {
            snapshotMap = Collections.unmodifiableMap(this.sfCountConcurrentMap);
            this.sfCountConcurrentMap = Maps.newConcurrentMap();
        } finally {
            this.lock.unlock();
        }

        StringBuilder stringBuilder = new StringBuilder();
        Iterator i$ = snapshotMap.entrySet().iterator();

        while(i$.hasNext()) {
            Entry entry = (Entry)i$.next();
            stringBuilder.append(" ").append((String)entry.getKey()).append(" ").append(((SFCountMonitorEvent.SFCount)entry.getValue()).getSuccess().sum()).append(",").append(((SFCountMonitorEvent.SFCount)entry.getValue()).getFail().sum());
        }

        return stringBuilder.toString();
    }

    public void addSucc(String key, long delta) {
        this.lock.lock();

        try {
            SFCountMonitorEvent.SFCount sfCount = (SFCountMonitorEvent.SFCount)this.sfCountConcurrentMap.get(key);
            if(sfCount == null) {
                sfCount = new SFCountMonitorEvent.SFCount();
            }

            sfCount.addSucc(delta);
            SFCountMonitorEvent.SFCount oldSFCount = (SFCountMonitorEvent.SFCount)this.sfCountConcurrentMap.putIfAbsent(key, sfCount);
            if(oldSFCount == null) {
                ;
            }
        } finally {
            this.lock.unlock();
        }

    }

    public void addFail(String key, long delta) {
        this.lock.lock();

        try {
            SFCountMonitorEvent.SFCount sfCount = (SFCountMonitorEvent.SFCount)this.sfCountConcurrentMap.get(key);
            if(sfCount == null) {
                sfCount = new SFCountMonitorEvent.SFCount();
            }

            sfCount.addFail(delta);
            SFCountMonitorEvent.SFCount oldSFCount = (SFCountMonitorEvent.SFCount)this.sfCountConcurrentMap.putIfAbsent(key, sfCount);
            if(oldSFCount == null) {
                ;
            }
        } finally {
            this.lock.unlock();
        }

    }

    public void addSucc(String key1, String key2, long delta) {
        this.addSucc(key1 + "_" + key2, delta);
    }

    public void addFail(String key1, String key2, long delta) {
        this.addFail(key1 + "_" + key2, delta);
    }

    private class SFCount {
        private LongAdder success;
        private LongAdder fail;

        private SFCount() {
            this.success = new LongAdder();
            this.fail = new LongAdder();
        }

        public void addSucc(long delta) {
            this.success.add(delta);
        }

        public void addFail(long delta) {
            this.fail.add(delta);
        }

        public LongAdder getSuccess() {
            return this.success;
        }

        public LongAdder getFail() {
            return this.fail;
        }
    }
}