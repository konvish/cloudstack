package com.kong.cloudstack.context;

import com.kong.cloudstack.ILifecycle;
import com.kong.cloudstack.context.ICloudContext;
import com.kong.cloudstack.context.impl.CloudContextImpl;
/**
 * Created by kong on 2016/1/24.
 */
public class CloudContextFactory {
    public CloudContextFactory() {
    }

    public static ICloudContext getCloudContext() {
        ((ILifecycle)CloudContextFactory.CloudContextHolder.instance).start();
        return CloudContextFactory.CloudContextHolder.instance;
    }

    private static class CloudContextHolder {
        private static final ICloudContext instance = new CloudContextImpl();

        private CloudContextHolder() {
        }
    }
}