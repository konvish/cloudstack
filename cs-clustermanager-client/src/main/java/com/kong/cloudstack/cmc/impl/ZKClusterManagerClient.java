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
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class ZKClusterManagerClient implements IClusterManagerClient {
    public static final Logger logger = LoggerFactory.getLogger(ZKClusterManagerClient.class);
    private volatile boolean isStart = false;
    private static final String ROOT_PATH_FORMAT = "/servers/%s";
    private static final String CLOUD_ROOT_PATH_FORMAT = "/servers/%s/%s";
    private static final String ROOT_PATH_PREFIX = "/servers";
    private static final String DEFAULT_SERVICE_NAME = "cluster-ip";
    public static final String DEFAULT_DOMAIN_NAME = "mc.zk.thinkjoy.cn";
    private List<String> bizSystems = Lists.newArrayList();
    private Set<String> regedBizSystems = Sets.newHashSet();
    private Set<String> removedBizSystems = Sets.newHashSet();
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
            path = String.format("/servers/%s", new Object[]{CloudContextFactory.getCloudContext().getApplicationName()});
        } else {
            path = String.format("/servers/%s/%s", new Object[]{productCode, CloudContextFactory.getCloudContext().getApplicationName()});
        }

        HashMap serverMetadata = Maps.newHashMap();
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

    private ZKAliveServer innerRegister(CuratorFramework client, String path) {
        ZKAliveServer server = null;

        try {
            server = new ZKAliveServer(client, path, "cluster-ip", "cluster server ip");
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
            path = String.format("/servers/%s", new Object[]{appName});
        } else {
            path = String.format("/servers/%s/%s", new Object[]{productCode, appName});
        }

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(ZKClient.getClient()).basePath(path).serializer(serializer).build();

        try {
            serviceDiscovery.start();
        } catch (Exception var10) {
            logger.error("serviceDiscovery start error!", var10);
        }

        ArrayList servers = Lists.newArrayList();

        try {
            Collection e = serviceDiscovery.queryForInstances("cluster-ip");
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
            List e = DynConfigClientFactory.getClient().getNodes("/servers");
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
            return productCode == null?DynConfigClientFactory.getClient().getConfig(String.format("/servers/%s", new Object[]{appName})):DynConfigClientFactory.getClient().getConfig(String.format("/servers/%s/%s", new Object[]{productCode, appName}));
        } catch (Exception var4) {
            logger.error("getBizSystemMetadata error for [" + appName + "]", var4);
            return null;
        }
    }

    public synchronized void initListenerServerChange(final IChangeListener listener) {
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
                            List newNodes = configuration.getNodes();
                            Iterator datas = ZKClusterManagerClient.this.bizSystems.iterator();

                            while(datas.hasNext()) {
                                String node = (String)datas.next();
                                if(!newNodes.contains(node)) {
                                    ZKClusterManagerClient.this.removedBizSystems.add(node);
                                }
                            }

                            ZKClusterManagerClient.this.bizSystems = newNodes;
                            if(configuration.getPathChildrenCacheEvent() != null) {
                                String[] datas1 = configuration.getPathChildrenCacheEvent().getData().getPath().split("/");
                                if(datas1 != null && datas1.length > 2) {
                                    configuration.setAppName(datas1[2]);
                                }
                            }

                            HashMap datas2 = Maps.newHashMap();
                            if(configuration.getPathChildrenCacheEvent() != null && Type.CHILD_REMOVED == configuration.getPathChildrenCacheEvent().getType()) {
                                datas2.put("firstadd", "remove");
                            } else {
                                datas2.put("firstadd", "add");
                            }

                            configuration.setDatas(datas2);
                            if(Type.CHILD_ADDED != configuration.getPathChildrenCacheEvent().getType()) {
                                listener.receiveConfigInfo(configuration);
                            }

                            ZKClusterManagerClient.this.initListenerServerChange(listener);
                        }
                    });
                }
            });
            this.isAlreadyReg = true;
        }

        String path = null;
        Iterator tempBizSystems = this.removedBizSystems.iterator();

        while(tempBizSystems.hasNext()) {
            String i$ = (String)tempBizSystems.next();
            path = String.format("/servers/%s", new Object[]{i$}) + "/" + "cluster-ip";
            DynConfigClientFactory.getClient().removeListeners(path);
            this.regedBizSystems.remove(path);
            logger.warn("remove server {}", i$);
        }

        List tempBizSystems1 = this.getBizSystems();

        for(Iterator i$1 = tempBizSystems1.iterator(); i$1.hasNext(); this.regedBizSystems.add(path)) {
            String appName = (String)i$1.next();
            path = String.format("/servers/%s", new Object[]{appName}) + "/" + "cluster-ip";
            if(!this.regedBizSystems.contains(path)) {
                logger.warn("register {} listener", path);
                DynConfigClientFactory.getClient().registerListeners(path, new IChangeListener() {
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor();
                    }

                    public void receiveConfigInfo(final Configuration configuration) {
                        this.getExecutor().execute(new Runnable() {
                            public void run() {
                                HashMap datas = Maps.newHashMap();
                                if(configuration.getPathChildrenCacheEvent() != null && configuration.getPathChildrenCacheEvent().getData() != null && configuration.getPathChildrenCacheEvent().getData().getPath() != null) {
                                    String[] splits = configuration.getPathChildrenCacheEvent().getData().getPath().split("/");
                                    if(splits != null && splits.length > 2) {
                                        configuration.setAppName(splits[2]);
                                    }

                                    String changeAddress = (String)((Map)JSON.parseObject(new String(configuration.getPathChildrenCacheEvent().getData().getData()), Map.class)).get("address");
                                    if(configuration.getPathChildrenCacheEvent().getType() == Type.CHILD_ADDED) {
                                        datas.put("add", changeAddress);
                                        configuration.setDatas(datas);
                                        listener.receiveConfigInfo(configuration);
                                    } else if(configuration.getPathChildrenCacheEvent().getType() == Type.CHILD_REMOVED) {
                                        datas.put("remove", changeAddress);
                                        configuration.setDatas(datas);
                                        if(!ClusterManagerClientFactory.createClient().getLiveServers(configuration.getAppName()).contains(changeAddress)) {
                                            listener.receiveConfigInfo(configuration);
                                        } else {
                                            ZKClusterManagerClient.logger.warn("还有相同的节点数据 ，不触发上层处理");
                                        }
                                    } else if(configuration.getPathChildrenCacheEvent().getType() == Type.CHILD_UPDATED) {
                                        datas.put("update", new String(configuration.getPathChildrenCacheEvent().getData().getData()));
                                        configuration.setDatas(datas);
                                        listener.receiveConfigInfo(configuration);
                                    } else {
                                        ZKClusterManagerClient.logger.error("unsupport path child change {}", configuration);
                                    }
                                }

                            }
                        });
                    }
                });
            }
        }

    }
}