package com.kong.cloudstack.cmc;

import com.kong.cloudstack.cmc.IClusterManagerClient;
import com.kong.cloudstack.cmc.impl.ZKClusterManagerClient;

/**
 * Created by kong on 2016/1/24.
 */
public class ClusterManagerClientFactory {
    private ClusterManagerClientFactory() {
    }

    public static IClusterManagerClient createClient() {
        return ClusterManagerClientFactory.ClusterManagerClientFactoryHolder.instance;
    }

    public static IClusterManagerClient createClient(String zkIp) {
        return new ZKClusterManagerClient();
    }

    private static class ClusterManagerClientFactoryHolder {
        private static final IClusterManagerClient instance = new ZKClusterManagerClient();

        private ClusterManagerClientFactoryHolder() {
        }
    }
}