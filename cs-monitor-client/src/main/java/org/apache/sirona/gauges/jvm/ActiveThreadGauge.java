package org.apache.sirona.gauges.jvm;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public class ActiveThreadGauge implements Gauge {
    public static final Role ACTIVE_THREAD;

    public ActiveThreadGauge() {
    }

    public Role role() {
        return ACTIVE_THREAD;
    }

    public double value() {
        return (double)Thread.activeCount();
    }

    static {
        ACTIVE_THREAD = new Role("Active Thread", Unit.UNARY);
    }
}