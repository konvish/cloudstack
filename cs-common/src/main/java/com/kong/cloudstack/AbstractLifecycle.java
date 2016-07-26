package com.kong.cloudstack;

/**
 * 生命周期抽象类
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

    /**
     * 进行实际的启动操作
     */
    protected abstract void doStart();
}
