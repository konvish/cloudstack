package com.kong.cloudstack.dynconfig.domain;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * Created by kong on 2016/1/24.
 */
public class Configuration implements Serializable {
    private static final long serialVersionUID = 5578228351252777377L;
    private String group;
    private String dataId;
    private String config;
    private List<String> nodes;
    private PathChildrenCacheEvent pathChildrenCacheEvent;
    private Map<String, String> datas;
    private String appName;

    public Configuration() {
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return this.dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public List<String> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public PathChildrenCacheEvent getPathChildrenCacheEvent() {
        return this.pathChildrenCacheEvent;
    }

    public void setPathChildrenCacheEvent(PathChildrenCacheEvent pathChildrenCacheEvent) {
        this.pathChildrenCacheEvent = pathChildrenCacheEvent;
    }

    public Map<String, String> getDatas() {
        return this.datas;
    }

    public void setDatas(Map<String, String> datas) {
        this.datas = datas;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String toString() {
        return "Configuration{group=\'" + this.group + '\'' + ", dataId=\'" + this.dataId + '\'' + ", config=\'" + this.config + '\'' + ", nodes=" + this.nodes + ", pathChildrenCacheEvent=" + this.pathChildrenCacheEvent + ", datas=" + this.datas + ", appName=\'" + this.appName + '\'' + '}';
    }
}