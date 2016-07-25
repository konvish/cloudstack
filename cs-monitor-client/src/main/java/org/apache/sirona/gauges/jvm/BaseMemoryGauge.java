package org.apache.sirona.gauges.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class BaseMemoryGauge implements Gauge {
    protected static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    public BaseMemoryGauge() {
    }
}
