package org.apache.sirona.configuration.ioc;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.util.ClassLoaders;
/**
 * Created by kong on 2016/1/24.
 */
public final class IoCs {
    private static final Map<Class<?>, Object> SINGLETONS = new ConcurrentHashMap();
    private static final Collection<IoCs.ToDestroy> INSTANCES = new ArrayList();
    public static final String SETTER_PREFIX = "set";
    private static Thread shutdownHook = null;

    public static <T> T[] newInstances(Class<T> api) {
        String names = Configuration.getProperty(api.getName(), (String)null);
        if(names == null) {
            return (Object[])((Object[])Array.newInstance(api, 0));
        } else {
            String[] split = names.split(",");
            Object[] array = (Object[])((Object[])Array.newInstance(api, split.length));

            for(int i = 0; i < array.length; ++i) {
                try {
                    array[i] = newInstance(api, split[i]);
                } catch (Exception var6) {
                    throw new SironaException(var6);
                }
            }

            return array;
        }
    }

    public static synchronized <T> T findOrCreateInstance(Class<T> clazz) {
        Object t = clazz.cast(SINGLETONS.get(clazz));
        return t != null?t:newInstance(clazz);
    }

    public static synchronized <T> T newInstance(Class<T> clazz) {
        String config = Configuration.getProperty(clazz.getName(), (String)null);

        try {
            if(config == null) {
                if(clazz.isInterface()) {
                    config = clazz.getPackage().getName() + ".Default" + clazz.getSimpleName();
                } else {
                    config = clazz.getName();
                }
            }

            Object e = newInstance(clazz, config);
            SINGLETONS.put(clazz, e);
            return e;
        } catch (Exception var3) {
            throw new SironaException("Cannot find instance for class " + clazz.getName() + " with config : " + config + " : " + var3.getMessage(), var3);
        }
    }

    private static <T> T newInstance(Class<T> clazz, String config) throws Exception {
        Class loadedClass;
        try {
            loadedClass = ClassLoaders.current().loadClass(config);
        } catch (Throwable var4) {
            loadedClass = clazz;
        }

        return clazz.cast(internalProcessInstance(loadedClass.newInstance()));
    }

    public static <T> T processInstance(T instance) {
        try {
            return internalProcessInstance(instance);
        } catch (Exception var2) {
            throw new SironaException(var2);
        }
    }

    private static <T> T internalProcessInstance(T instance) throws Exception {
        Class loadedClass = instance.getClass();
        if(loadedClass.getAnnotation(AutoSet.class) != null) {
            autoSet((String)null, instance, loadedClass);
        }

        for(Class clazz = loadedClass; clazz != null && !Object.class.equals(clazz); clazz = clazz.getSuperclass()) {
            Method[] arr$ = clazz.getDeclaredMethods();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Method m = arr$[i$];
                if(m.getAnnotation(Created.class) != null) {
                    m.setAccessible(true);
                    m.invoke(instance, new Object[0]);
                } else if(m.getAnnotation(Destroying.class) != null) {
                    m.setAccessible(true);
                    if(shutdownHook == null == Configuration.is("org.apache.org.apache.sirona.shutdown.hook", true)) {
                        shutdownHook = new Thread() {
                            public void run() {
                                IoCs.shutdown();
                            }
                        };
                        Runtime.getRuntime().addShutdownHook(shutdownHook);
                    }

                    INSTANCES.add(new IoCs.ToDestroy(m, instance));
                }
            }
        }

        return instance;
    }

    public static <T> T autoSet(T instance) throws Exception {
        return autoSet((String)null, instance);
    }

    public static <T> T autoSet(String key, T instance) throws Exception {
        return autoSet(key, instance, instance.getClass());
    }

    public static <T> T autoSet(String key, T instance, Class<?> loadedClass) throws Exception {
        for(Class current = loadedClass; current != null && !current.isInterface() && !Object.class.equals(current); current = current.getSuperclass()) {
            LinkedList done = new LinkedList();
            Method[] arr$ = current.getDeclaredMethods();
            int len$ = arr$.length;

            int i$;
            String value;
            for(i$ = 0; i$ < len$; ++i$) {
                Method field = arr$[i$];
                if(Void.TYPE.equals(field.getReturnType()) && field.getName().startsWith("set") && field.getParameterTypes().length == 1 && !Modifier.isStatic(field.getModifiers())) {
                    value = Introspector.decapitalize(field.getName().substring(3));
                    String acc;
                    if(key == null) {
                        acc = loadedClass.getName() + "." + value;
                    } else {
                        acc = key + "." + value;
                    }

                    String value1 = Configuration.getProperty(acc, (String)null);
                    if(value1 != null) {
                        done.add(value);
                        boolean acc1 = field.isAccessible();
                        if(!acc1) {
                            field.setAccessible(true);
                        }

                        try {
                            field.invoke(instance, new Object[]{convertTo(field.getParameterTypes()[0], value1)});
                        } finally {
                            if(!acc1) {
                                field.setAccessible(false);
                            }

                        }
                    }
                }
            }

            Field[] var21 = current.getDeclaredFields();
            len$ = var21.length;

            for(i$ = 0; i$ < len$; ++i$) {
                Field var22 = var21[i$];
                if(!Modifier.isFinal(var22.getModifiers()) && !done.contains(var22.getName())) {
                    value = Configuration.getProperty(loadedClass.getName() + "." + var22.getName(), (String)null);
                    if(value != null) {
                        done.add(var22.getName());
                        boolean var23 = var22.isAccessible();
                        if(!var23) {
                            var22.setAccessible(true);
                        }

                        try {
                            var22.set(instance, convertTo(var22.getType(), value));
                        } finally {
                            if(!var23) {
                                var22.setAccessible(false);
                            }

                        }
                    }
                }
            }
        }

        return instance;
    }

    public static void setSingletonInstance(Class<?> clazz, Object instance) {
        SINGLETONS.put(clazz, instance);
    }

    public static <T> T getInstance(Class<T> clazz) {
        return clazz.cast(SINGLETONS.get(clazz));
    }

    public static void shutdown() {
        Iterator i$ = INSTANCES.iterator();

        while(i$.hasNext()) {
            IoCs.ToDestroy c = (IoCs.ToDestroy)i$.next();
            c.destroy();
        }

        INSTANCES.clear();
        SINGLETONS.clear();
    }

    private static Object convertTo(Class<?> type, String value) {
        if(String.class.equals(type)) {
            return value;
        } else if(String[].class.equals(type)) {
            return value.split(",");
        } else if(Integer.TYPE.equals(type)) {
            return Integer.valueOf(Integer.parseInt(value));
        } else if(Long.TYPE.equals(type)) {
            return Long.valueOf(Long.parseLong(value));
        } else if(Boolean.TYPE.equals(type)) {
            return Boolean.valueOf(Boolean.parseBoolean(value));
        } else {
            throw new IllegalArgumentException("Type " + type.getName() + " not supported");
        }
    }

    private IoCs() {
    }

    private static class ToDestroy {
        private final Method method;
        private final Object target;

        public ToDestroy(Method m, Object instance) {
            this.method = m;
            this.target = instance;
        }

        public void destroy() {
            try {
                this.method.invoke(this.target, new Object[0]);
            } catch (Exception var2) {
                ;
            }

        }
    }
}
