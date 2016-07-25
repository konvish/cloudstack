package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.client.zookeeper.ZKClient;
import com.kong.cloudstack.client.zookeeper.ZKClientManager;
import com.kong.cloudstack.client.zookeeper.recover.ZKRecoverUtil;
import com.kong.cloudstack.context.CloudContextFactory;
import com.kong.cloudstack.dynconfig.IChangeListener;
import com.kong.cloudstack.dynconfig.domain.Configuration;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 动态配置中心客户端
 * 获取动态配置信息主动从zk获取，但考虑到zk server故障时的处理 需要本地存储容灾（zk client是否已经实现了该功能）
 * Created by kong on 2016/1/24.
 */
public class DynConfigClient {
    public static final Logger logger = LoggerFactory.getLogger(DynConfigClient.class);
    private volatile boolean isStart = false;
    public static final String PATH_FORMAT = "/configs/%s/%s/%s";
    public static final String CLOUD_PATH_FORMAT = "/configs/%s/%s/%s/%s";
    private List<IChangeListener> listeners;
    private ConcurrentMap<String, PathChildrenCache> pathChildrenCacheMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, Object> recoverDataCache = Maps.newConcurrentMap();
    //指定的zk ip
    private String zkIp = null;

    public DynConfigClient() {
    }

    public DynConfigClient(String zkIp) {
        this.zkIp = zkIp;
    }

    //初始化zookeeper客户端
    public void init() {
        if(!this.isStart) {
            //注册监听器
            this.innerRegisterListeners(ZKClient.getClient());
        }

    }

    //对 指定ip的zk进行相关初始化
    public void init(boolean isMulti) {
        if(!this.isStart && isMulti) {
            this.innerRegisterListeners(ZKClientManager.getClient(this.zkIp));
        }

    }

    /**
     * 从zk server获取配置信息 （主动调用）
     * @param group
     * @param dataId
     * @return
     * @throws Exception   抛出异常，由上层业务处理(一般退出应用，启动失败)
     */
    public String getConfig(String group, String dataId) throws Exception {
        return this.getConfig(CloudContextFactory.getCloudContext().getProductCode(), CloudContextFactory.getCloudContext().getApplicationName(), group, dataId);
    }

    /**
     * 获取特定appName的配置项值
     * @param appName
     * @param group
     * @param dataId
     * @return
     * @throws Exception
     */
    public String getConfig(String appName, String group, String dataId) throws Exception {
        String path = String.format(PATH_FORMAT, new Object[]{appName, group, dataId});
        return this.getConfig(path);
    }

    /**
     *
     * @param productCode 产品线code
     * @param appName
     * @param group
     * @param dataId
     * @return
     * @throws Exception
     */
    public String getConfig(String productCode, String appName, String group, String dataId) throws Exception {
        String path = null;
        if(productCode == null) {
            path = String.format(PATH_FORMAT, new Object[]{appName, group, dataId});
        } else {
            path = String.format(CLOUD_PATH_FORMAT, new Object[]{productCode, appName, group, dataId});
        }

        return this.getConfig(path);
    }

    /**
     * 获取特定路径的配置项值
     * @param path
     * @return
     * @throws Exception
     */
    public final String getConfig(final String path) throws Exception {
        String recoverPath = null;
        if(this.zkIp != null && this.zkIp.trim().length() > 0) {
            recoverPath = "/" + this.zkIp + path;
        } else {
            recoverPath = path;
        }

        Object bytes = null;
        boolean isSucc = false;

        byte[] bytes1;
        try {
            CuratorFramework e = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
            bytes1 = (byte[])e.getData().forPath(path);
            isSucc = true;
        } catch (Exception var6) {
            //从本地获取配置数据
            bytes1 = ZKRecoverUtil.loadRecoverData(recoverPath);
        }

        ZKRecoverUtil.doRecover(bytes1, recoverPath, this.recoverDataCache);
        return bytes1 == null?"":new String(bytes1);
    }

    /**
     * 获取特定路径的子节点
     * @param path
     * @return
     * @throws Exception
     */
    public List<String> getNodes(String path) throws Exception {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        List nodes = (List)client.getChildren().forPath(path);
        return nodes;
    }

    /**
     * 为特定组下特定的配置项注册监听器
     * @param group
     * @param dataId
     * @param listener
     */
    public void registerListeners(final String group,final String dataId,final IChangeListener listener) {
        this.registerListeners(CloudContextFactory.getCloudContext().getApplicationName(), group, dataId, listener);
    }

    /** @deprecated */
    public void registerListeners(String appName, String group, String dataId, IChangeListener listener) {
        String path = String.format(PATH_FORMAT, new Object[]{appName, group, dataId});
        this.doRegisterListeners((String)null, appName, path, group, dataId, listener);
    }


    public void registerListeners(String productCode, String appName, String group, String dataId, IChangeListener listener) {
        String path = null;
        if(productCode == null) {
            path = String.format(PATH_FORMAT, new Object[]{appName, group, dataId});
        } else {
            path = String.format(CLOUD_PATH_FORMAT, new Object[]{productCode, appName, group, dataId});
        }

        this.doRegisterListeners(productCode, appName, path, group, dataId, listener);
    }

    /**
     * 清空该path的所有监听器    目前仅用于 集群发现
     * @param path
     */
    public void removeListeners(String path) {
        //清空此cache的监听器（only one）
        ((PathChildrenCache)this.pathChildrenCacheMap.get(path)).getListenable().clear();
    }

    /**
     * 注册 集群发现path的监听器      目前仅用于 集群发现
     * @param path
     * @param listener
     */
    public void registerListeners(String path, final IChangeListener listener) {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        //使用Curator的NodeCache来做ZNode的监听，不用我们自己实现重复监听
        if(this.pathChildrenCacheMap.get(path) == null) {
            PathChildrenCache cache = new PathChildrenCache(client, path, true);
            this.pathChildrenCacheMap.putIfAbsent(path, cache);
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    List nodes = DynConfigClient.this.getNodes("/servers");
                    Configuration configuration = new Configuration();
                    configuration.setPathChildrenCacheEvent(event);
                    configuration.setNodes(nodes);
                    listener.receiveConfigInfo(configuration);
                }
            });

            try {
                cache.start(StartMode.BUILD_INITIAL_CACHE);
            } catch (Exception var6) {
                logger.error("Start NodeCache error for path: {}, error info: {}", path, var6.getMessage());
            }
        }

    }

    private final void doRegisterListeners(String productCode, String appName, String path, final String group, final String dataId, final IChangeListener listener) {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        String cloud_path = path;

        try {
            if(client.checkExists().forPath(path) == null && productCode != null) {
               cloud_path = String.format(CLOUD_PATH_FORMAT, new Object[]{productCode, appName, group, dataId});
            }
        } catch (Exception var13) {
            logger.error("doRegisterListeners error", var13);
        }

        final String cloud_path_final = cloud_path;

        //使用Curator的NodeCache来做ZNode的监听，不用我们自己实现重复监听
        final NodeCache cache = new NodeCache(client, cloud_path);
        cache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                Object data = null;

                byte[] data1;
                try {
                    data1 = cache.getCurrentData().getData();
                } catch (Exception var3) {
                    DynConfigClient.logger.warn("{} loadRecoverData ", cloud_path_final);
                    data1 = ZKRecoverUtil.loadRecoverData(cloud_path_final);
                }

                //具体的业务处理
                if(data1 != null) {
                    Configuration configuration = new Configuration();
                    configuration.setConfig(new String(data1));
                    configuration.setGroup(group);
                    configuration.setDataId(dataId);
                    //对动态变化的数据进行容灾处理
                    ZKRecoverUtil.doRecover(data1, cloud_path_final, DynConfigClient.this.recoverDataCache);
                    listener.receiveConfigInfo(configuration);
                }

            }
        });

        try {
            cache.start(true);
        } catch (Exception var12) {
            logger.error("Start NodeCache error for path: {}, error info: {}", cloud_path, var12.getMessage());
        }

    }

    /**
     * 原生zk监听器注册
     * @param zkClient
     */
    private void innerRegisterListeners(CuratorFramework zkClient) {
        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                DynConfigClient.logger.info("CuratorFramework state changed: {}", newState);
                if(newState != ConnectionState.CONNECTED && newState == ConnectionState.RECONNECTED) {
                }

            }
        });
        zkClient.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            public void unhandledError(String message, Throwable e) {
                DynConfigClient.logger.info("CuratorFramework unhandledError: {}", message);
            }
        });
    }

    public void setConfig(String path, String value) throws Exception {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        if(client.checkExists().forPath(path) == null) {
            this.createConfig(path, value);
        } else {
            client.setData().forPath(path, value.getBytes("utf-8"));
        }

    }

    public void createConfig(String path, String value) throws Exception {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        client.create().creatingParentsIfNeeded().forPath(path, value.getBytes("utf-8"));
    }

    public void deleteConfig(String path) throws Exception {
        CuratorFramework client = this.zkIp == null?ZKClient.getClient():ZKClientManager.getClient(this.zkIp);
        client.delete().forPath(path);
    }
}