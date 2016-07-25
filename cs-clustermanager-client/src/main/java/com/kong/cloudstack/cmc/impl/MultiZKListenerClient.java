package com.kong.cloudstack.cmc.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kong.cloudstack.cmc.ClusterManagerClientFactory;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;
import com.kong.cloudstack.dynconfig.IChangeListener;
import com.kong.cloudstack.dynconfig.domain.Configuration;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
/**
 * Created by kong on 2016/1/24.
 */
public class MultiZKListenerClient {
    public static final Logger logger = LoggerFactory.getLogger(ZKClusterManagerClient.class);
    private volatile boolean isStart = false;
    private static final String ROOT_PATH_FORMAT = "/servers/%s";
    private static final String ROOT_PATH_PREFIX = "/servers";
    private static final String DEFAULT_SERVICE_NAME = "cluster-ip";
    private List<String> bizSystems = Lists.newArrayList();
    private Set<String> regedBizSystems = Sets.newHashSet();
    private Set<String> removedBizSystems = Sets.newHashSet();
    private ConcurrentMap<String, Boolean> isAlreadyRegMap = Maps.newConcurrentMap();

    public MultiZKListenerClient() {
    }

    public static MultiZKListenerClient getInstance() {
        return MultiZKListenerClient.MultiZKListenerClientHolder.instance;
    }

    public void initListenerServerChange(final String zkIp, final IChangeListener listener) {
        if(!((Boolean)this.isAlreadyRegMap.get(zkIp)).booleanValue()) {
            logger.warn("register /servers listener");
            DynConfigClientFactory.getClient().registerListeners("/servers", new IChangeListener() {
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor();
                }

                public void receiveConfigInfo(final Configuration configuration) {
                    this.getExecutor().execute(new Runnable() {
                        public void run() {
                            MultiZKListenerClient.logger.warn("bizsystem change {}", configuration);
                            MultiZKListenerClient.this.removedBizSystems.clear();
                            List newNodes = configuration.getNodes();
                            Iterator datas = MultiZKListenerClient.this.bizSystems.iterator();

                            while(datas.hasNext()) {
                                String node = (String)datas.next();
                                if(!newNodes.contains(node)) {
                                    MultiZKListenerClient.this.removedBizSystems.add(node);
                                }
                            }

                            MultiZKListenerClient.this.bizSystems = newNodes;
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

                            MultiZKListenerClient.this.initListenerServerChange(zkIp, listener);
                        }
                    });
                }
            });
            this.isAlreadyRegMap.putIfAbsent(zkIp, Boolean.valueOf(true));
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

        List tempBizSystems1 = this.getBizSystems(zkIp);

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
                                            MultiZKListenerClient.logger.warn("还有相同的节点数据 ，不触发上层处理");
                                        }
                                    } else if(configuration.getPathChildrenCacheEvent().getType() == Type.CHILD_UPDATED) {
                                        datas.put("update", new String(configuration.getPathChildrenCacheEvent().getData().getData()));
                                        configuration.setDatas(datas);
                                        listener.receiveConfigInfo(configuration);
                                    } else {
                                        MultiZKListenerClient.logger.error("unsupport path child change {}", configuration);
                                    }
                                }

                            }
                        });
                    }
                });
            }
        }

    }

    public List<String> getBizSystems(String zkIp) {
        try {
            List e = DynConfigClientFactory.getClient(zkIp).getNodes("/servers");
            this.bizSystems = e;
            return this.bizSystems;
        } catch (Exception var3) {
            logger.error("getBizSystems error!", var3);
            return this.bizSystems;
        }
    }

    public static class MultiZKListenerClientHolder {
        private static MultiZKListenerClient instance = new MultiZKListenerClient();

        public MultiZKListenerClientHolder() {
        }
    }
}