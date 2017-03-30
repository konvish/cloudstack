package com.kong.cloudstack.client.zookeeper;

import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * zk client管理类
 * Created by kong on 2016/1/22.
 */
public class ZKClientManager {
    private static ConcurrentMap<String, CuratorFramework> zkClientMap = new ConcurrentHashMap<String, CuratorFramework>();

    private ZKClientManager() {
    }

    /**
     * 获取zk客户端
     * @param ip ip
     * @return
     */
    public static CuratorFramework getClient(String ip) {
        if(ip != null && ip.trim().length() != 0) {
            synchronized(ip) {
                if(!zkClientMap.containsKey(ip)) {
                    CuratorFramework client = ZKClient.create(ip);
                    CuratorFramework oldClient = (CuratorFramework)zkClientMap.putIfAbsent(ip, client);
                    if(oldClient != null) {
                        oldClient.close();
                    }

                    return client;
                }
            }

            return (CuratorFramework)zkClientMap.get(ip);
        } else {
            throw new IllegalArgumentException("zk ip not null!");
        }
    }
}
