package org.apache.sirona.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Created by kong on 2016/1/24.
 */
public final class DaemonThreadFactory implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger(1);
    private final String baseName;

    public DaemonThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(Thread.currentThread().getThreadGroup(), r, this.baseName + this.id.getAndIncrement());
        if(!thread.isDaemon()) {
            thread.setDaemon(true);
        }

        if(thread.getPriority() != 5) {
            thread.setPriority(5);
        }

        thread.setContextClassLoader(DaemonThreadFactory.class.getClassLoader());
        return thread;
    }
}