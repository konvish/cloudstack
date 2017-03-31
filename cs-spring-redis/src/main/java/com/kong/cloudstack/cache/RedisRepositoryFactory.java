package com.kong.cloudstack.cache;

import com.kong.cloudstack.dynconfig.DynConfigClient;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
/**
 * redis接口操作的工厂类
 * Created by kong on 2016/1/24.
 */
public class RedisRepositoryFactory {
    private static Map<Object, RedisRepository> instanceMap = new ConcurrentHashMap<Object, RedisRepository>();
    private static Map<Object, RedisManagerRepository> instanceManageMap = new ConcurrentHashMap<Object, RedisManagerRepository>();

    public RedisRepositoryFactory() {
    }

    /**
     * 获得redis操作接口
     * @param properties 配置文件
     * @param <K> K
     * @param <V> V
     * @return redis操作接口
     */
    public static <K, V> IRedisRepository<K, V> getRepository(Properties properties) {
        RedisRepository redisRepository = instanceMap.get(properties);
        if(redisRepository == null) {
            redisRepository = SimpleRedisRepositoryFactory.buildNewRedisRepository(properties);
            instanceMap.put(properties, redisRepository);
        }

        return redisRepository;
    }

    /**
     * 根据app名称，组别，dataId获取redis操作类
     * @param appName app名称
     * @param group 组别
     * @param dataId dataId
     * @param <K> K
     * @param <V> V
     * @return IRedisRepository
     * @throws Exception
     */
    public static <K, V> IRedisRepository<K, V> getRepository(String appName, String group, String dataId) throws Exception {
        DynConfigClient dynConfigClient = DynConfigClientFactory.getClient();
        String redisConfigs = dynConfigClient.getConfig(appName, group, dataId);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(redisConfigs.getBytes("UTF-8")));
        boolean isPool = Boolean.valueOf(properties.getProperty("redis.ispool", "false"));
        return !isPool?getRepository(properties):getRedisManagerRepository(properties);
    }

    /**
     * 根据配置文件获取redis操作接口
     * @param properties properties
     * @param <K> K
     * @param <V> V
     * @return IRedisRepository
     * @throws IOException
     */
    public static <K, V> IRedisRepository<K, V> getRedisManagerRepository(Properties properties) throws IOException {
        RedisManagerRepository redisRepository = (RedisManagerRepository)instanceManageMap.get(properties);
        if(redisRepository == null) {
            redisRepository = SimpleRedisRepositoryFactory.buildRedisManagerRepository(properties);
            instanceManageMap.put(properties, redisRepository);
        }

        return redisRepository;
    }
}