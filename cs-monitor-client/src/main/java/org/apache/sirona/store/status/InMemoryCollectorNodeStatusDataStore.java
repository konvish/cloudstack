package org.apache.sirona.store.status;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public class InMemoryCollectorNodeStatusDataStore implements CollectorNodeStatusDataStore {
    private final Map<String, NodeStatus> statuses = new ConcurrentHashMap();

    public InMemoryCollectorNodeStatusDataStore() {
    }

    public Map<String, NodeStatus> statuses() {
        return new TreeMap(this.statuses);
    }

    public void reset() {
        this.statuses.clear();
    }

    public void store(String node, NodeStatus status) {
        this.statuses.put(node, status);
    }
}
