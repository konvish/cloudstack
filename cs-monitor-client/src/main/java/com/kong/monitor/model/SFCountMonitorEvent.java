package com.kong.monitor.model;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 成功失败次数    key v1,v2    或    key1_key2 v1,v2
 * <p/>
 * Created by kong on 2016/1/22.
 */
public class SFCountMonitorEvent implements Event{
    private static final String VALUE_JOIN = ",";
    private static final String KEY_JOIN = "_";

    private String name;
    private ConcurrentMap<String, SFCount> sfCountConcurrentMap = Maps.newConcurrentMap();
    Lock lock = new ReentrantLock();

    public SFCountMonitorEvent() {
        MonitorEventContainer.getInstance().addEvent(this);
    }

    @Override
    public void add(String property, String value) {

    }

    @Override
    public String snapshot() {
        Map<String, SFCount> snapshotMap = null;
        lock.lock();
        try{
            snapshotMap = Collections.unmodifiableMap(sfCountConcurrentMap);
            sfCountConcurrentMap = Maps.newConcurrentMap();
        } finally {
            lock.unlock();
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, SFCount> entry : snapshotMap.entrySet()){
            stringBuilder.append(SPACE)
                    .append(entry.getKey())
                    .append(SPACE)
                    .append(entry.getValue().getSuccess().sum())
                    .append(VALUE_JOIN)
                    .append(entry.getValue().getFail().sum());
        }
        return stringBuilder.toString();
    }

    private class SFCount{
        private LongAdder success = new LongAdder();
        private LongAdder fail = new LongAdder();

        public void addSucc(long delta){
            success.add(delta);
        }

        public void addFail(long delta){
            fail.add(delta);
        }

        public LongAdder getSuccess() {
            return success;
        }

        public LongAdder getFail() {
            return fail;
        }
    }

    public void addSucc(String key, long delta){
        lock.lock();
        try{
            SFCount sfCount = sfCountConcurrentMap.get(key);
            if(sfCount == null){
                sfCount = new SFCount();
            }
            sfCount.addSucc(delta);

            SFCount oldSFCount = sfCountConcurrentMap.putIfAbsent(key, sfCount);
            if(oldSFCount == null){
                oldSFCount = sfCount;
            }
        } finally {
            lock.unlock();
        }
    }

    public void addFail(String key, long delta){
        lock.lock();
        try{
            SFCount sfCount = sfCountConcurrentMap.get(key);
            if(sfCount == null){
                sfCount = new SFCount();
            }
            sfCount.addFail(delta);

            SFCount oldSFCount = sfCountConcurrentMap.putIfAbsent(key, sfCount);
            if(oldSFCount == null){
                oldSFCount = sfCount;
            }
        } finally {
            lock.unlock();
        }
    }

    public void addSucc(String key1, String key2, long delta){
        this.addSucc(key1 + KEY_JOIN + key2, delta);
    }

    public void addFail(String key1, String key2, long delta){
        this.addFail(key1 + KEY_JOIN + key2, delta);
    }
}
