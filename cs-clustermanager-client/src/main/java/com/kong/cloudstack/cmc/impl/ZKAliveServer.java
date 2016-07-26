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
 * This shows a very simplified method of registering an instance with the service discovery. Each individual
 * instance in your distributed set of applications would create an instance of something similar to ZKAliveServer,
 * start it when the application comes up and close it when the application shuts down.
 *
 * Created by kong on 2016/1/24.
 */
public class ZKAliveServer implements Closeable {
    private final ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private final ServiceInstance<InstanceDetails> thisInstance;

    public ZKAliveServer(CuratorFramework client, String path, String serviceName, String description) throws Exception
    {
        String formatter = "{%s}://%s:{%d}";

        UriSpec uriSpec = new UriSpec(String.format(formatter, "tcp",NetUtil.getLocalHost(),CloudContextFactory.getCloudContext().getPort()));

        //address 为内网ip/hostname
        thisInstance = ServiceInstance.<InstanceDetails>builder()
                .id(CloudContextFactory.getCloudContext().getId())
                .name(serviceName)
                .payload(new InstanceDetails(description))
                .port(CloudContextFactory.getCloudContext().getPort())
                .uriSpec(uriSpec)
                .address(NetUtil.getLocalHost() + "/" + NetUtil.getLocalAddress().getHostName())
                .build();

        // if you mark your payload class with @JsonRootName the provided JsonInstanceSerializer will work
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(InstanceDetails.class);

        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(client)
                .basePath(path)
                .serializer(serializer)
                .thisInstance(thisInstance)
                .build();
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