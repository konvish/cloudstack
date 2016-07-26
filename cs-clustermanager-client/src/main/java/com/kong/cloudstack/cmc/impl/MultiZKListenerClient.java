package com.kong.cloudstack.cmc.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kong.cloudstack.cmc.ClusterManagerClientFactory;
import com.kong.cloudstack.dynconfig.DynConfigClientFactory;
import com.kong.cloudstack.dynconfig.IChangeListener;
import com.kong.cloudstack.dynconfig.domain.Configuration;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.kong.cloudstack.cmc.IClusterManagerClient.FIRST_ADD;

/**
 * 多zk client监听
 * Created by kong on 2016/1/24.
 */
public class MultiZKListenerClient {
    public static final Logger logger = LoggerFactory.getLogger(ZKClusterManagerClient.class);
    private volatile boolean isStart = false;
    private static final String ROOT_PATH_FORMAT = "/servers/%s";
    private static final String ROOT_PATH_PREFIX = "/servers";
    private static final String DEFAULT_SERVICE_NAME = "cluster-ip";
    /** 当前的业务系统列表 */
    private List<String> bizSystems = Lists.newArrayList();
    /** 已经进行了目录监听的业务系统集合 */
    private Set<String> regedBizSystems = Sets.newHashSet();
    /** 被删除的业务系统 */
    private Set<String> removedBizSystems = Sets.newHashSet();
    /** 是否对服务发现的根目录进行了监听 */
    private ConcurrentMap<String, Boolean> isAlreadyRegMap = Maps.newConcurrentMap();

    public MultiZKListenerClient() {
    }

    public static MultiZKListenerClient getInstance() {
        return MultiZKListenerClient.MultiZKListenerClientHolder.instance;
    }

    /**
     * 对任意的业务 云体系zk进行数据节点监听   除去当前默认的,因为在其他地方已注册
     * @param zkIp
     * @param listener
     */
    public void initListenerServerChange(final String zkIp, final IChangeListener listener) {
        if(!((Boolean)this.isAlreadyRegMap.get(zkIp)).booleanValue()) {
            logger.warn("register /servers listener");
            DynConfigClientFactory.getClient().registerListeners(ROOT_PATH_PREFIX, new IChangeListener() {
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor();
                }

                public void receiveConfigInfo(final Configuration configuration) {
                    this.getExecutor().execute(new Runnable() {
                        public void run() {
                            logger.warn("bizsystem change {}", configuration);
                            removedBizSystems.clear();
                            List newNodes = configuration.getNodes();
                            Iterator datas = bizSystems.iterator();

                            while(datas.hasNext()) {
                                String node = (String)datas.next();
                                if(!newNodes.contains(node)) {
                                    removedBizSystems.add(node);
                                }
                            }

                            bizSystems = newNodes;
                            if(configuration.getPathChildrenCacheEvent() != null) {
                                //从path获取appName
                                String[] datas1 = configuration.getPathChildrenCacheEvent().getData().getPath().split("/");
                                if(datas1 != null && datas1.length > 2) {
                                    configuration.setAppName(datas1[2]);
                                }
                            }

                            //需要触发上层告诉有新应用上线
                            HashMap datas2 = Maps.newHashMap();
                            if(configuration.getPathChildrenCacheEvent() != null && Type.CHILD_REMOVED == configuration.getPathChildrenCacheEvent().getType()) {
                                datas2.put(FIRST_ADD, "remove");
                            } else {
                                datas2.put(FIRST_ADD, "add");
                            }

                            configuration.setDatas(datas2);
                            if(Type.CHILD_ADDED != configuration.getPathChildrenCacheEvent().getType()) {
                                listener.receiveConfigInfo(configuration);
                            }

                            //重新注册
                            initListenerServerChange(zkIp, listener);
                        }
                    });
                }
            });
            this.isAlreadyRegMap.putIfAbsent(zkIp, Boolean.valueOf(true));
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
        List<String> tempBizSystems = this.getBizSystems(zkIp);

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


    public List<String> getBizSystems(String zkIp) {
        try {
            List e = DynConfigClientFactory.getClient(zkIp).getNodes(ROOT_PATH_PREFIX);
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