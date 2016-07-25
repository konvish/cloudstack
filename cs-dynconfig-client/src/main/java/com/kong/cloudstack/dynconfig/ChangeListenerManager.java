package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.dynconfig.IChangeListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentMap;
/**
 * 监听器管理及调度
 * Created by kong on 2016/1/24.
 */
public class ChangeListenerManager {
    /** 注册的监听器集合 <group_dataid(需要确保group和dataid没有“_”字符), value> 目前只支持key有一个监听器 TODO 后续可以考虑有多个（有场景再说） */
    private ConcurrentMap<String, IChangeListener> changeListenerMap;
    public static final String SPLIT = "_";

    private ChangeListenerManager() {
        this.changeListenerMap = Maps.newConcurrentMap();
    }

    public static ChangeListenerManager getInstance() {
        return ChangeListenerManager.ChangeListenerManagerHolder.instance;
    }

    public void addListener(String group, String dataId, IChangeListener listener) {
        Preconditions.checkNotNull(listener);
        String key = group + SPLIT + dataId;
        this.changeListenerMap.put(key, listener);
    }

    private static class ChangeListenerManagerHolder {
        private static ChangeListenerManager instance = new ChangeListenerManager();

        private ChangeListenerManagerHolder() {
        }
    }
}