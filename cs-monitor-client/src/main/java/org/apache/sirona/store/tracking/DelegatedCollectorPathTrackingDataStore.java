package org.apache.sirona.store.tracking;

import java.util.Collection;
import java.util.Date;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathCallInformation;
import org.apache.sirona.tracking.PathTrackingEntry;
/**
 * Created by kong on 2016/1/24.
 */
public class DelegatedCollectorPathTrackingDataStore implements CollectorPathTrackingDataStore {
    private final PathTrackingDataStore delegatedPathTrackingDataStore = (PathTrackingDataStore)IoCs.findOrCreateInstance(PathTrackingDataStore.class);

    public DelegatedCollectorPathTrackingDataStore() {
    }

    public void store(PathTrackingEntry pathTrackingEntry) {
        this.delegatedPathTrackingDataStore.store(pathTrackingEntry);
    }

    public void store(Collection<PathTrackingEntry> pathTrackingEntries) {
        this.delegatedPathTrackingDataStore.store(pathTrackingEntries);
    }

    public void clearEntries() {
        this.delegatedPathTrackingDataStore.clearEntries();
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId) {
        return this.delegatedPathTrackingDataStore.retrieve(trackingId);
    }

    public Collection<PathCallInformation> retrieveTrackingIds(Date startTime, Date endTime) {
        return this.delegatedPathTrackingDataStore.retrieveTrackingIds(startTime, endTime);
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId, int number) {
        return this.delegatedPathTrackingDataStore.retrieve(trackingId, number);
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId, String start, String end) {
        return this.delegatedPathTrackingDataStore.retrieve(trackingId, start, end);
    }
}

