package com.kong.cloudstack.cache;

import com.kong.cloudstack.dynconfig.DynConfigClient;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * Created by kong on 2016/1/24.
 */
public class RedisRepositoryFactory {
    private static Map<Object, RedisRepository> instanceMap = new ConcurrentHashMap();
    private static Map<Object, RedisManagerRepository> instanceManageMap = new ConcurrentHashMap();

    public RedisRepositoryFactory() {
    }

    public static <K, V> IRedisRepository<K, V> getRepository(Properties properties) {
        RedisRepository redisRepository = (RedisRepository)instanceMap.get(properties);
        if(redisRepository == null) {
            redisRepository = SimpleRedisRepositoryFactory.buildNewRedisRepository(properties);
            instanceMap.put(properties, redisRepository);
        }

        return redisRepository;
    }

    public static <K, V> IRedisRepository<K, V> getRepository(String appName, String group, String dataId) throws Exception {
        DynConfigClient dynConfigClient = DynConfigClientFactory.getClient();
        String redisConfigs = dynConfigClient.getConfig(appName, group, dataId);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(redisConfigs.getBytes("UTF-8")));
        boolean isPool = Boolean.valueOf(properties.getProperty("redis.ispool", "false")).booleanValue();
        return !isPool?getRepository(properties):getRedisManagerRepository(properties);
    }

    public static <K, V> IRedisRepository<K, V> getRedisManagerRepository(Properties properties) throws IOException {
        RedisManagerRepository redisRepository = (RedisManagerRepository)instanceManageMap.get(properties);
        if(redisRepository == null) {
            redisRepository = SimpleRedisRepositoryFactory.buildRedisManagerRepository(properties);
            instanceManageMap.put(properties, redisRepository);
        }

        return redisRepository;
    }
}