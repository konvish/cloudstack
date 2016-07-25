package com.kong.cloudstack.cmc;

import com.kong.cloudstack.cmc.impl.ZKAliveServer;
import com.kong.cloudstack.dynconfig.IChangeListener;
import java.util.List;
/**
 * Created by Administrator on 2016/1/24.
 */
public interface IClusterManagerClient {
    String FIRST_ADD = "firstadd";

    ZKAliveServer register(String var1);

    ZKAliveServer register(String var1, String var2);

    ZKAliveServer register();

    List<String> getLiveServers(String var1);

    List<String> getLiveServers(String var1, String var2);

    List<String> getLiveServers();

    List<String> getBizSystems();

    String getBizSystemMetadata(String var1);

    String getBizSystemMetadata(String var1, String var2);

    void initListenerServerChange(IChangeListener var1);
}