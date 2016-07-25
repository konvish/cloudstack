package com.kong.cache.spring;

import com.kong.monitor.model.SFCountMonitorEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
/**
 * Created by kong on 2016/1/22.
 */
public class HybirdCacheManager implements CacheManager {
    private boolean allowNullValues = true;
    private GuavaCacheManager l1CacheManager;
    private RedisCacheManager l2CacheManager;
    private SFCountMonitorEvent sfCountMonitorEvent;

    public HybirdCacheManager() {
    }

    public HybirdCacheManager(String... cacheNames) {
        this.setCacheNames(Arrays.asList(cacheNames));
    }

    public void setCacheNames(Collection<String> cacheNames) {
        if(cacheNames != null) {
            this.l1CacheManager.setCacheNames(cacheNames);
        }

    }

    public Cache getCache(String name) {
        Cache l1Cache = this.l1CacheManager.getCache(name);
        Cache l2Cache = this.l2CacheManager.getCache(name);
        Cache hybirdCache = this.createHybirdCache(l1Cache, l2Cache, name);
        return hybirdCache;
    }

    public Collection<String> getCacheNames() {
        LinkedHashSet names = new LinkedHashSet();
        names.addAll(this.l1CacheManager.getCacheNames());
        names.addAll(this.l2CacheManager.getCacheNames());
        return Collections.unmodifiableSet(names);
    }

    protected Cache createHybirdCache(Cache l1Cache, Cache l2Cache, String name) {
        return new HybirdCache(name, l1Cache, l2Cache, this.isAllowNullValues(), this.sfCountMonitorEvent);
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    public GuavaCacheManager getL1CacheManager() {
        return this.l1CacheManager;
    }

    public void setL1CacheManager(GuavaCacheManager l1CacheManager) {
        this.l1CacheManager = l1CacheManager;
    }

    public RedisCacheManager getL2CacheManager() {
        return this.l2CacheManager;
    }

    public void setL2CacheManager(RedisCacheManager l2CacheManager) {
        this.l2CacheManager = l2CacheManager;
    }

    public SFCountMonitorEvent getSfCountMonitorEvent() {
        return this.sfCountMonitorEvent;
    }

    public void setSfCountMonitorEvent(SFCountMonitorEvent sfCountMonitorEvent) {
        this.sfCountMonitorEvent = sfCountMonitorEvent;
    }
}