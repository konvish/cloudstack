package com.kong.cloudstack.cmc;

import com.kong.cloudstack.cmc.impl.ZKClusterManagerClient;

/**
 * ClusterManagerClient工厂类
 * Created by kong on 2016/1/24.
 */
public class ClusterManagerClientFactory {
    private ClusterManagerClientFactory() {
    }

    /**
     * 生成client单例对象
     * @return
     */
    public static IClusterManagerClient createClient() {
        return ClusterManagerClientFactory.ClusterManagerClientFactoryHolder.instance;
    }

    /**
     * 根据 不同的 zkip 获取 不同的 IClusterManagerClient
     * @param zkIp
     * @return
     */
    public static IClusterManagerClient createClient(String zkIp) {
        return new ZKClusterManagerClient();
    }

    private static class ClusterManagerClientFactoryHolder {
        private static final IClusterManagerClient instance = new ZKClusterManagerClient();

        private ClusterManagerClientFactoryHolder() {
        }
    }
}