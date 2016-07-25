package org.apache.sirona.store.status;

import java.util.Map;
import org.apache.sirona.status.NodeStatus;
/**
 * Created by kong on 2016/1/24.
 */
public interface NodeStatusDataStore {
    Map<String, NodeStatus> statuses();

    void reset();
}
