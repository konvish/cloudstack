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
 * MonitorEvent容器，保存所有的MonitorEvent
 * <p/>
 * 会对所有的MonitorEvent进行类别分类
 *  会将所有事件分散在几个不同的文件中；
 *  会把整机的事件元数据存储在一个特定的meta文件中
 *  存储地址,元数据："/var/lib/logs/monitors/meta/meta.properties"
 *  日志："/var/lib/logs/monitors/事件类型/*.log"
 *  Created by kong on 2016/1/22.
 */
public class MonitorEventContainer {
    public static final Logger logger = LoggerFactory.getLogger(MonitorEventContainer.class);

    public static final String MONITOR_PREFIX = "/var/lib/logs/monitors/";
    public static final String MONITOR_META_PREFIX = MONITOR_PREFIX + "meta/";
    public static final String META_FILE = "meta.properties";
    public static final String META_BAK_FILE = "meta_bak.properties";
    public static final String SPLIT_STR = ",";

    /** 所有事件数据 */
    private List<Event> monitorEventList = Lists.newArrayList();
    /** 所有的事件元数据信息 告诉需要存储在那里，然后根据文件大小设置自动滚动 */
    private static Map<Class, String> meta = Maps.newHashMap();
    private static Map<Class, ch.qos.logback.classic.Logger> loggers = Maps.newHashMap();
    //private ConcurrentMap<Class, List<FormatMonitorEvent>> eventMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, FormatMonitorEvent.MetaData> metaMap = Maps.newConcurrentMap();
    private BlockingQueue<FormatMonitorEvent.MetaData> fileMetaQueue = Queues.newLinkedBlockingQueue();

    private FileOutputStream metaFileOut;
    private File metaFile;
    /** 元数据properties句柄 */
    private Properties metaProperties;
    private Properties deltaMetaProperties = new Properties();

    /**
     * 添加事件
     * @param event 事件
     */
    public void addEvent(Event event) {
        monitorEventList.add(event);
    }

    public static class MonitorEventContainerHolder{
        private static MonitorEventContainer instance = new MonitorEventContainer();
    }

    public static MonitorEventContainer getInstance(){
        return MonitorEventContainerHolder.instance;
    }

    private static Properties properties;
    static{
        properties = FileLoader.getFile("monitor.properties");
        Set<String> propertyNames = properties.stringPropertyNames();
        String key = null;
        Class classKey = null;
        ch.qos.logback.classic.Logger dynLogger = null;
        try {
            for(String propertyName : propertyNames) {
                key = propertyName.substring(0, propertyName.lastIndexOf("."));

                classKey = Class.forName(key);
                meta.put(classKey, properties.getProperty(propertyName));
                dynLogger = initLogger(key, properties.getProperty(propertyName));
                loggers.put(classKey, dynLogger);
            }
        } catch (ClassNotFoundException e) {
            logger.error("handle monitor.properties error", e);
            System.exit(-1);
        }


    }

    /**
     * 动态生成对应的logger  以时间和文件大小滚动
     * @param className 类名
     * @param filePrefix 生成文件前缀
     * @return logger
     * @throws ClassNotFoundException
     */
    private static ch.qos.logback.classic.Logger initLogger(String className, String filePrefix) throws ClassNotFoundException {
        final ch.qos.logback.classic.Logger clazzLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(className);
        LoggerContext loggerContext = clazzLogger.getLoggerContext();
        // Set up the pattern
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date%msg%n");
        encoder.start();

        //文件
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(MONITOR_PREFIX + filePrefix + ".%d{yyyy-MM-dd}.%i.log");

        //文件大小
        SizeAndTimeBasedFNATP sizePolicy = new SizeAndTimeBasedFNATP();
        sizePolicy.setContext(loggerContext);
        sizePolicy.setMaxFileSize("200MB");
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizePolicy);

        //appender
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

    /**
     * 获取特定事件的元数据描述信息
     * @param eventName 事件名称
     * @return FormatMonitorEvent.MetaData
     */
    public FormatMonitorEvent.MetaData getMetadataByName(String eventName){
        return metaMap.get(eventName);
    }

    /**
     * 新建特定格式的event
     * @param name 事件名称
     * @param eventType 事件类型
     * @param metas 新加元数据信息
     * @param externalUserId 业务操作的主体方
     * @param properties 附加信息
     * @return 事件
     */
    public Event createFormatEvent(String name, String eventType, String metas, String externalUserId, Map<String, String> properties){
        FormatMonitorEvent.MetaData oldMetadata = metaMap.get(name);
        if(oldMetadata == null){
            String[] metaArray = metas.split(SPLIT_STR);
            oldMetadata = new FormatMonitorEvent.MetaData(Arrays.asList(metaArray), name);

            //有原始信息就get(),没有就put()
            FormatMonitorEvent.MetaData newMetadata = metaMap.putIfAbsent(name, oldMetadata);
            if(newMetadata == null){
                newMetadata = oldMetadata;
            }

            //异步消费
            fileMetaQueue.offer(oldMetadata);
        }
        //String tid
        FormatMonitorEvent monitorEvent = new FormatMonitorEvent.Builder(name, oldMetadata, "tid", eventType, externalUserId, properties).build();

        //本地记录meta


        //避免并发处理，放入一个list
        monitorEventList.add(monitorEvent);
        return monitorEvent;
    }

    /**
     * 将元数据描述存储在本地
     */
    public void metaStore(){
        try {
            FormatMonitorEvent.MetaData metaData = fileMetaQueue.take();
            if(!metaProperties.containsKey(metaData.getEventName())){//不存在，才持久化
                try{
                    deltaMetaProperties.clear();
                    deltaMetaProperties.setProperty(metaData.getEventName(), metaData.toString());
                    deltaMetaProperties.store(metaFileOut, "append line");
                } catch (IOException e) {
                    logger.error("metaStore error", e);
                    fileMetaQueue.offer(metaData);
                }
            }
        } catch (InterruptedException e) {
            logger.error("metaStore error!", e);
        }
    }

    /**
     * 初始化
     */
    public void init(){
        loadMetaFiles();
    }

    /**
     * 返回内存事件数大小
     * @return
     */
    public int getEventCount(){
        return monitorEventList.size();
    }

    public void flushLog(){
        ArrayList<Event> tempMonitorEventList = (ArrayList<Event>) ((ArrayList<Event>)monitorEventList).clone();//Collections.unmodifiableList(monitorEventList);
        for (Event monitorEvent : tempMonitorEventList) {
            loggers.get(monitorEvent.getClass()).error(monitorEvent.snapshot());
            monitorEventList.remove(monitorEvent);
        }
    }

    /**
     * 从本地文件装载事件的元数据描述
     */
    private void loadMetaFiles() {
        try {
            metaFile = new File(MONITOR_META_PREFIX.concat(META_FILE));
            if(metaFile.exists()){
                metaProperties = new Properties();
            } else {
                Files.createParentDirs(metaFile);
                //metaFile.createNewFile();
            }

            metaFileOut = new FileOutputStream(metaFile, true);
            metaProperties.load(new FileInputStream(metaFile));
            Set<Object> events = metaProperties.keySet();
            String eventName = null;
            String[] metaArray = null;
            FormatMonitorEvent.MetaData metaData = null;
            for(Object eventNameObj : events){
                eventName = (String) eventNameObj;
                metaArray = metaProperties.getProperty(eventName).split(SPLIT_STR);
                metaData = new FormatMonitorEvent.MetaData(Arrays.asList(metaArray), eventName);

                metaMap.put(eventName, metaData);

                //properties.
            }
        } catch (IOException e) {
            logger.error("loadMetaFiles error", e);
            System.exit(-1);
        }
    }
}
