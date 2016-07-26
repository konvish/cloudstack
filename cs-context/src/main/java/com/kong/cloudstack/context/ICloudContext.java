package com.kong.cloudstack.context;

/**
 * 被管理server上下文
 * Created by kong on 2016/1/24.
 */
public interface ICloudContext {
    String getApplicationName();

    String getId();

    String getApplicationZhName();

    String getAppType();

    String getOwner();

    String getOwnerContact();

    String getDescription();

    int getPort();

    int getHttpPort();

    /** 产品中文名称 */
    String getProduct();

    /** 产品编码 */
    String getProductCode();
}