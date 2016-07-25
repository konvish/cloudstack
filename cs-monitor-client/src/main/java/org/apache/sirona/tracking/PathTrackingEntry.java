package org.apache.sirona.tracking;

import java.io.Serializable;
/**
 * Created by kong on 2016/1/24.
 */
public class PathTrackingEntry implements Serializable {
    private static final long serialVersionUID = 4L;
    private String trackingId;
    private String nodeId;
    private String className;
    private String methodName;
    private long startTime;
    private long executionTime;
    private int level;

    public PathTrackingEntry() {
    }

    public PathTrackingEntry(String trackingId, String nodeId, String className, String methodName, long startTime, long executionTime, int level) {
        this.trackingId = trackingId;
        this.nodeId = nodeId;
        this.className = className;
        this.methodName = methodName;
        this.startTime = startTime;
        this.executionTime = executionTime;
        this.level = level;
    }

    public String getTrackingId() {
        return this.trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getExecutionTime() {
        return this.executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        return "PathTrackingEntry{trackingId=\'" + this.trackingId + '\'' + ", nodeId=\'" + this.nodeId + '\'' + ", className=\'" + this.className + '\'' + ", methodName=\'" + this.methodName + '\'' + ", startTime=" + this.startTime + ", executionTime=" + this.executionTime + ", level=" + this.level + '}';
    }
}
