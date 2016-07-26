package com.kong.cloudstack.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/**
 *
 * Created by kong on 2016/1/24.
 */
public interface IRedisRepository<K, V> {
    void del(K var1);

    void del(Collection<K> var1);

    Boolean exists(K var1);

    Boolean expire(K var1, long var2, TimeUnit var4);

    void expireAt(K var1, Date var2);

    Set<K> keys(K var1);

    String type(K var1);

    V get(K var1);

    V getSet(K var1, V var2);

    Long incr(K var1, long var2);

    void set(K var1, V var2);

    void set(K var1, V var2, long var3, TimeUnit var5);

    void hDel(K var1, Object... var2);

    Boolean hExists(K var1, K var2);

    Map<K, V> hGet(K var1);

    V hGet(K var1, K var2);

    Set<K> hKeys(K var1);

    Long hLen(K var1);

    void hSet(K var1, K var2, V var3);

    void hSet(K var1, Map<K, V> var2);

    List<V> hVals(K var1);

    V lIndex(K var1, long var2);

    void lInsert(K var1, long var2, V var4);

    Long lLen(K var1);

    V lPop(K var1);

    V lPop(K var1, long var2, TimeUnit var4);

    Long lPush(K var1, V var2);

    List<V> lRange(K var1, long var2, long var4);

    Long lRem(K var1, long var2, V var4);

    void lSet(K var1, long var2, V var4);

    void ltrim(K var1, long var2, long var4);

    Long rPush(K var1, V var2);

    V rPop(K var1);

    Long sAdd(K var1, V var2);

    Set<V> sDiff(K var1);

    Set<V> sMembers(K var1);

    Boolean sIsMember(K var1, V var2);

    V sPop(K var1);

    Long sRem(K var1, V var2);

    Long sCard(K var1);

    void zAdd(K var1, V var2, double var3);

    Set<V> zRange(K var1, long var2, long var4);

    Long zRem(K var1, Object... var2);

    Long zCard(K var1);
}
