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
 * zookeeper配置
 * 继承自{@link PropertySourcesPlaceholderConfigurer}
 * 可从spring的配置文件中获取配置，JVM配置(System.getProperty())和系统变量配置(System.getenv())
 * Created by kong on 2016/1/24.
 */
public class ZookeeperConfigurer extends PropertySourcesPlaceholderConfigurer {
    /**
     * 配置Map
     */
    private Map<String, Object> ctxPropsMap = new HashMap<String,Object>();
    //zk的配置地址
    private ZookeeperResource zkLocation;
    private Resource[] localLocations = new Resource[0];

    public ZookeeperConfigurer() {
    }

    /**
     * 添加新的配置地址
     * @param location resource
     */
    public void setLocation(Resource location) {
        this.zkLocation = (ZookeeperResource)location;
        super.setLocations((Resource[])mergeArray(this.localLocations, this.zkLocation));
    }

    /**
     * 添加多个配置地址
     * @param locations resources
     */
    public void setLocations(Resource[] locations) {
        System.arraycopy(locations, 0, this.localLocations, 0, locations.length);
        super.setLocations((Resource[])mergeArray(locations, this.zkLocation));
    }

    /**
     * 处理配置文件
     * @param beanFactoryToProcess 配置bean工厂
     * @param propertyResolver 配置处理类
     * @throws BeansException
     */
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, ConfigurablePropertyResolver propertyResolver) throws BeansException {
        super.processProperties(beanFactoryToProcess, propertyResolver);
    }

    /**
     * key对应的配置内容
     * @param key key
     * @return obj
     */
    public Object getProperty(String key) {
        return this.ctxPropsMap.get(key);
    }

    /**
     * zk资源信息
     * @return zookeeperResource
     */
    public ZookeeperResource getZkResource() {
        return this.zkLocation;
    }

    /**
     * 合并两份资源
     * @param m1 资源1
     * @param m2 资源2
     * @return 数组(m2追加在m1后面)
     */
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