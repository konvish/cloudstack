package com.kong.cloudstack.cmc.impl;

/**
 * Created by kong on 2016/1/24.
 */
public class ZKConfig {
    private String url;
    private String namespace;
    private int retryTime;
    private int timeout;

    public ZKConfig() {
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getRetryTime() {
        return this.retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}