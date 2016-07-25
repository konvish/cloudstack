package com.kong.monitor.model;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Random;
/**
 * Created by kong on 2016/1/22.
 */
abstract class Striped64 extends Number {
    static final Striped64.ThreadHashCode threadHashCode = new Striped64.ThreadHashCode();
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    transient volatile Striped64.Cell[] cells;
    transient volatile long base;
    transient volatile int busy;
    private static final Unsafe UNSAFE;
    private static final long baseOffset;
    private static final long busyOffset;

    Striped64() {
    }

    final boolean casBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, baseOffset, cmp, val);
    }

    final boolean casBusy() {
        return UNSAFE.compareAndSwapInt(this, busyOffset, 0, 1);
    }

    abstract long fn(long var1, long var3);

    final void retryUpdate(long x, Striped64.HashCode hc, boolean wasUncontended) {
        int h = hc.code;
        boolean collide = false;

        while(true) {
            Striped64.Cell[] as = this.cells;
            int n;
            long v;
            if(this.cells != null && (n = as.length) > 0) {
                Striped64.Cell a;
                if((a = as[n - 1 & h]) == null) {
                    if(this.busy == 0) {
                        Striped64.Cell var32 = new Striped64.Cell(x);
                        if(this.busy == 0 && this.casBusy()) {
                            boolean var33 = false;

                            try {
                                Striped64.Cell[] rs1 = this.cells;
                                int m;
                                int j;
                                if(this.cells != null && (m = rs1.length) > 0 && rs1[j = m - 1 & h] == null) {
                                    rs1[j] = var32;
                                    var33 = true;
                                }
                            } finally {
                                this.busy = 0;
                            }

                            if(var33) {
                                break;
                            }
                            continue;
                        }
                    }

                    collide = false;
                } else if(!wasUncontended) {
                    wasUncontended = true;
                } else {
                    if(a.cas(v = a.value, this.fn(v, x))) {
                        break;
                    }

                    if(n < NCPU && this.cells == as) {
                        if(!collide) {
                            collide = true;
                        } else if(this.busy == 0 && this.casBusy()) {
                            try {
                                if(this.cells == as) {
                                    Striped64.Cell[] var34 = new Striped64.Cell[n << 1];

                                    for(int var35 = 0; var35 < n; ++var35) {
                                        var34[var35] = as[var35];
                                    }

                                    this.cells = var34;
                                }
                            } finally {
                                this.busy = 0;
                            }

                            collide = false;
                            continue;
                        }
                    } else {
                        collide = false;
                    }
                }

                h ^= h << 13;
                h ^= h >>> 17;
                h ^= h << 5;
            } else if(this.busy == 0 && this.cells == as && this.casBusy()) {
                boolean init = false;

                try {
                    if(this.cells == as) {
                        Striped64.Cell[] rs = new Striped64.Cell[2];
                        rs[h & 1] = new Striped64.Cell(x);
                        this.cells = rs;
                        init = true;
                    }
                } finally {
                    this.busy = 0;
                }

                if(init) {
                    break;
                }
            } else if(this.casBase(v = this.base, this.fn(v, x))) {
                break;
            }
        }

        hc.code = h;
    }

    final void internalReset(long initialValue) {
        Striped64.Cell[] as = this.cells;
        this.base = initialValue;
        if(as != null) {
            int n = as.length;

            for(int i = 0; i < n; ++i) {
                Striped64.Cell a = as[i];
                if(a != null) {
                    a.value = initialValue;
                }
            }
        }

    }

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException var2) {
            try {
                return (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Unsafe run() throws Exception {
                        Class k = Unsafe.class;
                        Field[] arr$ = k.getDeclaredFields();
                        int len$ = arr$.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                            Field f = arr$[i$];
                            f.setAccessible(true);
                            Object x = f.get((Object)null);
                            if(k.isInstance(x)) {
                                return (Unsafe)k.cast(x);
                            }
                        }

                        throw new NoSuchFieldError("the Unsafe");
                    }
                });
            } catch (PrivilegedActionException var1) {
                throw new RuntimeException("Could not initialize intrinsics", var1.getCause());
            }
        }
    }

    static {
        try {
            UNSAFE = getUnsafe();
            Class e = Striped64.class;
            baseOffset = UNSAFE.objectFieldOffset(e.getDeclaredField("base"));
            busyOffset = UNSAFE.objectFieldOffset(e.getDeclaredField("busy"));
        } catch (Exception var1) {
            throw new Error(var1);
        }
    }

    static final class ThreadHashCode extends ThreadLocal<Striped64.HashCode> {
        ThreadHashCode() {
        }

        public Striped64.HashCode initialValue() {
            return new Striped64.HashCode();
        }
    }

    static final class HashCode {
        static final Random rng = new Random();
        int code;

        HashCode() {
            int h = rng.nextInt();
            this.code = h == 0?1:h;
        }
    }

    static final class Cell {
        volatile long p0;
        volatile long p1;
        volatile long p2;
        volatile long p3;
        volatile long p4;
        volatile long p5;
        volatile long p6;
        volatile long value;
        volatile long q0;
        volatile long q1;
        volatile long q2;
        volatile long q3;
        volatile long q4;
        volatile long q5;
        volatile long q6;
        private static final Unsafe UNSAFE;
        private static final long valueOffset;

        Cell(long x) {
            this.value = x;
        }

        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        static {
            try {
                UNSAFE = Striped64.getUnsafe();
                Class e = Striped64.Cell.class;
                valueOffset = UNSAFE.objectFieldOffset(e.getDeclaredField("value"));
            } catch (Exception var1) {
                throw new Error(var1);
            }
        }
    }
}
