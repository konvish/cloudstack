package org.apache.sirona.store.tracking;

import java.util.Collection;
import java.util.Date;
import org.apache.sirona.tracking.PathCallInformation;
import org.apache.sirona.tracking.PathTrackingEntry;
/**
 * Created by kong on 2016/1/24.
 */
public interface PathTrackingDataStore {
    void store(PathTrackingEntry var1);

    void store(Collection<PathTrackingEntry> var1);

    void clearEntries();

    Collection<PathTrackingEntry> retrieve(String var1);

    Collection<PathTrackingEntry> retrieve(String var1, int var2);

    Collection<PathTrackingEntry> retrieve(String var1, String var2, String var3);

    Collection<PathCallInformation> retrieveTrackingIds(Date var1, Date var2);
}