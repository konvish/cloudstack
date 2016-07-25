package org.apache.sirona;

/**
 * Created by kong on 2016/1/24.
 */
public class SironaException extends RuntimeException {
    public SironaException(Throwable e) {
        super(e);
    }

    public SironaException(String s) {
        super(s);
    }

    public SironaException(String s, Throwable throwable) {
        super(s, throwable);
    }
}