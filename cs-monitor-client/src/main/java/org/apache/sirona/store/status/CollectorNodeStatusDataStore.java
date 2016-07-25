package org.apache.sirona.store.status;

import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.store.status.NodeStatusDataStore;
/**
 * Created by kong on 2016/1/24.
 */
public interface CollectorNodeStatusDataStore extends NodeStatusDataStore {
    void store(String var1, NodeStatus var2);
}
