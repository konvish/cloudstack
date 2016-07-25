package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.dynconfig.IChangeListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentMap;
/**
 * Created by kong on 2016/1/24.
 */
public class ChangeListenerManager {
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
        String key = group + "_" + dataId;
        this.changeListenerMap.put(key, listener);
    }

    private static class ChangeListenerManagerHolder {
        private static ChangeListenerManager instance = new ChangeListenerManager();

        private ChangeListenerManagerHolder() {
        }
    }
}