package org.apache.sirona.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.apache.sirona.tracking.PathTrackingInformation;
/**
 * Created by kong on 2016/1/24.
 */
public class Context {
    private String uuid = "Sirona-" + UUID.randomUUID().toString();
    private AtomicInteger level = new AtomicInteger(0);
    private List<PathTrackingEntry> entries = new ArrayList();
    private PathTrackingInformation pathTrackingInformation;

    protected Context() {
    }

    public String getUuid() {
        return this.uuid;
    }

    public AtomicInteger getLevel() {
        return this.level;
    }

    public List<PathTrackingEntry> getEntries() {
        return this.entries;
    }

    public PathTrackingInformation getPathTrackingInformation() {
        return this.pathTrackingInformation;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setLevel(AtomicInteger level) {
        this.level = level;
    }

    public void setEntries(List<PathTrackingEntry> entries) {
        this.entries = entries;
    }

    public void setPathTrackingInformation(PathTrackingInformation pathTrackingInformation) {
        this.pathTrackingInformation = pathTrackingInformation;
    }
}
