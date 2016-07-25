package com.kong.cloudstack.cache;

import com.kong.cloudstack.cache.RedisManagerRepository;
import com.kong.cloudstack.cache.RedisRepository;
import com.kong.cloudstack.dynconfig.DynConfigClient;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ShardedJedisPool;
/**
 * Created by kong on 2016/1/24.
 */
public class SimpleRedisRepositoryFactory {
    public SimpleRedisRepositoryFactory() {
    }

    public static RedisRepository buildNewRedisRepository(String appName, String group, String dataId) throws Exception {
        DynConfigClient dynConfigClient = DynConfigClientFactory.getClient();
        String redisConfigs = dynConfigClient.getConfig(appName, group, dataId);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(redisConfigs.getBytes("UTF-8")));
        return buildNewRedisRepository(properties);
    }

    public static RedisRepository buildNewRedisRepository(Properties properties) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Integer.parseInt(properties.getProperty("redis.pool.maxIdle", String.valueOf(jedisPoolConfig.getMaxIdle()))));
        jedisPoolConfig.setMaxTotal(Integer.parseInt(properties.getProperty("redis.pool.maxTotal", String.valueOf(jedisPoolConfig.getMaxTotal()))));
        jedisPoolConfig.setMaxWaitMillis((long)Integer.parseInt(properties.getProperty("redis.pool.maxWaitMillis", String.valueOf(jedisPoolConfig.getMaxWaitMillis()))));
        jedisPoolConfig.setMinEvictableIdleTimeMillis((long)Integer.parseInt(properties.getProperty("redis.pool.minEvictableIdleTimeMillis", String.valueOf(jedisPoolConfig.getMinEvictableIdleTimeMillis()))));
        jedisPoolConfig.setMinIdle(Integer.parseInt(properties.getProperty("redis.pool.minIdle", String.valueOf(jedisPoolConfig.getMinIdle()))));
        jedisPoolConfig.setNumTestsPerEvictionRun(Integer.parseInt(properties.getProperty("redis.pool.numTestsPerEvictionRun", String.valueOf(jedisPoolConfig.getNumTestsPerEvictionRun()))));
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis((long)Integer.parseInt(properties.getProperty("redis.pool.minEvictableIdleTimeMillis", String.valueOf(jedisPoolConfig.getMinEvictableIdleTimeMillis()))));
        jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnBorrow", String.valueOf(jedisPoolConfig.getTestOnBorrow()))));
        jedisPoolConfig.setTestOnReturn(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnReturn", String.valueOf(jedisPoolConfig.getTestOnReturn()))));
        jedisPoolConfig.setTestWhileIdle(Boolean.parseBoolean(properties.getProperty("redis.pool.testWhileIdle", String.valueOf(jedisPoolConfig.getTestWhileIdle()))));
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis((long)Integer.parseInt(properties.getProperty("redis.pool.timeBetweenEvictionRunsMillis", String.valueOf(jedisPoolConfig.getTimeBetweenEvictionRunsMillis()))));
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig);
        jedisConnectionFactory.setDatabase(Integer.parseInt(properties.getProperty("redis.database", String.valueOf(jedisConnectionFactory.getDatabase()))));
        jedisConnectionFactory.setHostName(properties.getProperty("redis.ip", jedisConnectionFactory.getHostName()));
        jedisConnectionFactory.setPassword(properties.getProperty("redis.password", jedisConnectionFactory.getPassword()));
        jedisConnectionFactory.setPort(Integer.parseInt(properties.getProperty("redis.port", String.valueOf(jedisConnectionFactory.getPort()))));
        jedisConnectionFactory.afterPropertiesSet();
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        StringRedisSerializer serializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setHashKeySerializer(serializer);
        redisTemplate.afterPropertiesSet();
        RedisRepository redisRepository = new RedisRepository();
        redisRepository.setRedisTemplate(redisTemplate);
        return redisRepository;
    }

    public static RedisManagerRepository buildRedisManagerRepository(Properties properties) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Integer.parseInt(properties.getProperty("redis.pool.maxIdle", String.valueOf(jedisPoolConfig.getMaxIdle()))));
        jedisPoolConfig.setMaxTotal(Integer.parseInt(properties.getProperty("redis.pool.maxTotal", String.valueOf(jedisPoolConfig.getMaxTotal()))));
        jedisPoolConfig.setMaxWaitMillis((long)Integer.parseInt(properties.getProperty("redis.pool.maxWaitMillis", String.valueOf(jedisPoolConfig.getMaxWaitMillis()))));
        jedisPoolConfig.setMinEvictableIdleTimeMillis((long)Integer.parseInt(properties.getProperty("redis.pool.minEvictableIdleTimeMillis", String.valueOf(jedisPoolConfig.getMinEvictableIdleTimeMillis()))));
        jedisPoolConfig.setMinIdle(Integer.parseInt(properties.getProperty("redis.pool.minIdle", String.valueOf(jedisPoolConfig.getMinIdle()))));
        jedisPoolConfig.setNumTestsPerEvictionRun(Integer.parseInt(properties.getProperty("redis.pool.numTestsPerEvictionRun", String.valueOf(jedisPoolConfig.getNumTestsPerEvictionRun()))));
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis((long)Integer.parseInt(properties.getProperty("redis.pool.minEvictableIdleTimeMillis", String.valueOf(jedisPoolConfig.getMinEvictableIdleTimeMillis()))));
        jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnBorrow", String.valueOf(jedisPoolConfig.getTestOnBorrow()))));
        jedisPoolConfig.setTestOnReturn(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnReturn", String.valueOf(jedisPoolConfig.getTestOnReturn()))));
        jedisPoolConfig.setTestWhileIdle(Boolean.parseBoolean(properties.getProperty("redis.pool.testWhileIdle", String.valueOf(jedisPoolConfig.getTestWhileIdle()))));
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis((long)Integer.parseInt(properties.getProperty("redis.pool.timeBetweenEvictionRunsMillis", String.valueOf(jedisPoolConfig.getTimeBetweenEvictionRunsMillis()))));
        HashSet sentinels = new HashSet();
        String ip = properties.getProperty("redis.ip", "10.172.7.57");
        String port = properties.getProperty("redis.port", "26379");
        int database = Integer.parseInt(properties.getProperty("redis.database", "1"));
        String password = properties.getProperty("redis.password", (String)null);
        password = StringUtils.isEmpty(password)?null:password;
        int exprie = Integer.valueOf(properties.getProperty("redis.exprie", "20000")).intValue();
        String maseterName = properties.getProperty("redis.master", "master-1");
        sentinels.add(ip + ":" + port);
        JedisSentinelPool jedisPool = new JedisSentinelPool(maseterName, sentinels, jedisPoolConfig, exprie, password, database);
        RedisManagerRepository redisManagerRepository = new RedisManagerRepository();
        redisManagerRepository.setJedisPool(jedisPool);
        redisManagerRepository.setKeySerializer(new StringRedisSerializer());
        redisManagerRepository.setValueSerializer(new JdkSerializationRedisSerializer());
        return redisManagerRepository;
    }

    /** @deprecated */
    @Deprecated
    public static RedisRepository buildShardedRedisRepository() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        LinkedList jedisShardInfoList = new LinkedList();
        new ShardedJedisPool(jedisPoolConfig, jedisShardInfoList);
        return null;
    }

    public static void main(String[] args) throws IOException {
        String redisConfigs = "redis.database=0\n redis.timeout=3000\n redis.pool.testOnBorrow=true\n redis.pool.maxActive=40\n redis.pool.maxIdle=5\n redis.pool.maxWait=15000\n redis.ip=localhost\n redis.port=6379\n redis.password=";
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(redisConfigs.getBytes("UTF-8")));
        Iterator i$ = properties.stringPropertyNames().iterator();

        while(i$.hasNext()) {
            String name = (String)i$.next();
            System.out.println(name + " - " + properties.getProperty(name));
        }

    }
}
