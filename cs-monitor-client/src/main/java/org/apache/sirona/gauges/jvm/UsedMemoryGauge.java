package org.apache.sirona.gauges.jvm;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.jvm.BaseMemoryGauge;
/**
 * Created by kong on 2016/1/24.
 */
public class UsedMemoryGauge extends BaseMemoryGauge {
    public static final Role USED_MEMORY;

    public UsedMemoryGauge() {
    }

    public Role role() {
        return USED_MEMORY;
    }

    public double value() {
        return (double)MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed();
    }

    static {
        USED_MEMORY = new Role("Used Memory", Unit.UNARY);
    }
}
