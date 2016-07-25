package org.apache.sirona.tracking;

import java.util.Comparator;
import org.apache.sirona.tracking.PathTrackingEntry;
/**
 * Created by kong on 2016/1/24.
 */
public class PathTrackingEntryComparator implements Comparator<PathTrackingEntry> {
    public static final PathTrackingEntryComparator INSTANCE = new PathTrackingEntryComparator();

    public PathTrackingEntryComparator() {
    }

    public int compare(PathTrackingEntry pathTrackingEntry, PathTrackingEntry pathTrackingEntry2) {
        return Long.valueOf(pathTrackingEntry.getStartTime()).compareTo(Long.valueOf(pathTrackingEntry2.getStartTime()));
    }
}
