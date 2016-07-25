package com.kong.cloudstack;

/**
 * Created by kong on 2016/1/22.
 */
public abstract class AbstractLifecycle implements ILifecycle {
    protected volatile boolean isStart = false;

    public AbstractLifecycle() {
    }

    public void start() {
        if(!this.isStart) {
            this.doStart();
            this.isStart = true;
        }

    }

    public boolean isStarted() {
        return this.isStart;
    }

    protected abstract void doStart();
}
