package com.kong.cloudstack.client.zookeeper;

import com.kong.cloudstack.AbstractLifecycle;
import com.kong.cloudstack.utils.ConfigLoader;
import com.kong.cloudstack.utils.NetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
/**
 * Created by kong on 2016/1/22.
 */
public class ZKClient extends AbstractLifecycle {
    public static final Logger logger = LoggerFactory.getLogger(ZKClient.class);
    public static final String DEFAULT_DOMAIN_NAME = "mc.zk.kong.cn";
    private static volatile CuratorFramework zkClient = null;

    private ZKClient() {
    }

    protected void doStart() {
        this.isStart = true;
        String ip = null;

        try {
            ip = NetUtil.getIpByDomain("mc.zk.kong.cn");
        } catch (UnknownHostException var3) {
            logger.error("getIpByDomain error!", var3);
            System.exit(-1);
        }

        String url = ip + ":" + ConfigLoader.getInstance().getProperty("zk.port");
        zkClient = CuratorFrameworkFactory.newClient(url, new ExponentialBackoffRetry(1000, 3));
        zkClient.start();
        logger.warn("ZKClient start success!");
    }

    public static CuratorFramework create(String ip) {
        logger.warn(" start conn zk server {} ", ip);
        CuratorFramework newClient = null;
        synchronized(ip) {
            String url = ip + ":" + ConfigLoader.getInstance().getProperty("zk.port");
            newClient = CuratorFrameworkFactory.newClient(url, new ExponentialBackoffRetry(1000, 3));
            newClient.start();
        }

        logger.warn("  conn zk server {} success!", ip);
        return newClient;
    }

    public void stop() {
        if(zkClient != null) {
            zkClient.close();
        }

    }

    public static CuratorFramework getClient() {
        ZKClient.ZKClientHolder.instance.start();
        return zkClient;
    }

    private static class ZKClientHolder {
        private static final ZKClient instance = new ZKClient();

        private ZKClientHolder() {
        }
    }
}