package com.kong.cloudstack.context;

/**
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

    String getProduct();

    String getProductCode();
}