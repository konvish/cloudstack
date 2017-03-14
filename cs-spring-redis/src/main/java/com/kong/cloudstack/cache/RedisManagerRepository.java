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

    public Long time(Jedis jedis) {

        List<String> serverTimeInformation = jedis.time();

        Assert.notEmpty(serverTimeInformation, "Received invalid result from server. Expected 2 items in collection.");
        Assert.isTrue(serverTimeInformation.size() == 2,
                "Received invalid nr of arguments from redis server. Expected 2 received " + serverTimeInformation.size());

        return Converters.toTimeMillis(serverTimeInformation.get(0), serverTimeInformation.get(1));
    }

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

    public void set(final K key, final V value, final long timeout, final TimeUnit unit) {
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.setex(rawKey, (int) TimeoutUtils.toSeconds(timeout, unit), rawValue);
        } catch (Exception e) {
            jedis.psetex(rawKey, (int) timeout, rawValue);
            this.returnBrokenResource(jedis, e);
        } finally {
            this.returnResource(jedis);
        }
    }

    // Hash
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

    // List

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

    private void returnResource(Jedis jedis) {
        try {
            jedisSentinelPool.returnResource(jedis);
        } catch (Exception e) {
            jedisSentinelPool.returnBrokenResource(jedis);
            logger.warn("Jedis return resource error, " + e.getMessage(), e);
        }
    }

    private void returnBrokenResource(Jedis jedis, Exception e) {
        jedisSentinelPool.returnBrokenResource(jedis);
        logger.error("Jedis operate error, " + e.getMessage(), e);
    }
}
