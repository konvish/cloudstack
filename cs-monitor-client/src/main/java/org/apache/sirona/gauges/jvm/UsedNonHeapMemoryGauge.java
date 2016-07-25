package org.apache.sirona.gauges.jvm;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.jvm.BaseMemoryGauge;
/**
 * Created by kong on 2016/1/24.
 */
public class UsedNonHeapMemoryGauge extends BaseMemoryGauge {
    public static final Role USED_NONHEAPMEMORY;

    public UsedNonHeapMemoryGauge() {
    }

    public Role role() {
        return USED_NONHEAPMEMORY;
    }

    public double value() {
        return (double)MEMORY_MX_BEAN.getNonHeapMemoryUsage().getUsed();
    }

    static {
        USED_NONHEAPMEMORY = new Role("Used Non Heap Memory", Unit.UNARY);
    }
}
