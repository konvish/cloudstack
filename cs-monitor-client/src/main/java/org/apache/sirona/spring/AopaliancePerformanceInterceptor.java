package org.apache.sirona.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.aop.AbstractPerformanceInterceptor.SerializableMethod;
import org.apache.sirona.spring.MonitorMethod;
/**
 * Created by kong on 2016/1/24.
 */
public class AopaliancePerformanceInterceptor extends AbstractPerformanceInterceptor<MethodInvocation> implements MethodInterceptor {
    public AopaliancePerformanceInterceptor() {
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        MonitorMethod monitorMethod = (MonitorMethod)invocation.getMethod().getAnnotation(MonitorMethod.class);
        monitorMethod.test();
        invocation.getMethod().getAnnotations();
        return this.doInvoke(invocation);
    }

    protected String getCounterName(MethodInvocation invocation) {
        return this.getCounterName(invocation.getThis(), invocation.getMethod());
    }

    protected Object proceed(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }

    protected Object extractContextKey(MethodInvocation invocation) {
        return new SerializableMethod(invocation.getMethod());
    }
}

