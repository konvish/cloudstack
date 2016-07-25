package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.dynconfig.DynConfigClient;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentMap;
/**
 * Created by kong on 2016/1/24.
 */
public class DynConfigClientFactory {
    private static ConcurrentMap<String, DynConfigClient> dynConfigClientMap = Maps.newConcurrentMap();

    private DynConfigClientFactory() {
    }

    public static DynConfigClient getClient() {
        DynConfigClientFactory.DynConfigClientHolder.instance.init();
        return DynConfigClientFactory.DynConfigClientHolder.instance;
    }

    public static DynConfigClient getClient(String zkIp) {
        synchronized(zkIp) {
            if(dynConfigClientMap.get(zkIp) == null) {
                DynConfigClient client = new DynConfigClient(zkIp);
                client.init(true);
                dynConfigClientMap.put(zkIp, client);
            }

            return (DynConfigClient)dynConfigClientMap.get(zkIp);
        }
    }

    private static class DynConfigClientHolder {
        private static DynConfigClient instance = new DynConfigClient();

        private DynConfigClientHolder() {
        }
    }
}