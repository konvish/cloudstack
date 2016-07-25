package com.kong.cloudstack;

/**
 * Created by kong on 2016/1/22.
 */
public interface ILifecycle {
    void start();

    void stop();

    boolean isStarted();
}
