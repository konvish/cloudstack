package com.kong.cloudstack.cmc;

import com.kong.cloudstack.cmc.impl.ZKAliveServer;
import com.kong.cloudstack.dynconfig.IChangeListener;
import java.util.List;
/**
 * 集群机器管理client
 * Created by Administrator on 2016/1/24.
 */
public interface IClusterManagerClient {
    String FIRST_ADD = "firstadd";

    /**
     * 将本机注册到云管理中心
     * @param appName 业务系统名称
     */
    ZKAliveServer register(String appName);

    ZKAliveServer register(String var1, String var2);

    /**
     * 注册本机到当前上下文业务系统的服务列表中
     */
    ZKAliveServer register();

    /**
     * 获取某一应用的集群机器列表
     * @param appName
     * @return
     */
    List<String> getLiveServers(String appName);

    List<String> getLiveServers(String var1, String var2);

    /**
     * 获取当前系统的server列表
     * @return
     */
    List<String> getLiveServers();

    /**
     * 获取所有的业务系统名称
     * @return
     */
    List<String> getBizSystems();

    /**
     * 根据业务系统名称获取业务系统元数据描述
     * @param appName
     * @return
     */
    String getBizSystemMetadata(String appName);

    String getBizSystemMetadata(String productCode, String appName);

    /**
     * 监听服务器变化
     * @param listener
     */
    void initListenerServerChange(IChangeListener listener);
}