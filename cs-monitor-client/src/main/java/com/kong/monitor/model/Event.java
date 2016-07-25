package com.kong.monitor.model;

/**
 * Created by kong on 2016/1/22.
 */
public interface Event {
    String SPACE = " ";

    void add(String var1, String var2);

    String snapshot();
}
