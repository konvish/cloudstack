package org.apache.sirona.aop;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.aop.DefaultMonitorNameExtractor;
import org.apache.sirona.aop.MonitorNameExtractor;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Counter.Key;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.stopwatches.StopWatch;
/**
 * Created by kong on 2016/1/24.
 */
public abstract class AbstractPerformanceInterceptor<T> implements Serializable {
    private static final boolean ADAPTIVE = Configuration.is("org.apache.org.apache.sirona.performance.adaptive", false);
    private static final long FORCED_ITERATION = (long)Configuration.getInteger("org.apache.org.apache.sirona.performance.forced-iteration", 0);
    private static final long THRESHOLD = duration(Configuration.getProperty("org.apache.org.apache.sirona.performance.threshold", (String)null));
    private static final AbstractPerformanceInterceptor.ActivationContext ALWAYS_ACTIVE_CONTEXT = new AbstractPerformanceInterceptor.ActivationContext(true, 0L, 0L);
    protected static final ConcurrentMap<Object, AbstractPerformanceInterceptor.ActivationContext> CONTEXTS = new ConcurrentHashMap();
    protected MonitorNameExtractor monitorNameExtractor;

    private static long duration(String duration) {
        if(duration == null) {
            return 0L;
        } else {
            String[] parts = duration.split(" ");
            return parts.length == 1?Long.parseLong(duration.trim()):(parts.length == 2?TimeUnit.valueOf(parts[2].trim().toUpperCase(Locale.ENGLISH)).toNanos(Long.parseLong(parts[0].trim())):0L);
        }
    }

    public AbstractPerformanceInterceptor() {
        this.setMonitorNameExtractor(DefaultMonitorNameExtractor.INSTANCE);
    }

    protected Object doInvoke(T invocation) throws Throwable {
        String name = this.getCounterName(invocation);
        if(name == null) {
            return this.proceed(invocation);
        } else {
            AbstractPerformanceInterceptor.Context ctx = this.before(invocation, name);
            Throwable error = null;

            Object t;
            try {
                t = this.proceed(invocation);
            } catch (Throwable var9) {
                error = var9;
                throw var9;
            } finally {
                if(error == null) {
                    ctx.stop();
                } else {
                    ctx.stopWithException(error);
                }

            }

            return t;
        }
    }

    protected AbstractPerformanceInterceptor.Context before(T invocation, String name) {
        AbstractPerformanceInterceptor.ActivationContext context = this.doFindContext(invocation);

        try {
            StopWatch e;
            if(context.shouldExecute()) {
                Repository repository = Repository.INSTANCE;
                if(repository == null) {
                    System.out.println("repository is null");
                }

                Counter monitor = repository.getCounter(this.getKey(invocation, name));
                if(monitor == null) {
                    System.out.println("monitor is null");
                }

                e = Repository.INSTANCE.start(monitor);
            } else {
                e = null;
            }

            return this.newContext(invocation, context, e);
        } catch (Exception var7) {
            return this.newContext(invocation, context, new StopWatch() {
                public long getElapsedTime() {
                    return 0L;
                }

                public StopWatch stop() {
                    return this;
                }
            });
        }
    }

    protected AbstractPerformanceInterceptor.Context newContext(T invocation, AbstractPerformanceInterceptor.ActivationContext context, StopWatch stopwatch) {
        return new AbstractPerformanceInterceptor.Context(context, stopwatch);
    }

    protected Key getKey(T invocation, String name) {
        return new Key(this.getRole(), name);
    }

    protected boolean isAdaptive() {
        return ADAPTIVE;
    }

    protected Object extractContextKey(T invocation) {
        return null;
    }

    protected AbstractPerformanceInterceptor.ActivationContext getOrCreateContext(Object m) {
        AbstractPerformanceInterceptor.ActivationContext c = (AbstractPerformanceInterceptor.ActivationContext)CONTEXTS.get(m);
        if(c == null) {
            String counterName;
            if(AbstractPerformanceInterceptor.SerializableMethod.class.isInstance(m)) {
                counterName = this.getCounterName((Object)null, ((AbstractPerformanceInterceptor.SerializableMethod)AbstractPerformanceInterceptor.SerializableMethod.class.cast(m)).method());
            } else {
                counterName = m.toString();
            }

            return this.putAndGetActivationContext(m, new AbstractPerformanceInterceptor.ActivationContext(true, counterName));
        } else {
            return c;
        }
    }

    protected AbstractPerformanceInterceptor.ActivationContext putAndGetActivationContext(Object m, AbstractPerformanceInterceptor.ActivationContext newCtx) {
        AbstractPerformanceInterceptor.ActivationContext old = (AbstractPerformanceInterceptor.ActivationContext)CONTEXTS.putIfAbsent(m, newCtx);
        if(old != null) {
            newCtx = old;
        }

        return newCtx;
    }

    protected AbstractPerformanceInterceptor.ActivationContext doFindContext(T invocation) {
        if(!this.isAdaptive()) {
            return ALWAYS_ACTIVE_CONTEXT;
        } else {
            Object m = this.extractContextKey(invocation);
            return m != null?this.getOrCreateContext(m):ALWAYS_ACTIVE_CONTEXT;
        }
    }

    protected Role getRole() {
        return Role.PERFORMANCES;
    }

    protected abstract Object proceed(T var1) throws Throwable;

    protected abstract String getCounterName(T var1);

    protected String getCounterName(Object instance, Method method) {
        return this.monitorNameExtractor.getMonitorName(instance, method);
    }

    public void setMonitorNameExtractor(MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }

    protected static class ActivationContext implements Serializable {
        protected final long forceIteration;
        protected final long threshold;
        protected final boolean thresholdActive;
        protected volatile boolean active;
        protected volatile AtomicInteger iteration;

        public ActivationContext(boolean active, long th, long it) {
            this.active = true;
            this.iteration = new AtomicInteger(0);
            this.active = active;
            if(it >= 0L) {
                this.forceIteration = it;
            } else {
                this.forceIteration = AbstractPerformanceInterceptor.FORCED_ITERATION;
            }

            if(th >= 0L) {
                this.threshold = th;
            } else {
                this.threshold = AbstractPerformanceInterceptor.THRESHOLD;
            }

            this.thresholdActive = this.threshold > 0L;
        }

        public ActivationContext(boolean active, String name) {
            this(active, AbstractPerformanceInterceptor.duration(Configuration.getProperty("org.apache.org.apache.sirona.performance." + name + ".threshold", (String)null)), (long)Configuration.getInteger("org.apache.org.apache.sirona.performance." + name + ".forced-iteration", -1));
        }

        public boolean isForcedIteration() {
            return (long)this.iteration.incrementAndGet() > this.forceIteration;
        }

        protected long getThreshold() {
            return this.threshold;
        }

        protected boolean isThresholdActive() {
            return this.thresholdActive;
        }

        public boolean isActive() {
            return this.active;
        }

        public void reset() {
            this.active = false;
            this.iteration.set(0);
        }

        public boolean shouldExecute() {
            return this.isActive() || this.isForcedIteration();
        }

        public void elapsedTime(long elapsedTime) {
            if(this.isThresholdActive() && elapsedTime < this.getThreshold()) {
                this.reset();
            }

        }
    }

    protected static class SerializableMethod implements Serializable {
        protected final String clazz;
        protected final String method;
        protected transient Method realMethod;
        protected final int hashCode;

        public SerializableMethod(String clazz, String method, Method reflectMethod) {
            this.clazz = clazz;
            this.method = method;
            this.realMethod = reflectMethod;
            this.hashCode = reflectMethod.hashCode();
        }

        public SerializableMethod(Method m) {
            this(m.getDeclaringClass().getName(), m.getName(), m);
        }

        public Method method() {
            if(this.realMethod == null) {
                try {
                    for(Class e = Thread.currentThread().getContextClassLoader().loadClass(this.clazz); e != null; e = e.getSuperclass()) {
                        Method[] arr$ = e.getDeclaredMethods();
                        int len$ = arr$.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                            Method m = arr$[i$];
                            if(m.getName().equals(this.method)) {
                                this.realMethod = m;
                                return this.realMethod;
                            }
                        }
                    }
                } catch (ClassNotFoundException var6) {
                    throw new SironaException(var6.getMessage(), var6);
                }
            }

            return this.realMethod;
        }

        public boolean equals(Object o) {
            if(this == o) {
                return true;
            } else if(o != null && this.getClass() == o.getClass()) {
                AbstractPerformanceInterceptor.SerializableMethod that = (AbstractPerformanceInterceptor.SerializableMethod)AbstractPerformanceInterceptor.SerializableMethod.class.cast(o);
                return this.method != null && that.method != null?this.method.equals(that.method):this.hashCode == that.hashCode;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCode;
        }
    }

    public static class Context {
        private static final int MAX_LENGTH = Configuration.getInteger("org.apache.org.apache.sirona.performance.exception.max-length", 100);
        protected final AbstractPerformanceInterceptor.ActivationContext activationContext;
        protected final StopWatch stopWatch;

        public Context(AbstractPerformanceInterceptor.ActivationContext activationContext, StopWatch stopWatch) {
            this.activationContext = activationContext;
            this.stopWatch = stopWatch;
        }

        public void stop() {
            if(this.stopWatch != null) {
                long elapsedTime = this.stopWatch.stop().getElapsedTime();
                this.activationContext.elapsedTime(elapsedTime);
            }

        }

        public void stopWithException(Throwable error) {
            if(this.stopWatch != null) {
                this.stopWatch.stop();
                long elapsedTime = this.stopWatch.getElapsedTime();
                if(error != null) {
                    Repository.INSTANCE.getCounter(new Key(Role.FAILURES, error.getClass().getName() + ":" + (error.getMessage() != null?error.getMessage():""))).add((double)elapsedTime);
                }

                this.activationContext.elapsedTime(elapsedTime);
            }

        }
    }
}