package org.apache.sirona.spring;

import java.util.ArrayList;
import java.util.List;
import org.aopalliance.aop.Advice;
import org.apache.sirona.aop.DefaultMonitorNameExtractor;
import org.apache.sirona.aop.MonitorNameExtractor;
import org.apache.sirona.spring.AopaliancePerformanceInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
/**
 * Created by kong on 2016/1/24.
 */
public class PointcutMonitoringAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
    private MonitorNameExtractor monitorNameExtractor;
    private Pointcut pointcut;

    public PointcutMonitoringAutoProxyCreator() {
        this.monitorNameExtractor = DefaultMonitorNameExtractor.INSTANCE;
    }

    protected List<Advisor> findCandidateAdvisors() {
        AopaliancePerformanceInterceptor interceptor = new AopaliancePerformanceInterceptor();
        interceptor.setMonitorNameExtractor(this.monitorNameExtractor);
        PointcutAdvisor adivsor = this.createPointcutAdvisor(interceptor);
        ArrayList adivisors = new ArrayList(1);
        adivisors.add(adivsor);
        return adivisors;
    }

    protected PointcutAdvisor createPointcutAdvisor(Advice advice) {
        return new DefaultPointcutAdvisor(this.pointcut, advice);
    }

    public void setMonitorNameExtractor(MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    public MonitorNameExtractor getMonitorNameExtractor() {
        return this.monitorNameExtractor;
    }
}