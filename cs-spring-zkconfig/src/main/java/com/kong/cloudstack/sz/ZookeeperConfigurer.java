package com.kong.cloudstack.sz;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * Created by kong on 2016/1/24.
 */
public class ZookeeperConfigurer extends PropertySourcesPlaceholderConfigurer {
    private Map<String, Object> ctxPropsMap = new HashMap<String,Object>();
    private ZookeeperResource zkLocation;
    private Resource[] localLocations = new Resource[0];

    public ZookeeperConfigurer() {
    }

    public void setLocation(Resource location) {
        this.zkLocation = (ZookeeperResource)location;
        super.setLocations((Resource[])mergeArray(this.localLocations, this.zkLocation));
    }

    public void setLocations(Resource[] locations) {
        System.arraycopy(locations, 0, this.localLocations, 0, locations.length);
        super.setLocations((Resource[])mergeArray(locations, this.zkLocation));
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, ConfigurablePropertyResolver propertyResolver) throws BeansException {
        super.processProperties(beanFactoryToProcess, propertyResolver);
    }

    public Object getProperty(String key) {
        return this.ctxPropsMap.get(key);
    }

    public ZookeeperResource getZkResoucre() {
        return this.zkLocation;
    }

    private static Resource[] mergeArray(Resource[] m1, Resource m2) {
        Resource[] all = new Resource[m1.length + 1];
        if(m1.length > 0) {
            System.arraycopy(all, 0, m1, 0, m1.length);
            all[m1.length] = m2;
        } else {
            all[m1.length] = m2;
        }

        return all;
    }

    public static void main(String[] args) {
        Resource[] m1 = new Resource[2];
        Resource m2 = new Resource() {
            public boolean exists() {
                return false;
            }

            public boolean isReadable() {
                return false;
            }

            public boolean isOpen() {
                return false;
            }

            public URL getURL() throws IOException {
                return null;
            }

            public URI getURI() throws IOException {
                return null;
            }

            public File getFile() throws IOException {
                return null;
            }

            public long contentLength() throws IOException {
                return 0L;
            }

            public long lastModified() throws IOException {
                return 0L;
            }

            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            public String getFilename() {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public InputStream getInputStream() throws IOException {
                return null;
            }
        };
        Resource[] m3 = mergeArray(m1, m2);
        int i = m3.length;
    }
}