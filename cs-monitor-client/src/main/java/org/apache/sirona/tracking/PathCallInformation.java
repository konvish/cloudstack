package org.apache.sirona.tracking;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
/**
 * Created by kong on 2016/1/24.
 */
public class PathCallInformation implements Serializable {
    private static final long serialVersionUID = 4L;
    private String trackingId;
    private Date startTime;
    public static final Comparator<PathCallInformation> COMPARATOR = new Comparator() {
//        @Override
//        public int compare(Object o1, Object o2) {
//            return 0;
//        }

        public int compare(PathCallInformation o1, PathCallInformation o2) {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    };

    public PathCallInformation(String trackingId, Date startTime) {
        this.trackingId = trackingId;
        this.startTime = startTime;
    }

    public String getTrackingId() {
        return this.trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String toString() {
        return "PathCallInformation{trackingId=\'" + this.trackingId + '\'' + ", startTime=" + this.startTime + '}';
    }
}