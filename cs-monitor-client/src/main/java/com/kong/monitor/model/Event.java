package com.kong.monitor.model;

/**
 * 事件接口
 * Created by kong on 2016/1/22.
 */
public interface Event {
    String SPACE = " ";

    void add(String property, String value);

    /**
     * 数据进行镜像
     * @return
     */
    String snapshot();
}
