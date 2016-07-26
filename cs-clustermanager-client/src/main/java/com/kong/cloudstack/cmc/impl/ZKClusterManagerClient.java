package com.kong.cloudstack.cmc.impl;

import com.kong.cloudstack.client.zookeeper.ZKClient;
import com.kong.cloudstack.cmc.ClusterManagerClientFactory;
import com.kong.cloudstack.cmc.IClusterManagerClient;
import com.kong.cloudstack.cmc.impl.InstanceDetails;
import com.kong.cloudstack.cmc.impl.ZKAliveServer;
import com.kong.cloudstack.context.CloudContextFactory;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;
import com.kong.cloudstack.dynconfig.IChangeListener;
import com.kong.cloudstack.dynconfig.domain.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 注册(使用zk管理）
 * Created by kong on 2016/1/24.
 */
public class ZKClusterManagerClient implements IClusterManagerClient {
    public static final Logger logger = LoggerFactory.getLogger(ZKClusterManagerClient.class);
    private volatile boolean isStart = false;
    private static final String ROOT_PATH_FORMAT = "/servers/%s";
    private static final String CLOUD_ROOT_PATH_FORMAT = "/servers/%s/%s";
    private static final String ROOT_PATH_PREFIX = "/servers";
    private static final String DEFAULT_SERVICE_NAME = "cluster-ip";
    /** 云管理中心域名 */
    public static final String DEFAULT_DOMAIN_NAME = "mc.zk.kong.cn";

    /** 当前的业务系统列表 */
    private List<String> bizSystems = Lists.newArrayList();
    /** 已经进行了目录监听的业务系统集合 */
    private Set<String> regedBizSystems = Sets.newHashSet();
    /** 被删除的业务系统 */
    private Set<String> removedBizSystems = Sets.newHashSet();
    /** 是否对服务发现的根目录进行了监听 */
    private volatile boolean isAlreadyReg = false;

    public ZKClusterManagerClient() {
    }

    public synchronized ZKAliveServer register(String appName) {
        return this.register((String)null, appName);
    }

    public synchronized ZKAliveServer register(String productCode, String appName) {
        CuratorFramework client = ZKClient.getClient();
        String path = null;
        if(productCode == null) {
            path = String.format(ROOT_PATH_FORMAT, CloudContextFactory.getCloudContext().getApplicationName());
        } else {
            path = String.format(CLOUD_ROOT_PATH_FORMAT, productCode, CloudContextFactory.getCloudContext().getApplicationName());
        }

        HashMap<String,String> serverMetadata = Maps.newHashMap();
        serverMetadata.put("number", CloudContextFactory.getCloudContext().getApplicationName());
        serverMetadata.put("name", CloudContextFactory.getCloudContext().getApplicationZhName());
        serverMetadata.put("owner", CloudContextFactory.getCloudContext().getOwner());
        serverMetadata.put("ownerContact", CloudContextFactory.getCloudContext().getOwnerContact());
        serverMetadata.put("description", CloudContextFactory.getCloudContext().getDescription());
        serverMetadata.put("port", String.valueOf(CloudContextFactory.getCloudContext().getPort()));
        serverMetadata.put("httpPort", String.valueOf(CloudContextFactory.getCloudContext().getHttpPort()));
        serverMetadata.put("httpPort", String.valueOf(CloudContextFactory.getCloudContext().getHttpPort()));
        serverMetadata.put("product", CloudContextFactory.getCloudContext().getProduct());
        serverMetadata.put("productCode", CloudContextFactory.getCloudContext().getProductCode());
        ZKAliveServer server = this.innerRegister(client, path);

        //增加业务系统节点描述
        try {
            Thread.sleep(10L);
            ZKClient.getClient().setData().forPath(path, JSON.toJSONBytes(serverMetadata, new SerializerFeature[]{SerializerFeature.PrettyFormat}));
        } catch (Exception var8) {
            logger.error("register error", var8);
            System.exit(-1);
        }

        return server;
    }

    public ZKAliveServer register() {
        return this.register(CloudContextFactory.getCloudContext().getProductCode(), CloudContextFactory.getCloudContext().getApplicationName());
    }

    /**
     * 把当前服务注册到zk
     * @param client
     */
    private ZKAliveServer innerRegister(CuratorFramework client, String path) {
        ZKAliveServer server = null;

        try {
            server = new ZKAliveServer(client, path, DEFAULT_SERVICE_NAME, "cluster server ip");
            server.start();
        } catch (Exception var5) {
            logger.error("", var5);
            System.exit(-1);
        }

        return server;
    }

    public List<String> getLiveServers(String appName) {
        return this.getLiveServers((String)null, appName);
    }

    public List<String> getLiveServers(String productCode, String appName) {
        JsonInstanceSerializer serializer = new JsonInstanceSerializer(InstanceDetails.class);
        String path = null;
        if(productCode == null) {
            path = String.format(ROOT_PATH_FORMAT, appName);
        } else {
            path = String.format(CLOUD_ROOT_PATH_FORMAT, productCode, appName);
        }

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(ZKClient.getClient()).basePath(path).serializer(serializer).build();

        try {
            serviceDiscovery.start();
        } catch (Exception var10) {
            logger.error("serviceDiscovery start error!", var10);
        }

        ArrayList servers = Lists.newArrayList();

        try {
            Collection e = serviceDiscovery.queryForInstances(DEFAULT_SERVICE_NAME);
            Iterator i$ = e.iterator();

            while(i$.hasNext()) {
                ServiceInstance instance = (ServiceInstance)i$.next();
                servers.add(instance.getAddress());
            }

            serviceDiscovery.close();
        } catch (Exception var11) {
            logger.error("getBizSystems error!", var11);
        }

        return servers;
    }

    public List<String> getLiveServers() {
        return this.getLiveServers(CloudContextFactory.getCloudContext().getApplicationName());
    }

    public List<String> getBizSystems() {
        try {
            List e = DynConfigClientFactory.getClient().getNodes(ROOT_PATH_PREFIX);
            this.bizSystems = e;
            return this.bizSystems;
        } catch (Exception var2) {
            logger.error("getBizSystems error!", var2);
            return this.bizSystems;
        }
    }

    public String getBizSystemMetadata(String appName) {
        return this.getBizSystemMetadata((String)null, appName);
    }

    public String getBizSystemMetadata(String productCode, String appName) {
        try {
            return productCode == null?DynConfigClientFactory.getClient().getConfig(String.format(ROOT_PATH_FORMAT, appName)):DynConfigClientFactory.getClient().getConfig(String.format(CLOUD_ROOT_PATH_FORMAT, productCode, appName));
        } catch (Exception var4) {
            logger.error("getBizSystemMetadata error for [" + appName + "]", var4);
            return null;
        }
    }

    public synchronized void initListenerServerChange(final IChangeListener listener) {
        //先注册根目录
        if(!this.isAlreadyReg) {
            logger.warn("register /servers listener");
            DynConfigClientFactory.getClient().registerListeners("/servers", new IChangeListener() {
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor();
                }

                public void receiveConfigInfo(final Configuration configuration) {
                    this.getExecutor().execute(new Runnable() {
                        public void run() {
                            ZKClusterManagerClient.logger.warn("bizsystem change {}", configuration);
                            ZKClusterManagerClient.this.removedBizSystems.clear();
                            List<String> newNodes = configuration.getNodes();
                            Iterator datas = ZKClusterManagerClient.this.bizSystems.iterator();

                            while(datas.hasNext()) {
                                String node = (String)datas.next();
                                if(!newNodes.contains(node)) {//新的里面不存在表示被删除
                                    ZKClusterManagerClient.this.removedBizSystems.add(node);
                                }
                            }

                            bizSystems = newNodes;
                            if(configuration.getPathChildrenCacheEvent() != null) {
                                //从path获取appName
                                String[] datas1 = configuration.getPathChildrenCacheEvent().getData().getPath().split("/");
                                if(datas1.length > 2) {
                                    configuration.setAppName(datas1[2]);
                                }
                            }

                            //需要触发上层告诉有新应用上线
                            HashMap<String,String> datas2 = Maps.newHashMap();
                            if(configuration.getPathChildrenCacheEvent() != null && Type.CHILD_REMOVED == configuration.getPathChildrenCacheEvent().getType()) {
                                datas2.put("firstadd", "remove");
                            } else {
                                datas2.put("firstadd", "add");
                            }

                            configuration.setDatas(datas2);
                            if(Type.CHILD_ADDED != configuration.getPathChildrenCacheEvent().getType()) {
                                listener.receiveConfigInfo(configuration);
                            }

                            //重新注册
                            initListenerServerChange(listener);
                        }
                    });
                }
            });
            this.isAlreadyReg = true;
        }

        //删除的节点监听器清空，防止内存泄露
        String path = null;
        for(String node : removedBizSystems){
            path = String.format(ROOT_PATH_FORMAT, node) + "/" + DEFAULT_SERVICE_NAME;

            DynConfigClientFactory.getClient().removeListeners(path);
            regedBizSystems.remove(path);

            logger.warn("remove server {}", node);
        }

        //进行节点数据的监听
        List<String> tempBizSystems = getBizSystems();
        for(String appName : tempBizSystems){
            path = String.format(ROOT_PATH_FORMAT, appName) + "/" + DEFAULT_SERVICE_NAME;


            if(!regedBizSystems.contains(path)) {  //之前未监听的才监听
                logger.warn("register {} listener", path);
                DynConfigClientFactory.getClient().registerListeners(path, new IChangeListener() {
                    @Override
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor();
                    }

                    @Override
                    public void receiveConfigInfo(final Configuration configuration) {
                        getExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, String> datas = Maps.newHashMap();
                                if(configuration.getPathChildrenCacheEvent() != null
                                        && configuration.getPathChildrenCacheEvent().getData() != null
                                        && configuration.getPathChildrenCacheEvent().getData().getPath() != null){
                                    String[] splits = configuration.getPathChildrenCacheEvent().getData().getPath().split("/");
                                    if(splits != null && splits.length > 2) {
                                        configuration.setAppName(splits[2]);
                                    }
                                    String changeAddress = (String) JSON.parseObject(new String(configuration.getPathChildrenCacheEvent().getData().getData()), Map.class).get("address");
                                    if(configuration.getPathChildrenCacheEvent().getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                                        datas.put("add", changeAddress);
                                        configuration.setDatas(datas);

                                        //real handle
                                        listener.receiveConfigInfo(configuration);
                                    } else if(configuration.getPathChildrenCacheEvent().getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                                        datas.put("remove", changeAddress);
                                        configuration.setDatas(datas);

                                        //如果 还有相同的节点数据 ，不触发上层处理
                                        if(!ClusterManagerClientFactory.createClient().getLiveServers(configuration.getAppName()).contains(changeAddress)){
                                            //real handle
                                            listener.receiveConfigInfo(configuration);
                                        } else {
                                            logger.warn("还有相同的节点数据 ，不触发上层处理");
                                        }
                                    } else if(configuration.getPathChildrenCacheEvent().getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                                        datas.put("update", new String(configuration.getPathChildrenCacheEvent().getData().getData()));
                                        configuration.setDatas(datas);

                                        //real handle
                                        listener.receiveConfigInfo(configuration);
                                    } else {
                                        logger.error("unsupport path child change {}", configuration);
                                    }
                                }

                            }
                        });
                    }
                });
            }
            regedBizSystems.add(path);
        }
    }
}