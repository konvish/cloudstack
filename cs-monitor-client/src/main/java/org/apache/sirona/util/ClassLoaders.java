package org.apache.sirona.util;

/**
 * Created by kong on 2016/1/24.
 */
public class ClassLoaders {
    public static ClassLoader current() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl != null?tccl:ClassLoaders.class.getClassLoader();
    }

    private ClassLoaders() {
    }
}