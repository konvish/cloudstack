package com.kong.cloudstack.cache;

/**
 * Created by kong on 2016/1/24.
 */
import com.kong.cloudstack.cache.IRedisRepository;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Repository("redisManagerRepository")
public class RedisManagerRepository<K, V> implements IRedisRepository<K, V> {
    private Logger logger = LoggerFactory.getLogger(RedisManagerRepository.class);
    private RedisSerializer keySerializer = new StringRedisSerializer();
    private RedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
    private RedisSerializer hashKeySerializer = new StringRedisSerializer();
    private RedisSerializer hashValueSerializer = new StringRedisSerializer();
    private JedisSentinelPool jedisPool;

    public RedisManagerRepository() {
    }

    public JedisSentinelPool getJedisPool() {
        return this.jedisPool;
    }

    public void setJedisPool(JedisSentinelPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public RedisSerializer getKeySerializer() {
        return this.keySerializer;
    }

    public void setKeySerializer(RedisSerializer keySerializer) {
        this.keySerializer = keySerializer;
    }

    public RedisSerializer getValueSerializer() {
        return this.valueSerializer;
    }

    public void setValueSerializer(RedisSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public RedisSerializer getHashKeySerializer() {
        return this.hashKeySerializer;
    }

    public void setHashKeySerializer(RedisSerializer hashKeySerializer) {
        this.hashKeySerializer = hashKeySerializer;
    }

    public RedisSerializer getHashValueSerializer() {
        return this.hashValueSerializer;
    }

    public void setHashValueSerializer(RedisSerializer hashValueSerializer) {
        this.hashValueSerializer = hashValueSerializer;
    }

    private byte[] rawKey(Object key) {
        Assert.notNull(key, "non null key required");
        return this.keySerializer == null && key instanceof byte[]?(byte[])((byte[])key):this.keySerializer.serialize(key);
    }

    <T> List<T> deserializeHashValues(List<byte[]> rawValues) {
        return this.hashValueSerializer == null?rawValues:SerializationUtils.deserialize(rawValues, this.hashValueSerializer);
    }

    List<V> deserializeValues(List<byte[]> rawValues) {
        return this.valueSerializer == null?rawValues:SerializationUtils.deserialize(rawValues, this.valueSerializer);
    }

    private byte[][] rawKeys(Collection<K> keys) {
        byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;

        Object key;
        for(Iterator i$ = keys.iterator(); i$.hasNext(); rawKeys[i++] = this.rawKey(key)) {
            key = i$.next();
        }

        return rawKeys;
    }

    byte[] rawValue(Object value) {
        return this.valueSerializer == null && value instanceof byte[]?(byte[])((byte[])value):this.valueSerializer.serialize(value);
    }

    <HK> byte[][] rawHashKeys(HK... hashKeys) {
        byte[][] rawHashKeys = new byte[hashKeys.length][];
        int i = 0;
        Object[] arr$ = hashKeys;
        int len$ = hashKeys.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Object hashKey = arr$[i$];
            rawHashKeys[i++] = this.rawHashKey(hashKey);
        }

        return rawHashKeys;
    }

    <HK> byte[] rawHashKey(HK hashKey) {
        Assert.notNull(hashKey, "non null hash key required");
        return this.hashKeySerializer == null && hashKey instanceof byte[]?(byte[])((byte[])hashKey):this.hashKeySerializer.serialize(hashKey);
    }

    private V deserializeValue(byte[] value) {
        return this.valueSerializer == null?value:this.valueSerializer.deserialize(value);
    }

    <HK, HV> Map<HK, HV> deserializeHashMap(Map<byte[], byte[]> entries) {
        if(entries == null) {
            return null;
        } else {
            LinkedHashMap map = new LinkedHashMap(entries.size());
            Iterator i$ = entries.entrySet().iterator();

            while(i$.hasNext()) {
                Entry entry = (Entry)i$.next();
                map.put(this.deserializeHashKey((byte[])entry.getKey()), this.deserializeHashValue((byte[])entry.getValue()));
            }

            return map;
        }
    }

    <HK> HK deserializeHashKey(byte[] value) {
        return this.hashKeySerializer == null?value:this.hashKeySerializer.deserialize(value);
    }

    <HV> HV deserializeHashValue(byte[] value) {
        return this.hashValueSerializer == null?value:this.hashValueSerializer.deserialize(value);
    }

    Set<V> deserializeValues(Set<byte[]> rawValues) {
        return this.valueSerializer == null?rawValues:SerializationUtils.deserialize(rawValues, this.valueSerializer);
    }

    byte[][] rawValues(Object... values) {
        byte[][] rawValues = new byte[values.length][];
        int i = 0;
        Object[] arr$ = values;
        int len$ = values.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Object value = arr$[i$];
            rawValues[i++] = this.rawValue(value);
        }

        return rawValues;
    }

    <HV> byte[] rawHashValue(HV value) {
        return this.hashValueSerializer == null & value instanceof byte[]?(byte[])((byte[])value):this.hashValueSerializer.serialize(value);
    }

    <T> Set<T> deserializeHashKeys(Set<byte[]> rawKeys) {
        return this.hashKeySerializer == null?rawKeys:SerializationUtils.deserialize(rawKeys, this.hashKeySerializer);
    }

    public void del(K key) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        try {
            jedis.del(rawKey);
        } catch (Exception var8) {
            this.returnBrokenResource(jedis, var8);
        } finally {
            this.returnResource(jedis);
        }

    }

    public void del(Collection<K> keys) {
        Jedis jedis = this.jedisPool.getResource();

        try {
            if(CollectionUtils.isEmpty(keys)) {
                return;
            }

            byte[][] e = this.rawKeys(keys);
            jedis.del(e);
        } catch (Exception var7) {
            this.returnBrokenResource(jedis, var7);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Boolean exists(K key) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        Boolean var5;
        try {
            Boolean e = jedis.exists(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = Boolean.valueOf(true);
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Boolean expire(K key, long timeout, TimeUnit unit) {
        byte[] rawKey = this.rawKey(key);
        long rawTimeout = TimeoutUtils.toMillis(timeout, unit);
        Jedis jedis = this.jedisPool.getResource();

        Boolean var10;
        try {
            Boolean e;
            if(rawTimeout <= 2147483647L) {
                e = JedisConverters.toBoolean(jedis.pexpire(rawKey, rawTimeout));
                return e;
            }

            e = JedisConverters.toBoolean(jedis.pexpireAt(rawKey, this.time(jedis).longValue() + rawTimeout));
            return e;
        } catch (Exception var14) {
            this.logger.error("", var14);
            var10 = JedisConverters.toBoolean(jedis.expire(rawKey, (int)TimeoutUtils.toSeconds(timeout, unit)));
        } finally {
            this.returnResource(jedis);
        }

        return var10;
    }

    public Long time(Jedis jedis) {
        List serverTimeInformation = jedis.time();
        Assert.notEmpty(serverTimeInformation, "Received invalid result from server. Expected 2 items in collection.");
        Assert.isTrue(serverTimeInformation.size() == 2, "Received invalid nr of arguments from redis server. Expected 2 received " + serverTimeInformation.size());
        return Converters.toTimeMillis((String)serverTimeInformation.get(0), (String)serverTimeInformation.get(1));
    }

    public void expireAt(K key, Date date) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        try {
            jedis.pexpireAt(rawKey, date.getTime());
        } catch (Exception var9) {
            jedis.expireAt(rawKey, date.getTime() / 1000L);
            this.returnBrokenResource(jedis, var9);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Set<K> keys(K pattern) {
        byte[] rawKey = this.rawKey(pattern);
        Jedis jedis = this.jedisPool.getResource();

        Set var5;
        try {
            Set e = jedis.keys(rawKey);
            var5 = this.keySerializer != null?SerializationUtils.deserialize(e, this.keySerializer):e;
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public String type(K key) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        Object var5;
        try {
            String e = jedis.type(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (String)var5;
    }

    public V get(K key) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        Object var5;
        try {
            Object e = this.deserializeValue(jedis.get(rawKey));
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public V getSet(K key, V value) {
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);
        Jedis jedis = this.jedisPool.getResource();

        Object var7;
        try {
            Object e = this.deserializeValue(jedis.getSet(rawKey, rawValue));
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var7;
    }

    public Long incr(K key, long delta) {
        byte[] rawKey = this.rawKey(key);
        Jedis jedis = this.jedisPool.getResource();

        Object var7;
        try {
            Long e = jedis.incrBy(rawKey, delta);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public void set(K key, V value) {
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);
        Jedis jedis = this.jedisPool.getResource();

        try {
            jedis.set(rawKey, rawValue);
        } catch (Exception var10) {
            this.returnBrokenResource(jedis, var10);
        } finally {
            this.returnResource(jedis);
        }

    }

    public void set(K key, V value, long timeout, TimeUnit unit) {
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);
        Jedis jedis = this.jedisPool.getResource();

        try {
            jedis.setex(rawKey, (int)TimeoutUtils.toSeconds(timeout, unit), rawValue);
        } catch (Exception var13) {
            jedis.psetex(rawKey, (int)timeout, rawValue);
            this.returnBrokenResource(jedis, var13);
        } finally {
            this.returnResource(jedis);
        }

    }

    public void hDel(K key, Object... hKeys) {
        byte[] rawKey = this.rawKey(key);
        byte[][] rawHashKeys = this.rawHashKeys(hKeys);
        Jedis jedis = this.jedisPool.getResource();

        try {
            jedis.hdel(rawKey, rawHashKeys);
        } catch (Exception var10) {
            this.returnBrokenResource(jedis, var10);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Boolean hExists(K key, K hKeys) {
        byte[] rawKey = this.rawKey(key);
        byte[] rawHashKey = this.rawHashKey(hKeys);
        Jedis jedis = this.jedisPool.getResource();

        Object var7;
        try {
            Boolean e = jedis.hexists(rawKey, rawHashKey);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Boolean)var7;
    }

    public Map<K, V> hGet(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Map var5;
        try {
            Map e = jedis.hgetAll(rawKey);
            var5 = this.deserializeHashMap(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public V hGet(K key, K hKey) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawHashKey = this.rawHashKey(hKey);

        Object var7;
        try {
            byte[] e = jedis.hget(rawKey, rawHashKey);
            var7 = this.deserializeHashValue(e);
            return var7;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var7;
    }

    public Set<K> hKeys(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Set var5;
        try {
            Set e = jedis.hkeys(rawKey);
            var5 = this.deserializeHashKeys(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Long hLen(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            Long e = jedis.hlen(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var5;
    }

    public void hSet(K key, K hk, V hv) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawHashKey = this.rawHashKey(hk);
        byte[] rawHashValue = this.rawHashValue(hv);

        try {
            jedis.hset(rawKey, rawHashKey, rawHashValue);
        } catch (Exception var12) {
            this.returnBrokenResource(jedis, var12);
        } finally {
            this.returnResource(jedis);
        }

    }

    public void hSet(K key, Map<K, V> map) {
        if(!map.isEmpty()) {
            Jedis jedis = this.jedisPool.getResource();
            byte[] rawKey = this.rawKey(key);

            try {
                LinkedHashMap e = new LinkedHashMap(map.size());
                Iterator i$ = map.entrySet().iterator();

                while(i$.hasNext()) {
                    Entry entry = (Entry)i$.next();
                    e.put(this.rawHashKey(entry.getKey()), this.rawHashValue(entry.getValue()));
                }

                jedis.hmset(rawKey, e);
            } catch (Exception var11) {
                this.returnBrokenResource(jedis, var11);
            } finally {
                this.returnResource(jedis);
            }

        }
    }

    public List<V> hVals(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        List var5;
        try {
            List e = jedis.hvals(rawKey);
            var5 = this.deserializeHashValues(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public V lIndex(K key, long index) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var7;
        try {
            byte[] e = jedis.lindex(rawKey, index);
            var7 = this.deserializeValue(e);
            return var7;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var7;
    }

    public void lInsert(K key, long index, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        try {
            jedis.lset(rawKey, index, rawValue);
        } catch (Exception var12) {
            this.returnBrokenResource(jedis, var12);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Long lLen(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            Long e = jedis.llen(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var5;
    }

    public V lPop(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            byte[] e = jedis.lpop(rawKey);
            var5 = this.deserializeValue(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public V lPop(K key, long timeout, TimeUnit unit) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        int tm = (int)TimeoutUtils.toSeconds(timeout, unit);

        Object result;
        try {
            List e = jedis.blpop(tm, new byte[][]{rawKey});
            byte[] result1 = CollectionUtils.isEmpty(e)?null:(byte[])e.get(1);
            Object var10 = this.deserializeValue(result1);
            return var10;
        } catch (Exception var14) {
            this.returnBrokenResource(jedis, var14);
            result = null;
        } finally {
            this.returnResource(jedis);
        }

        return result;
    }

    public Long lPush(K key, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        Object var7;
        try {
            Long e = jedis.lpush(rawKey, new byte[][]{rawValue});
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public List<V> lRange(K key, long start, long end) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        List var9;
        try {
            List e = jedis.lrange(rawKey, start, end);
            var9 = this.deserializeValues(e);
            return var9;
        } catch (Exception var13) {
            this.returnBrokenResource(jedis, var13);
            var9 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var9;
    }

    public Long lRem(K key, long index, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        Object var9;
        try {
            Long e = jedis.lrem(rawKey, index, rawValue);
            return e;
        } catch (Exception var13) {
            this.returnBrokenResource(jedis, var13);
            var9 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var9;
    }

    public void lSet(K key, long index, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        try {
            jedis.lset(rawKey, index, rawValue);
        } catch (Exception var12) {
            this.returnBrokenResource(jedis, var12);
        } finally {
            this.returnResource(jedis);
        }

    }

    public void ltrim(K key, long start, long end) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        try {
            jedis.ltrim(rawKey, start, end);
        } catch (Exception var12) {
            this.returnBrokenResource(jedis, var12);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Long rPush(K key, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        Object var7;
        try {
            Long e = jedis.rpush(rawKey, new byte[][]{rawValue});
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public V rPop(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            byte[] e = jedis.rpop(rawKey);
            var5 = this.deserializeValue(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Long sAdd(K key, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[][] rawValues = this.rawValues(new Object[]{value});

        Object var7;
        try {
            Long e = jedis.sadd(rawKey, rawValues);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public Set<V> sDiff(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Set var5;
        try {
            Set e = jedis.sdiff(new byte[][]{rawKey});
            var5 = this.deserializeValues(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Set<V> sMembers(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Set var5;
        try {
            Set e = jedis.smembers(rawKey);
            var5 = this.deserializeValues(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Boolean sIsMember(K key, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        Object var7;
        try {
            Boolean e = jedis.sismember(rawKey, rawValue);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Boolean)var7;
    }

    public V sPop(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            byte[] e = jedis.spop(rawKey);
            var5 = this.deserializeValue(e);
            return var5;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var5;
    }

    public Long sRem(K key, V value) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[][] rawValues = this.rawValues(new Object[]{value});

        Object var7;
        try {
            Long e = jedis.srem(rawKey, rawValues);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public Long sCard(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            Long e = jedis.scard(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var5;
    }

    public void zAdd(K key, V value, double score) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[] rawValue = this.rawValue(value);

        try {
            jedis.zadd(rawKey, score, rawValue);
        } catch (Exception var12) {
            this.returnBrokenResource(jedis, var12);
        } finally {
            this.returnResource(jedis);
        }

    }

    public Set<V> zRange(K key, long start, long end) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Set var9;
        try {
            Set e = jedis.zrange(rawKey, start, end);
            var9 = this.deserializeValues(e);
            return var9;
        } catch (Exception var13) {
            this.returnBrokenResource(jedis, var13);
            var9 = null;
        } finally {
            this.returnResource(jedis);
        }

        return var9;
    }

    public Long zRem(K key, Object... values) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);
        byte[][] rawValues = this.rawValues(values);

        Object var7;
        try {
            Long e = jedis.zrem(rawKey, rawValues);
            return e;
        } catch (Exception var11) {
            this.returnBrokenResource(jedis, var11);
            var7 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var7;
    }

    public Long zCard(K key) {
        Jedis jedis = this.jedisPool.getResource();
        byte[] rawKey = this.rawKey(key);

        Object var5;
        try {
            Long e = jedis.zcard(rawKey);
            return e;
        } catch (Exception var9) {
            this.returnBrokenResource(jedis, var9);
            var5 = null;
        } finally {
            this.returnResource(jedis);
        }

        return (Long)var5;
    }

    private void returnResource(Jedis jedis) {
        try {
            this.jedisPool.returnResource(jedis);
        } catch (Exception var3) {
            this.jedisPool.returnBrokenResource(jedis);
            this.logger.warn("Jedis return resource error, " + var3.getMessage(), var3);
        }

    }

    private void returnBrokenResource(Jedis jedis, Exception e) {
        this.jedisPool.returnBrokenResource(jedis);
        this.logger.error("Jedis operate error, " + e.getMessage(), e);
    }
}