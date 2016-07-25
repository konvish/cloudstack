package com.kong.monitor.model;

/**
 * Created by kong on 2016/1/22.
 */
import com.kong.monitor.model.Event;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FormatMonitorEvent implements Event {
    private Map<String, String> allProperties;
    private FormatMonitorEvent.MetaData metadata;

    private FormatMonitorEvent(Map<String, String> allProperties, FormatMonitorEvent.MetaData metadata) {
        this.allProperties = allProperties;
        this.metadata = metadata;
    }

    public void add(String property, String value) {
    }

    public String snapshot() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator i$ = this.metadata.getMetadata().iterator();

        while(i$.hasNext()) {
            String key = (String)i$.next();
            stringBuilder.append(" ").append((String)this.allProperties.get(key));
        }

        return stringBuilder.toString();
    }

    public static class Builder {
        private final String name;
        private final FormatMonitorEvent.MetaData metadata;
        private final String tid;
        private final String eventType;
        private final String externalUserId;
        private Map<String, String> properties;

        public Builder(String name, FormatMonitorEvent.MetaData metadata, String tid, String eventType, String externalUserId, Map<String, String> properties) {
            this.name = name;
            this.metadata = metadata;
            this.tid = tid;
            this.eventType = eventType;
            this.externalUserId = externalUserId;
            this.properties = properties;
        }

        public FormatMonitorEvent.Builder add(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public FormatMonitorEvent build() {
            HashMap allProperties = Maps.newHashMap();
            allProperties.putAll(this.properties);
            allProperties.put("name", this.name);
            allProperties.put("event_type", this.eventType);
            allProperties.put("tid", this.tid);
            allProperties.put("external_user_id", this.externalUserId);
            return new FormatMonitorEvent(allProperties, this.metadata);
        }
    }

    public static class MetaData {
        private String eventName;
        private List<String> metadata = Lists.newArrayList();

        public MetaData(List<String> newMetadata, String eventName) {
            this.eventName = eventName;
            this.metadata.add("name");
            this.metadata.add("event_type");
            this.metadata.add("tid");
            this.metadata.add("external_user_id");
            Iterator i$ = newMetadata.iterator();

            while(i$.hasNext()) {
                String key = (String)i$.next();
                this.metadata.add(key);
            }

        }

        public List<String> getMetadata() {
            return this.metadata;
        }

        public String getEventName() {
            return this.eventName;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator i$ = this.metadata.iterator();

            while(i$.hasNext()) {
                String metadataStr = (String)i$.next();
                stringBuilder.append(metadataStr).append(",");
            }

            return stringBuilder.subSequence(0, stringBuilder.length() - 1).toString();
        }
    }
}