package org.apache.sirona.gauges.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public class CPUGauge implements Gauge {
    public static final Role CPU;
    private static final OperatingSystemMXBean SYSTEM_MX_BEAN;

    public CPUGauge() {
    }

    public Role role() {
        return CPU;
    }

    public double value() {
        return SYSTEM_MX_BEAN.getSystemLoadAverage();
    }

    static {
        CPU = new Role("CPU", Unit.UNARY);
        SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
    }
}
