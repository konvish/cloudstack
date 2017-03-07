package com.kong.cache.spring;

import com.kong.monitor.model.SFCountMonitorEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.Serializable;
/**
 * 两级缓存
 * L2基于redis实现，因为涉及到key的序列化，目前默认使用String类型
 * Created by kong on 2016/1/22.
 */
public class HybirdCache implements Cache {
    /** L1cache jvm进程内缓存 example:guava */
    private Cache l1Cache;
    /** L2cache 分布式缓存 example:redis */
    private Cache l2Cache;
    private static final Object NULL_HOLDER = new HybirdCache.NullHolder();
    private final String name;
    private final boolean allowNullValues;
    public static final String L1 = "_l1";
    /** metric记录 */
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
        //TODO metric记录 整体缓存命中率、l1的缓存命中率、 l2的缓存命中率
        this.sfCountMonitorEvent.addSucc(key.toString(), 1L);
        ValueWrapper value = this.l1Cache.get(key);
        if(value == null) {
            //l1 未命中
            this.sfCountMonitorEvent.addFail(key.toString() + L1, 1L);
            value = this.l2Cache.get(String.valueOf(key));
            if(value == null) {
                //都未命中
                this.sfCountMonitorEvent.addFail(key.toString(), 1L);
            }

            return value;
        } else {
            //l1 命中
            this.sfCountMonitorEvent.addSucc(key.toString() + L1, 1L);
            return value;
        }
    }

    public <T> T get(Object key, Class<T> type) {
        //TODO metric记录
        Object value = this.l1Cache.get(key, type);
        return value == null?this.l2Cache.get(String.valueOf(key), type):null;
    }

    public void put(Object key, Object value) {
        //TODO metric记录
        this.l1Cache.put(key, value);
        this.l2Cache.put(String.valueOf(key), value);
    }

    public ValueWrapper putIfAbsent(Object key, Object value) {
        //TODO metric记录
        this.l1Cache.putIfAbsent(key, value);
        return this.l2Cache.putIfAbsent(String.valueOf(key), value);
    }

    public void evict(Object key) {
        //TODO metric记录
        this.l1Cache.evict(key);
        this.l2Cache.evict(String.valueOf(key));
    }

    public void clear() {
        //TODO metric记录
        this.l1Cache.clear();
        //l2缓存 不支持全局clear
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