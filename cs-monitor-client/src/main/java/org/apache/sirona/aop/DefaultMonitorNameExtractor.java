package org.apache.sirona.aop;

import java.lang.reflect.Method;
import org.apache.sirona.aop.MonitorNameExtractor;
/**
 * Created by kong on 2016/1/24.
 */
public class DefaultMonitorNameExtractor implements MonitorNameExtractor {
    public static final DefaultMonitorNameExtractor INSTANCE = new DefaultMonitorNameExtractor();

    private DefaultMonitorNameExtractor() {
    }

    public String getMonitorName(Object instance, Method method) {
        return instance == null?method.getDeclaringClass().getName() + "." + method.getName():instance.getClass().getName() + "." + method.getName();
    }
}