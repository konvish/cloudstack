package org.apache.sirona.store.tracking;

import java.util.Collection;
import java.util.Collections;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class AbstractPathTrackingDataStore implements PathTrackingDataStore {
    public AbstractPathTrackingDataStore() {
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId, int number) {
        return Collections.emptyList();
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId, String start, String end) {
        return Collections.emptyList();
    }
}

