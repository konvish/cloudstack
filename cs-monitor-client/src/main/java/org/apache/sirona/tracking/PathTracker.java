package org.apache.sirona.tracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.spi.Order;
import org.apache.sirona.spi.SPI;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.Context;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.apache.sirona.tracking.PathTrackingInformation;
import org.apache.sirona.tracking.PathTrackingInvocationListener;
/**
 * Created by kong on 2016/1/24.
 */
public class PathTracker {
    private static final String NODE = Configuration.getProperty("org.apache.org.apache.sirona.javaagent.path.tracking.marker", Configuration.getProperty("org.apache.org.apache.sirona.cube.CubeBuilder.marker", "node"));
    private static final PathTrackingDataStore PATH_TRACKING_DATA_STORE = ((DataStoreFactory)IoCs.findOrCreateInstance(DataStoreFactory.class)).getPathTrackingDataStore();
    private static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal() {
        protected Context initialValue() {
            return new Context();
        }
    };
    private final PathTrackingInformation pathTrackingInformation;
    private static final boolean USE_EXECUTORS = Boolean.parseBoolean(Configuration.getProperty("org.apache.org.apache.sirona.pathtracking.useexecutors", "false"));
    private static boolean USE_SINGLE_STORE = Boolean.parseBoolean(Configuration.getProperty("org.apache.org.apache.sirona.pathtracking.singlestore", "false"));
    protected static ExecutorService EXECUTORSERVICE;
    private static PathTrackingInvocationListener[] LISTENERS;

    public static PathTrackingInvocationListener[] getPathTrackingInvocationListeners() {
        return LISTENERS;
    }

    private PathTracker(PathTrackingInformation pathTrackingInformation) {
        this.pathTrackingInformation = pathTrackingInformation;
    }

    private static void cleanUp() {
        THREAD_LOCAL.remove();
    }

    public static PathTracker start(PathTrackingInformation pathTrackingInformation) {
        Context context = (Context)THREAD_LOCAL.get();
        int level = 0;
        PathTrackingInformation current = context.getPathTrackingInformation();
        if(current == null) {
            level = context.getLevel().incrementAndGet();
            pathTrackingInformation.setLevel(level);
        } else if(current != pathTrackingInformation) {
            level = context.getLevel().incrementAndGet();
            pathTrackingInformation.setLevel(level);
            pathTrackingInformation.setParent(current);
        }

        pathTrackingInformation.setStart(System.nanoTime());
        context.setPathTrackingInformation(pathTrackingInformation);
        PathTrackingInvocationListener[] arr$ = LISTENERS;
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            PathTrackingInvocationListener listener = arr$[i$];
            if(level == 1) {
                listener.startPath(context);
            } else {
                listener.enterMethod(context);
            }
        }

        return new PathTracker(pathTrackingInformation);
    }

    public void stop() {
        long end = System.nanoTime();
        long start = this.pathTrackingInformation.getStart();
        final Context context = (Context)THREAD_LOCAL.get();
        String uuid = context.getUuid();
        PathTrackingInformation current = context.getPathTrackingInformation();
        if(this.pathTrackingInformation != current) {
            context.getLevel().decrementAndGet();
            context.setPathTrackingInformation(this.pathTrackingInformation.getParent());
        }

        int len$;
        if(context.getPathTrackingInformation() != null) {
            PathTrackingInvocationListener[] pathTrackingEntry = LISTENERS;
            int arr$ = pathTrackingEntry.length;

            for(len$ = 0; len$ < arr$; ++len$) {
                PathTrackingInvocationListener i$ = pathTrackingEntry[len$];
                i$.exitMethod(context);
            }
        }

        PathTrackingEntry var14 = new PathTrackingEntry(uuid, NODE, this.pathTrackingInformation.getClassName(), this.pathTrackingInformation.getMethodName(), start, end - start, this.pathTrackingInformation.getLevel());
        if(USE_SINGLE_STORE) {
            PATH_TRACKING_DATA_STORE.store(var14);
        } else {
            context.getEntries().add(var14);
        }

        if(this.pathTrackingInformation.getLevel() == 1 && this.pathTrackingInformation.getParent() == null) {
            if(!USE_SINGLE_STORE) {
                Runnable var15 = new Runnable() {
                    public void run() {
                        PathTracker.PATH_TRACKING_DATA_STORE.store(context.getEntries());
                        PathTracker.cleanUp();
                    }
                };
                if(USE_EXECUTORS) {
                    EXECUTORSERVICE.submit(var15);
                } else {
                    var15.run();
                }
            }

            PathTrackingInvocationListener[] var16 = LISTENERS;
            len$ = var16.length;

            for(int var13 = 0; var13 < len$; ++var13) {
                PathTrackingInvocationListener listener = var16[var13];
                listener.endPath(context);
            }
        }

    }

    @Destroying
    public void destroy() {
        shutdown();
    }

    public static void shutdown() {
        EXECUTORSERVICE.shutdownNow();
    }

    static {
        if(USE_EXECUTORS) {
            int classLoader = Configuration.getInteger("org.apache.org.apache.sirona.pathtracking.executors", 5);
            EXECUTORSERVICE = Executors.newFixedThreadPool(classLoader);
        }

        ClassLoader classLoader1 = PathTracker.class.getClassLoader();
        if(classLoader1 == null) {
            classLoader1 = Thread.currentThread().getContextClassLoader();
        }

        ArrayList listeners = new ArrayList();
        Iterator iterator = SPI.INSTANCE.find(PathTrackingInvocationListener.class, classLoader1).iterator();

        while(iterator.hasNext()) {
            try {
                listeners.add(IoCs.autoSet(iterator.next()));
            } catch (Exception var4) {
                throw new RuntimeException(var4.getMessage(), var4);
            }
        }

        Collections.sort(listeners, PathTracker.ListenerComparator.INSTANCE);
        LISTENERS = (PathTrackingInvocationListener[])listeners.toArray(new PathTrackingInvocationListener[listeners.size()]);
    }

    private static class ListenerComparator implements Comparator<PathTrackingInvocationListener> {
        private static final PathTracker.ListenerComparator INSTANCE = new PathTracker.ListenerComparator();

        private ListenerComparator() {
        }

        public int compare(PathTrackingInvocationListener o1, PathTrackingInvocationListener o2) {
            Order order1 = (Order)o1.getClass().getAnnotation(Order.class);
            Order order2 = (Order)o2.getClass().getAnnotation(Order.class);
            return order2 == null?-1:(order1 == null?1:order1.value() - order2.value());
        }
    }
}