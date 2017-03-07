package com.kong.monitor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 监控事件，所有的监控使用此事件模型处理
 * Created by kong on 2016/1/22.
 */
public class FormatMonitorEvent implements Event {
    /**元数据与值*/
    private Map<String, String> allProperties;
    /**元数据*/
    private FormatMonitorEvent.MetaData metadata;

    private FormatMonitorEvent(Map<String, String> allProperties, FormatMonitorEvent.MetaData metadata) {
        this.allProperties = allProperties;
        this.metadata = metadata;
    }

    public void add(String property, String value) {
    }

    /**
     * 根据FormatMonitorEvent.MetaData与allProperties进行镜像
     * @return metadata's value's...
     */
    public String snapshot() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator it = this.metadata.getMetadata().iterator();

        while(it.hasNext()) {
            String key = (String)it.next();
            stringBuilder.append(" ").append(this.allProperties.get(key));
        }

        return stringBuilder.toString();
    }

    /**
     * 事件结构构造
     */
    public static class Builder {
        /** 事件名称 */
        private final String name;
        /** event元数据 props的key列表，在log中不显示，减少log文件大小 */
        private final MetaData metadata;
        /** traceid 链式的追踪id */
        private final String tid;
        /**事件类型*/
        private final String eventType;
        /** 业务操作的主体方 */
        private final String externalUserId;
        /** 附带信息 */
        private Map<String, String> properties;

        public Builder(String name, FormatMonitorEvent.MetaData metadata, String tid, String eventType, String externalUserId, Map<String, String> properties) {
            this.name = name;
            this.metadata = metadata;
            this.tid = tid;
            this.eventType = eventType;
            this.externalUserId = externalUserId;
            this.properties = properties;
        }

        /**
         * 添加附带信息
         * @param key key
         * @param value value
         * @return FormatMonitorEvent.Builder
         */
        public FormatMonitorEvent.Builder add(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        /**
         * 事件模型构建
         * @return FormatMonitorEvent
         */
        public FormatMonitorEvent build() {
            HashMap<String,String> allProperties = Maps.newHashMap();
            allProperties.putAll(this.properties);
            allProperties.put("name", this.name);
            allProperties.put("event_type", this.eventType);
            allProperties.put("tid", this.tid);
            allProperties.put("external_user_id", this.externalUserId);
            return new FormatMonitorEvent(allProperties, this.metadata);
        }
    }

    /**
     * 元事件的结构数据
     * 事件名称,List(元数据)
     */
    public static class MetaData {
        private String eventName;
        private List<String> metadata = Lists.newArrayList();

        /**
         * 构造函数
         * 默认事件名称,List("name","event_type","tid","external_user_id")
         * @param newMetadata 新元数据类型
         * @param eventName 事件名称
         */
        public MetaData(List<String> newMetadata, String eventName) {
            this.eventName = eventName;
            this.metadata.add("name");
            this.metadata.add("event_type");
            this.metadata.add("tid");
            this.metadata.add("external_user_id");
            Iterator it = newMetadata.iterator();

            //追加新元数据类型
            while(it.hasNext()) {
                String key = (String)it.next();
                this.metadata.add(key);
            }

        }

        public List<String> getMetadata() {
            return this.metadata;
        }

        public String getEventName() {
            return this.eventName;
        }

        /**
         * 重写toString类型
         * @return metadata.mkString(,)
         */
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator it = this.metadata.iterator();

            while(it.hasNext()) {
                String metadataStr = (String)it.next();
                stringBuilder.append(metadataStr).append(",");
            }

            return stringBuilder.subSequence(0, stringBuilder.length() - 1).toString();
        }
    }
}