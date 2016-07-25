package org.apache.sirona.counters;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by kong on 2016/1/24.
 */
public class Unit implements Comparable<Unit>, Serializable {
    private static final Map<String, Unit> UNITS = new ConcurrentHashMap();
    public static final Unit UNARY = new Unit("u");
    public static final Unit DECA;
    public static final Unit HECTO;
    public static final Unit KILO;
    public static final Unit MEGA;
    public static final Unit GIGA;
    public static final Unit TERA;
    private final String name;
    private final long scale;
    private Unit primary;

    public static Unit get(String name) {
        return (Unit)UNITS.get(name);
    }

    public Unit(String name) {
        this.name = name;
        this.primary = this;
        this.scale = 1L;
        UNITS.put(name, this);
    }

    public Unit(String name, Unit derived, long scale) {
        this.name = name;
        this.primary = derived.isPrimary()?derived:derived.getPrimary();
        this.scale = scale * derived.getScale();
        UNITS.put(name, this);
    }

    public String getName() {
        return this.name;
    }

    public long getScale() {
        return this.scale;
    }

    public double convert(double value, Unit unit) {
        if(unit == this) {
            return value;
        } else if(!this.isCompatible(unit)) {
            throw new IllegalArgumentException("unit " + this.name + " is incompatible with unit " + unit.name);
        } else {
            return value * (double)unit.getScale() / (double)this.scale;
        }
    }

    public boolean isPrimary() {
        return this.primary == this;
    }

    public boolean isCompatible(Unit unit) {
        return this.primary == unit.getPrimary();
    }

    public Unit getPrimary() {
        return this.primary;
    }

    public int compareTo(Unit o) {
        return this.scale < o.scale?-1:1;
    }

    public String toString() {
        return this.name;
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + (this.name == null?0:this.name.hashCode());
        return result1;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            Unit other = (Unit)obj;
            if(this.name == null) {
                if(other.name != null) {
                    return false;
                }
            } else if(!this.name.equals(other.name)) {
                return false;
            }

            return true;
        }
    }

    static {
        DECA = new Unit("*10", UNARY, 10L);
        HECTO = new Unit("*100", DECA, 10L);
        KILO = new Unit("*1000", HECTO, 10L);
        MEGA = new Unit("*10^6", KILO, 1000L);
        GIGA = new Unit("*10^9", MEGA, 1000L);
        TERA = new Unit("*10^12", GIGA, 1000L);
    }

    public static class Binary extends Unit {
        public static final Unit BYTE = new Unit("b");
        public static final Unit KBYTE;
        public static final Unit MBYTE;
        public static final Unit GBYTE;

        public Binary(String name) {
            super(name);
        }

        public Binary(String name, Unit derived, long scale) {
            super(name, derived, scale);
        }

        static {
            KBYTE = new Unit("Kb", BYTE, 1024L);
            MBYTE = new Unit("Mb", KBYTE, 1024L);
            GBYTE = new Unit("Gb", MBYTE, 1024L);
        }
    }

    public static class Time extends Unit {
        public static final Unit NANOSECOND = new Unit("ns");
        public static final Unit MICROSECOND;
        public static final Unit MILLISECOND;
        public static final Unit SECOND;
        public static final Unit MINUTE;
        public static final Unit HOUR;
        public static final Unit DAY;

        public Time(String name) {
            super(name);
        }

        public Time(String name, Unit derived, long scale) {
            super(name, derived, scale);
        }

        static {
            MICROSECOND = new Unit("us", NANOSECOND, 1000L);
            MILLISECOND = new Unit("ms", MICROSECOND, 1000L);
            SECOND = new Unit("s", MILLISECOND, 1000L);
            MINUTE = new Unit("min", SECOND, 60L);
            HOUR = new Unit("h", MINUTE, 60L);
            DAY = new Unit("day", HOUR, 24L);
        }
    }
}