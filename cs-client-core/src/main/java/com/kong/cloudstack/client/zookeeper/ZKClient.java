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
 * zk客户端
 * Created by kong on 2016/1/22.
 */
public class ZKClient extends AbstractLifecycle {
    public static final Logger logger = LoggerFactory.getLogger(ZKClient.class);
    /** 云管理中心域名 */
    public static final String DEFAULT_DOMAIN_NAME = "mc.zk.kong.cn";
    private static volatile CuratorFramework zkClient = null;

    private ZKClient() {
    }

    protected void doStart() {
        this.isStart = true;
        String ip = null;

        try {
            ip = NetUtil.getIpByDomain(DEFAULT_DOMAIN_NAME);
        } catch (UnknownHostException var3) {
            logger.error("getIpByDomain error!", var3);
            System.exit(-1);
        }

        String url = ip + ":" + ConfigLoader.getInstance().getProperty("zk.port");
        zkClient = CuratorFrameworkFactory.newClient(url, new ExponentialBackoffRetry(1000, 3));
        zkClient.start();
        logger.warn("ZKClient start success!");
    }

    /**
     * 根据ip获取zk client, 可以直接调用该方法，但不建议 请使用 ZKClientManager 调用
     * @param ip
     * @return
     */
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

    /**
     * 获取zk客户端实例（单例）
     * @return
     */
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