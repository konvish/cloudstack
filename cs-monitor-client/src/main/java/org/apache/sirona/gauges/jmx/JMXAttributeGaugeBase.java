package org.apache.sirona.gauges.jmx;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class JMXAttributeGaugeBase implements Gauge {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();
    private final ObjectName name;
    private final String attribute;
    private final Role role;

    public JMXAttributeGaugeBase(ObjectName name, String attribute, String role, Unit unit) {
        this.name = name;
        this.attribute = attribute;
        this.role = new Role(role, unit);
    }

    public JMXAttributeGaugeBase(ObjectName name, String attribute) {
        this(name, attribute, name.getCanonicalName() + "#" + attribute, Unit.UNARY);
    }

    public Role role() {
        return this.role;
    }

    public double value() {
        try {
            return ((Number)Number.class.cast(SERVER.getAttribute(this.name, this.attribute))).doubleValue();
        } catch (Exception var2) {
            throw new SironaException(var2);
        }
    }
}