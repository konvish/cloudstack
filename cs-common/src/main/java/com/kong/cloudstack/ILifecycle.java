package com.kong.cloudstack;

/**
 * 生命周期管理接口
 * Created by kong on 2016/1/22.
 */
public interface ILifecycle {
    void start();

    void stop();

    boolean isStarted();
}
