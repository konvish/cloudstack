package com.kong.cloudstack.cache;

import com.kong.cloudstack.cache.IRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 基于spring集成Jedis template，在applicationContext-redis中配置
 * 利用jedis自主操作api
 * Redis集群操作API，实现了RedisAPIs接口
 * Created by kong on 2016/1/24.
 */

@Repository("redisManagerRepository")
public class RedisManagerRepository<K, V> implements IRedisRepository<K, V> {

    private Logger logger = LoggerFactory.getLogger(RedisManagerRepository.class);

    private RedisSerializer keySerializer = new StringRedisSerializer();
    private RedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
    private RedisSerializer hashKeySerializer = new StringRedisSerializer();
    private RedisSerializer hashValueSerializer = new StringRedisSerializer();

    @Autowired(required = false)
    private JedisSentinelPool jedisSentinelPool;

    public JedisSentinelPool getJedisSentinelPool() {
        return jedisSentinelPool;
    }

    public void setJedisSentinelPool(JedisSentinelPool jedisSentinelPool) {
        this.jedisSentinelPool = jedisSentinelPool;
    }

    public RedisSerializer getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(RedisSerializer keySerializer) {
        this.keySerializer = keySerializer;
    }

    public RedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(RedisSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public RedisSerializer getHashKeySerializer() {
        return hashKeySerializer;
    }

    public void setHashKeySerializer(RedisSerializer hashKeySerializer) {
        this.hashKeySerializer = hashKeySerializer;
    }

    public RedisSerializer getHashValueSerializer() {
        return hashValueSerializer;
    }

    public void setHashValueSerializer(RedisSerializer hashValueSerializer) {
        this.hashValueSerializer = hashValueSerializer;
    }

    /**
     * 对key进行序列化
     * @param key key
     * @return byte[] key
     */
    private byte[] rawKey(Object key) {
        Assert.notNull(key, "non null key required");
        if (keySerializer == null && key instanceof byte[]) {
            return (byte[]) key;
        }
        return keySerializer.serialize(key);
    }

    /**
     * 对List进行反序列化
     * StringRedisSerializer
     * @param rawValues rawValues
     * @param <T> 反序列化后泛型T
     * @return
     */
    @SuppressWarnings("unchecked")
    <T> List<T> deserializeHashValues(List<byte[]> rawValues) {
        if (hashValueSerializer == null) {
            return (List<T>) rawValues;
        }
        return SerializationUtils.deserialize(rawValues, hashValueSerializer);
    }

    /**
     * 基于Jdk对List反序列化方式
     * JdkSerializationRedisSerializer
     * @param rawValues rawValues
     * @return list(V)
     */
    @SuppressWarnings("unchecked")
    List<V> deserializeValues(List<byte[]> rawValues) {
        if (valueSerializer == null) {
            return (List<V>) rawValues;
        }
        return SerializationUtils.deserialize(rawValues, valueSerializer);
    }

    /**
     * 集合key的序列化
     * @param keys keys集合
     * @return byte[][]
     */
    private byte[][] rawKeys(Collection<K> keys) {
        final byte[][] rawKeys = new byte[keys.size()][];

        int i = 0;
        for (K key : keys) {
            rawKeys[i++] = rawKey(key);
        }

        return rawKeys;
    }

    /**
     * value序列化
     * JdkSerializationRedisSerializer
     * @param value value
     * @return byte[]
     */
    @SuppressWarnings("unchecked")
    byte[] rawValue(Object value) {
        if (valueSerializer == null && value instanceof byte[]) {
            return (byte[]) value;
        }
        return valueSerializer.serialize(value);
    }

    /**
     * hashKeys 序列化
     * @param hashKeys hashKeys...
     * @param <HK> 泛型
     * @return byte[][]
     */
    <HK> byte[][] rawHashKeys(HK... hashKeys) {
        final byte[][] rawHashKeys = new byte[hashKeys.length][];
        int i = 0;
        for (HK hashKey : hashKeys) {
            rawHashKeys[i++] = rawHashKey(hashKey);
        }
        return rawHashKeys;
    }

    /**
     * hashKey 序列化
     * @param hashKey hashKey
     * @param <HK> 泛型
     * @return byte[]
     */
    @SuppressWarnings("unchecked")
    <HK> byte[] rawHashKey(HK hashKey) {
        Assert.notNull(hashKey, "non null hash key required");
        if (hashKeySerializer == null && hashKey instanceof byte[]) {
            return (byte[]) hashKey;
        }
        return hashKeySerializer.serialize(hashKey);
    }

    /**
     * 反序列化
     * @param value value
     * @return V
     */
    private V deserializeValue(byte[] value) {
        if (valueSerializer == null) {
            return (V) value;
        }
        return (V) valueSerializer.deserialize(value);
    }

    /**
     * 对Map进行反序列化
     * @param entries map
     * @param <HK> 泛型HashKey
     * @param <HV> 泛型HashValue
     * @return Map<HK.HV>
     */
    @SuppressWarnings("unchecked")
    <HK, HV> Map<HK, HV> deserializeHashMap(Map<byte[], byte[]> entries) {
        // connection in pipeline/multi mode
        if (entries == null) {
            return null;
        }

        Map<HK, HV> map = new LinkedHashMap<HK, HV>(entries.size());

        for (Map.Entry<byte[], byte[]> entry : entries.entrySet()) {
            map.put((HK) deserializeHashKey(entry.getKey()), (HV) deserializeHashValue(entry.getValue()));
        }

        return map;
    }

    /**
     * 反序列化hashKey
     * @param value value
     * @param <HK> hashKey泛型
     * @return HK
     */
    @SuppressWarnings({"unchecked"})
    <HK> HK deserializeHashKey(byte[] value) {
        if (hashKeySerializer == null) {
            return (HK) value;
        }
        return (HK) hashKeySerializer.deserialize(value);
    }

    /**
     * 反序列化HashValue
     * @param value value
     * @param <HV> HV
     * @return HV
     */
    @SuppressWarnings("unchecked")
    <HV> HV deserializeHashValue(byte[] value) {
        if (hashValueSerializer == null) {
            return (HV) value;
        }
        return (HV) hashValueSerializer.deserialize(value);
    }

    /**
     * 对Set进行反序列化
     * @param rawValues set
     * @return Set<V>
     */
    @SuppressWarnings("unchecked")
    Set<V> deserializeValues(Set<byte[]> rawValues) {
        if (valueSerializer == null) {
            return (Set<V>) rawValues;
        }
        return SerializationUtils.deserialize(rawValues, valueSerializer);
    }

    /**
     * 序列化多个值
     * @param values values...
     * @return byte[][]
     */
    byte[][] rawValues(Object... values) {
        final byte[][] rawValues = new byte[values.length][];
        int i = 0;
        for (Object value : values) {
            rawValues[i++] = rawValue(value);
        }
        return rawValues;
    }

    /**
     * 序列化hashValue
     * @param value value
     * @param <HV> HV
     * @return byte[]
     */
    @SuppressWarnings("unchecked")
    <HV> byte[] rawHashValue(HV value) {
        if (hashValueSerializer == null & value instanceof byte[]) {
            return (byte[]) value;
        }
        return hashValueSerializer.serialize(value);
    }

    /**
     * 反序列化hashKey集合
     * @param rawKeys set
     * @param <T> 泛型T
     * @return Set<T>
     */
    @SuppressWarnings("unchecked")
    <T> Set<T> deserializeHashKeys(Set<byte[]> rawKeys) {
        if (hashKeySerializer == null) {
            return (Set<T>) rawKeys;
        }
        return SerializationUtils.deserialize(rawKeys, hashKeySerializer);
    }
//    private BoundValueOperations<K,V> getBoundValueOps(K key) {
//        Jedis jedis = jedisPool.getResource();
//        jedis.
//        return redisTemplate.boundValueOps(key);
//    }
//
//    private BoundZSetOperations<K,V> getBoundZSetOps(K key) {
//        return redisTemplate.boundZSetOps(key);
//    }
//
//    private BoundSetOperations<K,V> getBoundSetOps(K key) {
//        return redisTemplate.boundSetOps(key);
//    }
//
//    private BoundListOperations<K,V> getBoundListOps(K key) {
//        return redisTemplate.boundListOps(key);
//    }
//
//    private <HK, HV> BoundHashOperations<K, HK, HV> getBoundHashOps(K key) {
//        return redisTemplate.boundHashOps(key);
//    }

    /********************************************/
    /********************************************/
    /********************************************/
    /********************************************/

    /**
     * 删除key
     * @param key key
     */
    public void del(final K key) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.del(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除多个key
     * @param keys keys
     */
    public void del(final Collection<K> keys) {

        Jedis jedis = jedisSentinelPool.getResource();
        try {
            if (CollectionUtils.isEmpty(keys)) {
                return;
            }

            final byte[][] rawKeys = rawKeys(keys);

            jedis.del(rawKeys);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 检查key是否存在
     * @param key key
     * @return boolean
     */
    public Boolean exists(final K key) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.exists(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return true;
        } finally {
            this.returnResource(jedis);
        }

    }

    /**
     * 设置key的过期时间
     * @param key key
     * @param timeout 过期时间s
     * @param unit timeUnit
     * @return boolean
     */
    public Boolean expire(final K key, final long timeout, final TimeUnit unit) {

        final byte[] rawKey = rawKey(key);
        final long rawTimeout = TimeoutUtils.toMillis(timeout, unit);

        Jedis jedis = jedisSentinelPool.getResource();
        try {
            if (rawTimeout > Integer.MAX_VALUE) {
                return JedisConverters.toBoolean(jedis.pexpireAt(rawKey, time(jedis) + rawTimeout));
            }
            return JedisConverters.toBoolean(jedis.pexpire(rawKey, rawTimeout));
        } catch (Exception e) {
            logger.error("", e);
            // Driver may not support pExpire or we may be running on Redis 2.4
            return JedisConverters.toBoolean(jedis.expire(rawKey, (int) TimeoutUtils.toSeconds(timeout, unit)));
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     *  jedis 的时间
     * @param jedis jedis
     * @return long
     */
    public Long time(Jedis jedis) {

        List<String> serverTimeInformation = jedis.time();

        Assert.notEmpty(serverTimeInformation, "Received invalid result from server. Expected 2 items in collection.");
        Assert.isTrue(serverTimeInformation.size() == 2,
                "Received invalid nr of arguments from redis server. Expected 2 received " + serverTimeInformation.size());

        return Converters.toTimeMillis(serverTimeInformation.get(0), serverTimeInformation.get(1));
    }

    /**
     * 设置key在date时候过期
     * @param key key
     * @param date date
     */
    public void expireAt(final K key, Date date) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.pexpireAt(rawKey, date.getTime());
        } catch (Exception e) {
            jedis.expireAt(rawKey, date.getTime() / 1000);
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取所有包含pattern的key
     * @param pattern pattern
     * @return set<key>
     */
    public Set<K> keys(final K pattern) {
        final byte[] rawKey = rawKey(pattern);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            Set<byte[]> rawKeys = jedis.keys(rawKey);
            Set<K> k = (Set<K>) rawKeys;
            return keySerializer != null ? SerializationUtils.deserialize(rawKeys, keySerializer) : k;
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key对应的类型
     * @param key key
     * @return str
     */
    public String type(final K key) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.type(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key对应的值
     * @param key key
     * @return V
     */
    public V get(final K key) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return deserializeValue(jedis.get(rawKey));
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key对应的值，原来的值与value组成的set
     * string的大小不超过1GB
     * @param key key
     * @param value value
     * @return V
     */
    public V getSet(final K key, final V value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return deserializeValue(jedis.getSet(rawKey, rawValue));
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * key的值加上delta个位置
     * @param key key
     * @param delta delta
     * @return long
     */
    public Long incr(final K key, final long delta) {
        final byte[] rawKey = rawKey(key);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.incrBy(rawKey, delta);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置key与对应值
     * @param key key
     * @param value value
     */
    public void set(final K key, final V value) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.set(rawKey, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置key对应value，并设置过期时间
     * @param key key
     * @param value value
     * @param timeout 过期时间s
     * @param unit TimeUnit
     */
    public void set(final K key, final V value, final long timeout, final TimeUnit unit) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.setex(rawKey, (int) TimeoutUtils.toSeconds(timeout, unit), rawValue);
        } catch (Exception e) {
            jedis.psetex(rawKey, timeout, rawValue);
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除hashKey
     * @param key key
     * @param hKeys hashKeys
     */
    public void hDel(final K key, final Object... hKeys) {
        final byte[] rawKey = rawKey(key);
        final byte[][] rawHashKeys = rawHashKeys(hKeys);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.hdel(rawKey, rawHashKeys);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 检查hashKey是否存在
     * @param key key
     * @param hKeys hashKey
     * @return boolean
     */
    public Boolean hExists(final K key, final K hKeys) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawHashKey = rawHashKey(hKeys);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.hexists(rawKey, rawHashKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key对应的map
     * @param key key
     * @return Map
     */
    public Map<K, V> hGet(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            Map<byte[], byte[]> entries = jedis.hgetAll(rawKey);
            return deserializeHashMap(entries);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 根据key与hashKey获取值
     * @param key key
     * @param hKey hashKey
     * @return V
     */
    public V hGet(final K key, final K hKey) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawHashKey = rawHashKey(hKey);
        try {
            byte[] rawHashValue = jedis.hget(rawKey, rawHashKey);
            return deserializeHashValue(rawHashValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * key里面的所有的hashKey
     * @param key key
     * @return set<hashKey>
     */
    public Set<K> hKeys(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            Set<byte[]> rawValues = jedis.hkeys(rawKey);
            return deserializeHashKeys(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * key对应map的长度
     * @param key key
     * @return long
     */
    public Long hLen(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            return jedis.hlen(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 以平铺的方式设置key对应的map
     * @param key key
     * @param hk hashKey
     * @param hv hashValue
     */
    public void hSet(final K key, final K hk, final V hv) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawHashKey = rawHashKey(hk);
        final byte[] rawHashValue = rawHashValue(hv);
        try {
            jedis.hset(rawKey, rawHashKey, rawHashValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 以map方式设置key对应的map
     * @param key key
     * @param map map
     */
    public void hSet(final K key, final Map<K, V> map) {
        if (map.isEmpty()) {
            return;
        }
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            final Map<byte[], byte[]> hashes = new LinkedHashMap<byte[], byte[]>(map.size());

            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                hashes.put(rawHashKey(entry.getKey()), rawHashValue(entry.getValue()));
            }
            jedis.hmset(rawKey, hashes);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key里面所有的hashValue
     * @param key key
     * @return list<hashValue>
     */
    public List<V> hVals(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            List<byte[]> rawValues = jedis.hvals(rawKey);
            return deserializeHashValues(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /*** List ***********/

    /**
     * 获取key对应下标为index的值
     * 针对List
     * @param key key
     * @param index index
     * @return V
     */
    public V lIndex(final K key, final long index) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            byte[] result = jedis.lindex(rawKey, index);
            return deserializeValue(result);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * key对应List的index位置插入值
     * @param key key
     * @param index index
     * @param value value
     */
    public void lInsert(final K key, final long index, V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            jedis.lset(rawKey, index, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取key对应list的长度
     * @param key key
     * @return long
     */
    public Long lLen(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            return jedis.llen(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从key对应的list中pop出值
     * @param key key
     * @return V
     */
    public V lPop(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            byte[] result = jedis.lpop(rawKey);
            return deserializeValue(result);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 某个时间从key对应的list中pop出值
     * @param key key
     * @param timeout 时间s
     * @param unit TimeUnit
     * @return V
     */
    public V lPop(final K key, long timeout, TimeUnit unit) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final int tm = (int) TimeoutUtils.toSeconds(timeout, unit);
        try {
            List<byte[]> lPop = jedis.blpop(tm, rawKey);
            byte[] result = (CollectionUtils.isEmpty(lPop) ? null : lPop.get(1));
            return deserializeValue(result);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * list中push value
     * lpush是从list的头部开始
     * @param key key
     * @param value value
     * @return list长度
     */
    public Long lPush(final K key, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            return jedis.lpush(rawKey, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 取出list中下标从start到end的值
     * @param key key
     * @param start startIndex
     * @param end endIndex
     * @return list<V>
     */
    public List<V> lRange(final K key, final long start, final long end) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            List<byte[]> rawValues = jedis.lrange(rawKey, start, end);
            return deserializeValues(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 移除index后面的所有value的值
     * index可为负数，负数则从后面往前算
     * @param key key
     * @param index index
     * @param value value
     * @return 移除的数
     */
    public Long lRem(final K key, final long index, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            return jedis.lrem(rawKey, index, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 在index后面添加value
     * index可为负数，负数是从后面往前数
     * @param key key
     * @param index index
     * @param value value
     */
    public void lSet(final K key, final long index, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            jedis.lset(rawKey, index, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 留下list里面下标start到end的内容
     * @param key key
     * @param start startIndex
     * @param end endIndex
     */
    public void ltrim(final K key, final long start, final long end) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            jedis.ltrim(rawKey, start, end);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * list中push value
     * rpush是从list的尾部追加
     * @param key key
     * @param value value
     * @return list长度
     */
    public Long rPush(final K key, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            return jedis.rpush(rawKey, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list尾部pop出值
     * @param key key
     * @return V
     */
    public V rPop(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            byte[] result = jedis.rpop(rawKey);
            return deserializeValue(result);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    // Set

    /**
     * set中加value
     * @param key key
     * @param value value
     * @return 状态
     */
    public Long sAdd(final K key, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[][] rawValues = rawValues(value);
        try {
            return jedis.sadd(rawKey, rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 对比key中没有的值
     * @param key key
     * @return key中没有的值
     */
    public Set<V> sDiff(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            Set<byte[]> rawValues = jedis.sdiff(rawKey);
            return deserializeValues(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取set中的所有值
     * @param key key
     * @return set<V>
     */
    public Set<V> sMembers(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            Set<byte[]> rawValues = jedis.smembers(rawKey);
            return deserializeValues(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 检查set中是否存在value
     * @param key key
     * @param value value
     * @return boolean
     */
    public Boolean sIsMember(final K key, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            return jedis.sismember(rawKey, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * set中pop出元素
     * @param key key
     * @return V
     */
    public V sPop(final K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            byte[] result = jedis.spop(rawKey);
            return deserializeValue(result);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 移除set中的value
     * @param key key
     * @param value value
     * @return 状态
     */
    public Long sRem(final K key, final V value) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[][] rawValues = rawValues(value);
        try {
            return jedis.srem(rawKey, rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取set的长度
     * @param key key
     * @return 长度
     */
    public Long sCard(K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            return jedis.scard(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    // SortedSet

    /**
     * sorted中加入value，评分score
     * @param key key
     * @param value value
     * @param score score
     */
    public void zAdd(final K key, final V value, final double score) {

        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        try {
            jedis.zadd(rawKey, score, rawValue);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取sorted set中小标start到end的值
     * @param key key
     * @param start startIndex
     * @param end endIndex
     * @return set<v>
     */
    public Set<V> zRange(final K key, final long start, final long end) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            Set<byte[]> rawValues = jedis.zrange(rawKey, start, end);
            return deserializeValues(rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 移除sorted set中values
     * @param key key
     * @param values values
     * @return 状态
     */
    public Long zRem(final K key, final Object... values) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        final byte[][] rawValues = rawValues(values);
        try {
            return jedis.zrem(rawKey, rawValues);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取sorted set的长度
     * @param key key
     * @return 长度
     */
    public Long zCard(K key) {
        Jedis jedis = jedisSentinelPool.getResource();
        final byte[] rawKey = rawKey(key);
        try {
            return jedis.zcard(rawKey);
        } catch (Exception e) {
            this.returnBrokenResource(jedis, e);
            return null;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 归还jedis
     * @param jedis jedis
     */
    private void returnResource(Jedis jedis) {
        try {
            jedisSentinelPool.returnResource(jedis);
        } catch (Exception e) {
            jedisSentinelPool.returnBrokenResource(jedis);
            logger.warn("Jedis return resource error, " + e.getMessage(), e);
        }
    }

    /**
     * 归还jedis
     * @param jedis jedis
     * @param e
     */
    private void returnBrokenResource(Jedis jedis, Exception e) {
        jedisSentinelPool.returnBrokenResource(jedis);
        logger.error("Jedis operate error, " + e.getMessage(), e);
    }
}
