package com.kong.monitor.model;

import com.kong.cloudstack.AbstractLifecycle;
import com.kong.cloudstack.utils.NamedThreadFactory;
import com.kong.monitor.model.MonitorEventContainer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by kong on 2016/1/24.
 */
public class MonitorEventManager extends AbstractLifecycle {
    public static final Logger logger = LoggerFactory.getLogger(MonitorEventManager.class);

    private MonitorEventManager() {
    }

    public static MonitorEventManager getInstance() {
        return MonitorEventManager.MonitorEventManagerHolder.instance;
    }

    protected void doStart() {
        MonitorEventContainer.getInstance().init();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("METASTORE"));
        executor.execute(new Runnable() {
            public void run() {
                long startTime = 0L;
                long interval = 0L;

                while(true) {
                    do {
                        startTime = System.currentTimeMillis();
                        MonitorEventContainer.getInstance().metaStore();
                        interval = System.currentTimeMillis() - startTime;
                    } while(interval >= 1000L);

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException var6) {
                        MonitorEventManager.logger.error("sleep error", var6);
                    }
                }
            }
        });
        boolean minWinSize = true;
        boolean maxWinSise = true;
        boolean deltaTime = true;
        ScheduledExecutorService logFlushExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LOGFLUSH"));
        logFlushExecutor.execute(new Runnable() {
            public void run() {
                boolean sleepTime = true;
                boolean eventCount = false;

                while(true) {
                    while(true) {
                        try {
                            int eventCount1 = MonitorEventContainer.getInstance().getEventCount();
                            int sleepTime1;
                            if(eventCount1 > 10000) {
                                sleepTime1 = 0;
                            } else if(eventCount1 > 1000 && eventCount1 < 10000) {
                                sleepTime1 = 1000 - eventCount1 % 1000 * 100;
                            } else {
                                sleepTime1 = 1000;
                            }

                            MonitorEventContainer.getInstance().flushLog();

                            try {
                                Thread.sleep((long)sleepTime1);
                                MonitorEventManager.logger.debug("logflush period is {}", Integer.valueOf(sleepTime1));
                            } catch (InterruptedException var4) {
                                MonitorEventManager.logger.error("sleep error", var4);
                            }
                        } catch (Exception var5) {
                            MonitorEventManager.logger.error("logFlush error!", var5);
                        }
                    }
                }
            }
        });
    }

    public void stop() {
    }

    public void addMonitor() {
    }

    public static class MonitorEventManagerHolder {
        private static MonitorEventManager instance = new MonitorEventManager();

        public MonitorEventManagerHolder() {
        }
    }
}