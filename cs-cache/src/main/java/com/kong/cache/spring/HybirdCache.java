package com.kong.cache.spring;

import com.kong.monitor.model.SFCountMonitorEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.Serializable;
/**
 * Created by kong on 2016/1/22.
 */
public class HybirdCache implements Cache {
    private Cache l1Cache;
    private Cache l2Cache;
    private static final Object NULL_HOLDER = new HybirdCache.NullHolder();
    private final String name;
    private final boolean allowNullValues;
    public static final String L1 = "_l1";
    private SFCountMonitorEvent sfCountMonitorEvent;

    public HybirdCache(String name, Cache l1Cache, Cache l2Cache, boolean allowNullValues, SFCountMonitorEvent sfCountMonitorEvent) {
        this.name = name;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.allowNullValues = allowNullValues;
        this.sfCountMonitorEvent = sfCountMonitorEvent;
    }

    public final boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    public String getName() {
        return this.name;
    }

    public Object getNativeCache() {
        return null;
    }

    public ValueWrapper get(Object key) {
        this.sfCountMonitorEvent.addSucc(key.toString(), 1L);
        ValueWrapper value = this.l1Cache.get(key);
        if(value == null) {
            this.sfCountMonitorEvent.addFail(key.toString() + "_l1", 1L);
            value = this.l2Cache.get(String.valueOf(key));
            if(value == null) {
                this.sfCountMonitorEvent.addFail(key.toString(), 1L);
            }

            return value;
        } else {
            this.sfCountMonitorEvent.addSucc(key.toString() + "_l1", 1L);
            return value;
        }
    }

    public <T> T get(Object key, Class<T> type) {
        Object value = this.l1Cache.get(key, type);
        return value == null?this.l2Cache.get(String.valueOf(key), type):null;
    }

    public void put(Object key, Object value) {
        this.l1Cache.put(key, value);
        this.l2Cache.put(String.valueOf(key), value);
    }

    public ValueWrapper putIfAbsent(Object key, Object value) {
        this.l1Cache.putIfAbsent(key, value);
        return this.l2Cache.putIfAbsent(String.valueOf(key), value);
    }

    public void evict(Object key) {
        this.l1Cache.evict(key);
        this.l2Cache.evict(String.valueOf(key));
    }

    public void clear() {
        this.l1Cache.clear();
        this.l2Cache.clear();
    }

    private ValueWrapper toWrapper(Object value) {
        return value != null?new SimpleValueWrapper(this.fromStoreValue(value)):null;
    }

    protected Object fromStoreValue(Object storeValue) {
        return this.allowNullValues && storeValue == NULL_HOLDER?null:storeValue;
    }

    public Cache getL1Cache() {
        return this.l1Cache;
    }

    public void setL1Cache(Cache l1CacheProvider) {
        this.l1Cache = l1CacheProvider;
    }

    public Cache getL2Cache() {
        return this.l2Cache;
    }

    public void setL2Cache(Cache l2CacheProvider) {
        this.l2Cache = l2CacheProvider;
    }

    private static class NullHolder implements Serializable {
        private NullHolder() {
        }
    }
}