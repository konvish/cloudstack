package org.apache.sirona.aop;

import java.lang.reflect.Method;
/**
 * Created by kong on 2016/1/24.
 */
public interface MonitorNameExtractor {
    String getMonitorName(Object var1, Method var2);
}
