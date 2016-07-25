package org.apache.sirona.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.aop.AbstractPerformanceInterceptor.SerializableMethod;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.util.ClassLoaders;

public final class SironaProxyFactory {
    public static <T> T monitor(Class<T> clazz, Object instance) {
        return clazz.cast(((ProxyFactory)IoCs.findOrCreateInstance(ProxyFactory.class)).createInvokerProxy(ClassLoaders.current(), new SironaProxyFactory.SironaPerformanceHandler(instance), new Class[]{clazz}));
    }

    private SironaProxyFactory() {
    }

    private static class Invocation {
        private final Object target;
        private final Method method;
        private final Object[] args;

        private Invocation(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }
    }

    private static class SironaPerformanceHandler extends AbstractPerformanceInterceptor<SironaProxyFactory.Invocation> implements Invoker {
        private final Object instance;

        public SironaPerformanceHandler(Object instance) {
            this.instance = instance;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return this.doInvoke(new SironaProxyFactory.Invocation(this.instance, method, args));
        }

        protected Object proceed(SironaProxyFactory.Invocation invocation) throws Throwable {
            try {
                return invocation.method.invoke(invocation.target, invocation.args);
            } catch (InvocationTargetException var3) {
                throw var3.getCause();
            }
        }

        protected String getCounterName(SironaProxyFactory.Invocation invocation) {
            return this.getCounterName(invocation.target, invocation.method);
        }

        protected Object extractContextKey(SironaProxyFactory.Invocation invocation) {
            return new SerializableMethod(invocation.method);
        }
    }
}