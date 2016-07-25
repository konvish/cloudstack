package com.kong.monitor.model;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.io.Files;
import com.kong.cloudstack.utils.FileLoader;
import com.kong.monitor.model.FormatMonitorEvent.Builder;
import com.kong.monitor.model.FormatMonitorEvent.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
/**
 * Created by kong on 2016/1/22.
 */
public class MonitorEventContainer {
    public static final Logger logger = LoggerFactory.getLogger(MonitorEventContainer.class);
    public static final String MONITOR_PREFIX = "/var/lib/logs/monitors/";
    public static final String MONITOR_META_PREFIX = "/var/lib/logs/monitors/meta/";
    public static final String META_FILE = "meta.properties";
    public static final String META_BAK_FILE = "meta_bak.properties";
    public static final String SPLIT_STR = ",";
    private List<Event> monitorEventList = Lists.newArrayList();
    private static Map<Class, String> meta = Maps.newHashMap();
    private static Map<Class, ch.qos.logback.classic.Logger> loggers = Maps.newHashMap();
    private ConcurrentMap<String, MetaData> metaMap = Maps.newConcurrentMap();
    private BlockingQueue<MetaData> fileMetaQueue = Queues.newLinkedBlockingQueue();
    private FileOutputStream metaFileOut;
    private File metaFile;
    private Properties metaProperties;
    private Properties deltaMetaProperties = new Properties();
    private static Properties properties = FileLoader.getFile("monitor.properties");

    public MonitorEventContainer() {
    }

    public void addEvent(Event event) {
        this.monitorEventList.add(event);
    }

    public static MonitorEventContainer getInstance() {
        return MonitorEventContainer.MonitorEventContainerHolder.instance;
    }

    private static ch.qos.logback.classic.Logger initLogger(String className, String filePrefix) throws ClassNotFoundException {
        ch.qos.logback.classic.Logger clazzLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(className);
        LoggerContext loggerContext = clazzLogger.getLoggerContext();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date%msg%n");
        encoder.start();
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern("/var/lib/logs/monitors/" + filePrefix + ".%d{yyyy-MM-dd}.%i.log");
        SizeAndTimeBasedFNATP sizePolicy = new SizeAndTimeBasedFNATP();
        sizePolicy.setContext(loggerContext);
        sizePolicy.setMaxFileSize("200MB");
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizePolicy);
        RollingFileAppender rollingFileAppender = new RollingFileAppender();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingPolicy.setParent(rollingFileAppender);
        clazzLogger.addAppender(rollingFileAppender);
        rollingPolicy.start();
        sizePolicy.start();
        rollingFileAppender.start();
        return clazzLogger;
    }

    public MetaData getMetadataByName(String eventName) {
        return (MetaData)this.metaMap.get(eventName);
    }

    public Event createFormatEvent(String name, String eventType, String metas, String externalUserId, Map<String, String> properties) {
        MetaData oldMetadata = (MetaData)this.metaMap.get(name);
        if(oldMetadata == null) {
            String[] monitorEvent = metas.split(",");
            oldMetadata = new MetaData(Arrays.asList(monitorEvent), name);
            MetaData newMetadata = (MetaData)this.metaMap.putIfAbsent(name, oldMetadata);
            if(newMetadata == null) {
                ;
            }

            this.fileMetaQueue.offer(oldMetadata);
        }

        FormatMonitorEvent monitorEvent1 = (new Builder(name, oldMetadata, "tid", eventType, externalUserId, properties)).build();
        this.monitorEventList.add(monitorEvent1);
        return monitorEvent1;
    }

    public void metaStore() {
        try {
            MetaData e = (MetaData)this.fileMetaQueue.take();
            if(!this.metaProperties.containsKey(e.getEventName())) {
                try {
                    this.deltaMetaProperties.clear();
                    this.deltaMetaProperties.setProperty(e.getEventName(), e.toString());
                    this.deltaMetaProperties.store(this.metaFileOut, "append line");
                } catch (IOException var7) {
                    logger.error("metaStore error", var7);
                    this.fileMetaQueue.offer(e);
                } finally {
                    ;
                }
            }
        } catch (InterruptedException var9) {
            logger.error("metaStore error!", var9);
        }

    }

    public void init() {
        this.loadMetaFiles();
    }

    public int getEventCount() {
        return this.monitorEventList.size();
    }

    public void flushLog() {
        ArrayList tempMonitorEventList = (ArrayList)((ArrayList)this.monitorEventList).clone();
        Iterator i$ = tempMonitorEventList.iterator();

        while(i$.hasNext()) {
            Event monitorEvent = (Event)i$.next();
            ((ch.qos.logback.classic.Logger)loggers.get(monitorEvent.getClass())).error(monitorEvent.snapshot());
            this.monitorEventList.remove(monitorEvent);
        }

    }

    private void loadMetaFiles() {
        try {
            this.metaFile = new File("/var/lib/logs/monitors/meta/".concat("meta.properties"));
            if(this.metaFile.exists()) {
                this.metaProperties = new Properties();
            } else {
                Files.createParentDirs(this.metaFile);
            }

            this.metaFileOut = new FileOutputStream(this.metaFile, true);
            this.metaProperties.load(new FileInputStream(this.metaFile));
            Set e = this.metaProperties.keySet();
            String eventName = null;
            String[] metaArray = null;
            MetaData metaData = null;
            Iterator i$ = e.iterator();

            while(i$.hasNext()) {
                Object eventNameObj = i$.next();
                eventName = (String)eventNameObj;
                metaArray = this.metaProperties.getProperty(eventName).split(",");
                metaData = new MetaData(Arrays.asList(metaArray), eventName);
                this.metaMap.put(eventName, metaData);
            }
        } catch (IOException var7) {
            logger.error("loadMetaFiles error", var7);
            System.exit(-1);
        }

    }

    static {
        Set propertyNames = properties.stringPropertyNames();
        String key = null;
        Class classKey = null;
        ch.qos.logback.classic.Logger dynLogger = null;

        try {
            Iterator e = propertyNames.iterator();

            while(e.hasNext()) {
                String propertyName = (String)e.next();
                key = propertyName.substring(0, propertyName.lastIndexOf("."));
                classKey = Class.forName(key);
                meta.put(classKey, properties.getProperty(propertyName));
                dynLogger = initLogger(key, properties.getProperty(propertyName));
                loggers.put(classKey, dynLogger);
            }
        } catch (ClassNotFoundException var6) {
            logger.error("handle monitor.properties error", var6);
            System.exit(-1);
        }

    }

    public static class MonitorEventContainerHolder {
        private static MonitorEventContainer instance = new MonitorEventContainer();

        public MonitorEventContainerHolder() {
        }
    }
}