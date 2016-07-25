package org.apache.sirona.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
/**
 * Created by kong on 2016/1/24.
 */
public class SerializeUtils {
    private SerializeUtils() {
    }

    public static byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(e);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            return e.toByteArray();
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static <T> T deserialize(byte[] source, final Class<T> tClass) {
        try {
            final ByteArrayInputStream e = new ByteArrayInputStream(source);
            ObjectInputStream ois = new ObjectInputStream(e) {
                protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                    return tClass;
                }
            };
            Object obj = tClass.cast(ois.readObject());
            ois.close();
            return obj;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }
}