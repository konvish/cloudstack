package com.kong.monitor.model;

import com.kong.cloudstack.AbstractLifecycle;
import com.kong.cloudstack.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MonitorEventContainer 管理器  会对container进行相应的清理工作
 * <p/>
 * Created by kong on 2016/1/22.
 */
public class MonitorEventManager extends AbstractLifecycle{
    public static final Logger logger = LoggerFactory.getLogger(MonitorEventManager.class);

    private MonitorEventManager(){}

    public static class MonitorEventManagerHolder{
        private static MonitorEventManager instance = new MonitorEventManager();
    }

    public static MonitorEventManager getInstance(){
        return MonitorEventManagerHolder.instance;
    }

    @Override
    protected void doStart() {
        MonitorEventContainer.getInstance().init();

        //定期将元数据描述刷新到本地文件
        ExecutorService executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("METASTORE"));
        executor.execute(new Runnable() {
            @Override
            public void run() {
                long startTime = 0;
                long interval = 0;
                while(true) {
                    startTime = System.currentTimeMillis();
                    MonitorEventContainer.getInstance().metaStore();
                    interval = System.currentTimeMillis() - startTime;
                    if(interval < 1000){ //太快，停一下
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            logger.error("sleep error", e);
                        }
                    }
                }
            }
        });

        //定期将内存数据刷到磁盘  采用简单的拥塞算法   窗口大小后续可以动态调整
        final int minWinSize = 1000;
        final int maxWinSise = 10000;
        final int deltaTime = 100;
        ExecutorService logFlushExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LOGFLUSH"));
        logFlushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //默认1秒
                int sleepTime = 1000;
                int eventCount = 0;
                while (true) {
                    try {
                        eventCount = MonitorEventContainer.getInstance().getEventCount();
                        if(eventCount > maxWinSise){
                            sleepTime = 0;
                        } else if(eventCount > minWinSize && eventCount < maxWinSise){
                            sleepTime = 1000 -  (eventCount % minWinSize) * deltaTime;
                        } else {
                            sleepTime = 1000;
                        }
                        MonitorEventContainer.getInstance().flushLog();

                        try {
                            Thread.sleep(sleepTime);
                            logger.debug("logflush period is {}", sleepTime);
                        } catch (InterruptedException e) {
                            logger.error("sleep error", e);
                        }
                    } catch (Exception e) {
                        logger.error("logFlush error!", e);
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        //MonitorEventContainer.getInstance().destory();
    }

    public void addMonitor(){

    }
}
