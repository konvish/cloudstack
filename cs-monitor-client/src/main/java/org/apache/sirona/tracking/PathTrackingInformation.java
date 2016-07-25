package org.apache.sirona.tracking;

/**
 * Created by kong on 2016/1/24.
 */
public class PathTrackingInformation {
    private String className;
    private String methodName;
    private PathTrackingInformation parent;
    private long start;
    private long end;
    private int level;

    public PathTrackingInformation(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public PathTrackingInformation getParent() {
        return this.parent;
    }

    public void setParent(PathTrackingInformation parent) {
        this.parent = parent;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        return "PathTrackingInformation{className=\'" + this.className + "\', methodName=\'" + this.methodName + "\', parent=" + this.parent + '}';
    }
}