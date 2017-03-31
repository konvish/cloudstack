package com.kong.cloudstack.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
/**
 * 基于spring集成Jedis template，在applicationContext-redis中配置
 * 在RedisTemplate外又封装了一层，实现了RedisAPIs接口
 * Created by kong on 2016/1/24.
 */
@Repository("redisRepository")
public class RedisRepository<K, V> implements IRedisRepository<K, V> {
    private Logger logger = LoggerFactory.getLogger(RedisRepository.class);
    @Autowired
    private RedisTemplate<K, V> redisTemplate;

    public RedisRepository() {
    }

    private BoundValueOperations<K, V> getBoundValueOps(K key) {
        return this.redisTemplate.boundValueOps(key);
    }

    private BoundZSetOperations<K, V> getBoundZSetOps(K key) {
        return this.redisTemplate.boundZSetOps(key);
    }

    private BoundSetOperations<K, V> getBoundSetOps(K key) {
        return this.redisTemplate.boundSetOps(key);
    }

    private BoundListOperations<K, V> getBoundListOps(K key) {
        return this.redisTemplate.boundListOps(key);
    }

    private <HK, HV> BoundHashOperations<K, HK, HV> getBoundHashOps(K key) {
        return this.redisTemplate.boundHashOps(key);
    }

    public void del(K key) {
        this.redisTemplate.delete(key);
    }

    public void del(Collection<K> keys) {
        this.redisTemplate.delete(keys);
    }

    public Boolean exists(K key) {
        return this.redisTemplate.hasKey(key);
    }

    public Boolean expire(K key, long timeout, TimeUnit unit) {
        return this.redisTemplate.expire(key, timeout, unit);
    }

    public void expireAt(K key, Date date) {
        this.redisTemplate.expireAt(key, date);
    }

    public Set<K> keys(K pattern) {
        return this.redisTemplate.keys(pattern);
    }

    public String type(K key) {
        return this.redisTemplate.type(key).code();
    }

    public V get(K key) {
        BoundValueOperations ops = this.getBoundValueOps(key);
        return (V)ops.get();
    }

    public V getSet(K key, V value) {
        BoundValueOperations ops = this.getBoundValueOps(key);
        return (V)ops.getAndSet(value);
    }

    public Long incr(K key, long delta) {
        BoundValueOperations ops = this.getBoundValueOps(key);
        return ops.increment(delta);
    }

    public void set(K key, V value) {
        BoundValueOperations ops = this.getBoundValueOps(key);
        ops.set(value);
    }

    public void set(K key, V value, long timeout, TimeUnit unit) {
        BoundValueOperations ops = this.getBoundValueOps(key);
        ops.set(value, timeout, unit);
    }

    public void hDel(K key, Object... hKeys) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        ops.delete(hKeys);
    }

    public Boolean hExists(K key, K hKeys) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return ops.hasKey(hKeys);
    }

    public Map<K, V> hGet(K key) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return ops.entries();
    }

    public V hGet(K key, K hKey) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return (V)ops.get(hKey);
    }

    public Set<K> hKeys(K key) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return ops.keys();
    }

    public Long hLen(K key) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return ops.size();
    }

    public void hSet(K key, K hk, V hv) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        ops.put(hk, hv);
    }

    public void hSet(K key, Map<K, V> map) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        ops.putAll(map);
    }

    public List<V> hVals(K key) {
        BoundHashOperations ops = this.getBoundHashOps(key);
        return ops.values();
    }

    public V lIndex(K key, long index) {
        BoundListOperations ops = this.getBoundListOps(key);
        return (V)ops.index(index);
    }

    public void lInsert(K key, long index, V value) {
        BoundListOperations ops = this.getBoundListOps(key);
        ops.set(index, value);
    }

    public Long lLen(K key) {
        BoundListOperations ops = this.getBoundListOps(key);
        return ops.size();
    }

    public V lPop(K key) {
        BoundListOperations ops = this.getBoundListOps(key);
        return (V)ops.leftPop();
    }

    public V lPop(K key, long timeout, TimeUnit unit) {
        BoundListOperations ops = this.getBoundListOps(key);
        return (V)ops.leftPop(timeout, unit);
    }

    public Long lPush(K key, V value) {
        BoundListOperations ops = this.getBoundListOps(key);
        return ops.leftPush(value);
    }

    public List<V> lRange(K key, long start, long end) {
        BoundListOperations ops = this.getBoundListOps(key);
        return ops.range(start, end);
    }

    public Long lRem(K key, long index, V value) {
        BoundListOperations ops = this.getBoundListOps(key);
        return ops.remove(index, value);
    }

    public void lSet(K key, long index, V value) {
        BoundListOperations ops = this.getBoundListOps(key);
        ops.set(index, value);
    }

    public void ltrim(K key, long start, long end) {
        BoundListOperations ops = this.getBoundListOps(key);
        ops.trim(start, end);
    }

    public Long rPush(K key, V value) {
        BoundListOperations ops = this.getBoundListOps(key);
        return ops.rightPush(value);
    }

    public V rPop(K key) {
        BoundListOperations ops = this.getBoundListOps(key);
        return (V)ops.rightPop();
    }

    public Long sAdd(K key, V value) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.add(new Object[]{value});
    }

    public Set<V> sDiff(K key) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.diff(key);
    }

    public Set<V> sMembers(K key) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.members();
    }

    public Boolean sIsMember(K key, V value) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.isMember(value);
    }

    public V sPop(K key) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return (V)ops.pop();
    }

    public Long sRem(K key, V value) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.remove(new Object[]{value});
    }

    public Long sCard(K key) {
        BoundSetOperations ops = this.getBoundSetOps(key);
        return ops.size();
    }

    public void zAdd(K key, V value, double score) {
        BoundZSetOperations ops = this.getBoundZSetOps(key);
        ops.add(value, score);
    }

    public Set<V> zRange(K key, long start, long end) {
        BoundZSetOperations ops = this.getBoundZSetOps(key);
        return ops.range(start, end);
    }

    public Long zRem(K key, Object... values) {
        BoundZSetOperations ops = this.getBoundZSetOps(key);
        return ops.remove(values);
    }

    public Long zCard(K key) {
        BoundZSetOperations ops = this.getBoundZSetOps(key);
        return ops.zCard();
    }

    public RedisTemplate<K, V> getRedisTemplate() {
        return this.redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
