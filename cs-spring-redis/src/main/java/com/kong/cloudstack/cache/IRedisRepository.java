package com.kong.cloudstack.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/**
 * redis操作接口
 * Created by kong on 2016/1/24.
 */
public interface IRedisRepository<K, V> {
    void del(K key);

    void del(Collection<K> keys);

    Boolean exists(K key);

    Boolean expire(K key, long seconds, TimeUnit timeUnit);

    void expireAt(K key, Date date);

    Set<K> keys(K key);

    String type(K key);

    V get(K key);

    V getSet(K key, V value);

    Long incr(K key, long number);

    void set(K key, V value);

    void set(K key, V value, long seconds, TimeUnit timeUnit);

    void hDel(K key, Object... hashKey);

    Boolean hExists(K key, K hashKey);

    Map<K, V> hGet(K key);

    V hGet(K key, K hashKey);

    Set<K> hKeys(K key);

    Long hLen(K key);

    void hSet(K key, K hashKey, V hashValue);

    void hSet(K key, Map<K, V> map);

    List<V> hVals(K key);

    V lIndex(K key, long index);

    void lInsert(K key, long index, V value);

    Long lLen(K key);

    V lPop(K key);

    V lPop(K key, long index, TimeUnit timeUnit);

    Long lPush(K key, V value);

    List<V> lRange(K key, long startIndex, long endIndex);

    Long lRem(K key, long index, V value);

    void lSet(K key, long index, V value);

    void ltrim(K key, long startIndex, long endIndex);

    Long rPush(K key, V value);

    V rPop(K key);

    Long sAdd(K key, V value);

    Set<V> sDiff(K key);

    Set<V> sMembers(K key);

    Boolean sIsMember(K key, V value);

    V sPop(K key);

    Long sRem(K key, V value);

    Long sCard(K key);

    void zAdd(K key, V value, double num);

    Set<V> zRange(K key, long startIndex, long endIndex);

    Long zRem(K key, Object... value);

    Long zCard(K key);
}
