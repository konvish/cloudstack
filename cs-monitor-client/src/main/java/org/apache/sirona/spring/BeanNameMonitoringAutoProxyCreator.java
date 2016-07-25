package org.apache.sirona.spring;

import org.apache.sirona.aop.DefaultMonitorNameExtractor;
import org.apache.sirona.aop.MonitorNameExtractor;
import org.apache.sirona.spring.AopaliancePerformanceInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
/**
 * Created by kong on 2016/1/24.
 */
public class BeanNameMonitoringAutoProxyCreator extends BeanNameAutoProxyCreator {
    private MonitorNameExtractor counterNameExtractor;

    public BeanNameMonitoringAutoProxyCreator() {
        this.counterNameExtractor = DefaultMonitorNameExtractor.INSTANCE;
    }

    protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName, TargetSource targetSource) {
        if(super.getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource) != DO_NOT_PROXY) {
            AopaliancePerformanceInterceptor interceptor = new AopaliancePerformanceInterceptor();
            interceptor.setMonitorNameExtractor(this.counterNameExtractor);
            return new Object[]{interceptor};
        } else {
            return DO_NOT_PROXY;
        }
    }

    public void setCounterNameExtractor(MonitorNameExtractor counterNameExtractor) {
        this.counterNameExtractor = counterNameExtractor;
    }

    public MonitorNameExtractor getCounterNameExtractor() {
        return this.counterNameExtractor;
    }
}
