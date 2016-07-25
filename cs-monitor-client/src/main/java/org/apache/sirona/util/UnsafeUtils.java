package org.apache.sirona.util;

import java.lang.reflect.Field;
import sun.misc.Unsafe;
/**
 * Created by kong on 2016/1/24.
 */
public class UnsafeUtils {
    private static Unsafe UNSAFE;

    private UnsafeUtils() {
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    static {
        try {
            Field e = Unsafe.class.getDeclaredField("theUnsafe");
            e.setAccessible(true);
            UNSAFE = (Unsafe)e.get((Object)null);
        } catch (Exception var1) {
            throw new RuntimeException(var1.getMessage(), var1);
        }
    }
}