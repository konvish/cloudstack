package com.kong.cloudstack.cmc.impl;

import org.codehaus.jackson.map.annotate.JsonRootName;
/**
 *
 * Created by kong on 2016/1/24.
 */
@JsonRootName("details")
public class InstanceDetails {
    private String name;
    private String number;
    private String owner;
    private String ownerContact;
    private String description;
    private int port;
    private int httpPort;

    public InstanceDetails() {
        this("");
    }

    public InstanceDetails(String description) {
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}