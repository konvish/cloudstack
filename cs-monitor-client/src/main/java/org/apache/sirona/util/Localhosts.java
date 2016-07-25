package org.apache.sirona.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Created by kong on 2016/1/24.
 */
public class Localhosts {
    private static final String value;

    private Localhosts() {
    }

    public static String get() {
        return value;
    }

    static {
        String tmp;
        try {
            tmp = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException var2) {
            tmp = "org/apache/sirona";
        }

        value = tmp;
    }
}
