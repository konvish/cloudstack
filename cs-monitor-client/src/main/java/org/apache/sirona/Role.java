package org.apache.sirona;

import java.io.Serializable;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.counters.Unit.Time;
/**
 * Created by kong on 2016/1/24.
 */
public class Role implements Comparable<Role>, Serializable {
    public static final Role WEB;
    public static final Role JSP;
    public static final Role JDBC;
    public static final Role PERFORMANCES;
    public static final Role FAILURES;
    private final String name;
    private final Unit unit;

    public Role(String name, Unit unit) {
        if(name == null) {
            throw new IllegalArgumentException("A role name is required");
        } else if(unit == null) {
            throw new IllegalArgumentException("A role unit is required");
        } else {
            this.name = name;
            this.unit = unit;
        }
    }

    public String getName() {
        return this.name;
    }

    public Unit getUnit() {
        return this.unit;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            Role role = (Role)Role.class.cast(o);
            return this.name.equals(role.name);
        } else {
            return false;
        }
    }

    public int compareTo(Role o) {
        return this.name.compareTo(o.name);
    }

    public String toString() {
        return "Role{name=\'" + this.name + '\'' + ", unit=" + this.unit + '}';
    }

    static {
        WEB = new Role("web", Time.NANOSECOND);
        JSP = new Role("jsp", Time.NANOSECOND);
        JDBC = new Role("jdbc", Time.NANOSECOND);
        PERFORMANCES = new Role("performances", Time.NANOSECOND);
        FAILURES = new Role("failures", Unit.UNARY);
    }
}
