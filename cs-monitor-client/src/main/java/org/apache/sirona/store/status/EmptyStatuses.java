package org.apache.sirona.store.status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.store.status.NodeStatusDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class EmptyStatuses implements NodeStatusDataStore {
    private Map<String, NodeStatus> statuses = new ConcurrentHashMap();

    public EmptyStatuses() {
    }

    public Map<String, NodeStatus> statuses() {
        return this.statuses;
    }

    public void reset() {
    }
}