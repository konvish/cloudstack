package com.kong.cloudstack.sz;

import com.kong.cloudstack.client.zookeeper.ZKClient;
import com.kong.cloudstack.client.zookeeper.recover.ZKRecoverUtil;
import com.kong.cloudstack.context.CloudContextFactory;
import com.kong.cloudstack.sz.util.EncryptUtil;
import com.kong.cloudstack.utils.AESUtil;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.AbstractResource;
/**
 * zookeeper资源
 *
 * Created by kong on 2016/1/24.
 */
public class ZookeeperResource extends AbstractResource implements ApplicationContextAware, DisposableBean {
    private static Logger log = LoggerFactory.getLogger(ZookeeperResource.class);
    /**
     * zk地址头
     */
    public static final String URL_HEADER = "zk://";
    private static final String PATH_FORMAT = "/startconfigs/%s/config";
    private static final String CLOUD_PATH_FORMAT = "/startconfigs/%s/%s/config";
    private String path = String.format(PATH_FORMAT, CloudContextFactory.getCloudContext().getApplicationName());
    private String cloud_path = String.format(CLOUD_PATH_FORMAT,CloudContextFactory.getCloudContext().getProductCode(), CloudContextFactory.getCloudContext().getApplicationName());
    ConcurrentMap<String, Object> recoverDataCache = Maps.newConcurrentMap();
    AbstractApplicationContext ctx;

    public ZookeeperResource() {
    }

    /**
     * 检查zk的配置是否存在
     * @return boolean
     */
    public boolean exists() {
        try {
            return null != ZKClient.getClient().checkExists().forPath("");
        } catch (Exception var2) {
            log.error("Falied to detect the config in zookeeper.", var2);
            return false;
        }
    }

    /**
     * 一直返回false
     * @return boolean
     */
    public boolean isOpen() {
        return false;
    }

    /**
     * 获取zk的URL
     * @return url
     * @throws IOException
     */
    public URL getURL() throws IOException {
        return new URL(URL_HEADER + this.path);
    }

    /**
     * 获取配置文件名
     * @return str
     * @throws IllegalStateException
     */
    public String getFilename() throws IllegalStateException {
        return this.path;
    }

    /**
     * zk的配置文件描述
     * @return str
     */
    public String getDescription() {
        return "Zookeeper resouce at '"+URL_HEADER + this.path;
    }

    /**
     * 读取配置文件的内容为数据流
     * @return 输入流
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        byte[] data = null;

        try {
            if(ZKClient.getClient().checkExists().forPath(this.cloud_path) == null) {
                data = ZKClient.getClient().getData().forPath(this.path);
            } else if(ZKClient.getClient().checkExists().forPath(this.cloud_path) == null) {
                log.error("{} and {} none exists", this.cloud_path, this.path);
                System.exit(-1);
            } else {
                data = ZKClient.getClient().getData().forPath(this.cloud_path);
            }
        } catch (Exception var7) {
            log.error("zk server error", var7);

            // 读取cmc配置失败时加载本地备份的配置
            try {
                data = ZKRecoverUtil.loadRecoverData(this.cloud_path);
            } catch (Exception var6) {
                log.error("zk server cloud_path error", var7);
                data = ZKRecoverUtil.loadRecoverData(this.path);
            }
        }

        // 备份cmc配置到本地
        ZKRecoverUtil.doRecover(data, this.path, this.recoverDataCache);
        ZKRecoverUtil.doRecover(data, this.cloud_path, this.recoverDataCache);
        log.debug("init get startconfig data {}", new String(data));
        if(EncryptUtil.isEncrypt(data)) {
            byte[] pureData = new byte[data.length - 2];
            System.arraycopy(data, 2, pureData, 0, data.length - 2);
            String originStr = null;

            try {
                originStr = AESUtil.aesDecrypt(new String(pureData), EncryptUtil.encryptKey);
            } catch (Exception var5) {
                log.error("decrypt error", var5);
                System.exit(-1);
            }

            return new ByteArrayInputStream(originStr.getBytes());
        } else {
            return new ByteArrayInputStream(data);
        }
    }

    /**
     * 设置上下文
     * @param ctx ApplicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = (AbstractApplicationContext)ctx;
    }

    /**
     * 销毁ZookeeperResource
     * @throws Exception
     */
    public void destroy() throws Exception {
        log.info("Destory Zookeeper Resouce.");
    }
}