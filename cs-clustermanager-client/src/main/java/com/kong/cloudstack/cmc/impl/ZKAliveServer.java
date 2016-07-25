package com.kong.cloudstack.cmc.impl;

import com.kong.cloudstack.context.CloudContextFactory;
import com.kong.cloudstack.utils.NetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.Closeable;
import java.io.IOException;
/**
 * Created by kong on 2016/1/24.
 */
public class ZKAliveServer implements Closeable {
    private final ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private final ServiceInstance<InstanceDetails> thisInstance;

    public ZKAliveServer(CuratorFramework client, String path, String serviceName, String description) throws Exception {
        String formatter = "{%s}://%s:{%d}";
        UriSpec uriSpec = new UriSpec(String.format(formatter, new Object[]{"tcp", NetUtil.getLocalHost(), Integer.valueOf(CloudContextFactory.getCloudContext().getPort())}));
        ServiceInstance<Object> build = ServiceInstance.builder().id(CloudContextFactory.getCloudContext().getId()).name(serviceName).payload(new InstanceDetails(description)).port(CloudContextFactory.getCloudContext().getPort()).uriSpec(uriSpec).address(NetUtil.getLocalHost() + "/" + NetUtil.getLocalAddress().getHostName()).build();
        this.thisInstance =(ServiceInstance)build;
        JsonInstanceSerializer serializer = new JsonInstanceSerializer(InstanceDetails.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(path).serializer(serializer).thisInstance(this.thisInstance).build();
    }

    public ServiceInstance<InstanceDetails> getThisInstance() {
        return this.thisInstance;
    }

    public void start() throws Exception {
        this.serviceDiscovery.start();
    }

    public void close() throws IOException {
        CloseableUtils.closeQuietly(this.serviceDiscovery);
    }
}