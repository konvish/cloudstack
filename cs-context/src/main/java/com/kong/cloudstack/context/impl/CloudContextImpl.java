package com.kong.cloudstack.context.impl;

import com.kong.cloudstack.AbstractLifecycle;
import com.kong.cloudstack.context.ICloudContext;
import com.kong.cloudstack.context.domain.AppType;
import com.kong.cloudstack.utils.ConfigLoader;
import com.kong.cloudstack.utils.NetUtil;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * server上下文实现类
 * Created by kong on 2016/1/24.
 */
public class CloudContextImpl extends AbstractLifecycle implements ICloudContext {
    public static final Logger logger = LoggerFactory.getLogger(CloudContextImpl.class);
    private volatile boolean isStart = false;
    public static final String DEPLOY_PATH_KEY = "deploypath";
    public static final String DEPLOY_FILE_KEY = "deployfile";
    public static final String APP_METADATA_FILE = "/config/metadata.properties";
    public static final String APP_METADATA_FILE_NOHUB = "config/metadata.properties";
    public static final String APP_NAME = "name";
    public static final String APP_ZHNAME = "zh_name";
    public static final String APP_TYPE = "type";
    public static final String APP_OWNER = "owner";
    public static final String APP_OWNERCONTACT = "ownercontact";
    public static final String APP_DESCRIPTION = "description";
    public static final String APP_PORT = "port";
    public static final String APP_HTTP_PORT = "http_port";
    public static final String PRODUCT = "product";
    public static final String PRODUCT_CODE = "product_code";
    /** 应用名称 */
    private String applicationName;
    private String zhName;
    /** 应用类型 */
    private AppType appType;
    /** 系统owner */
    private String owner;
    /** 系统owner 联系方式 */
    private String ownerContact;
    private String description;
    /** dubbo端口，必须填写 */
    private int port;
    /** http端口,默认80 */
    private int httpPort = 80;
    /** 系统所属产品线 */
    private String product;
    /** 系统所属产品线代码 */
    private String productCode;
    /**
     * cluster node id
     * ip converter
     */
    private String id;

    public CloudContextImpl() {
    }

    /**
     * 通过左移位操作（<<）给每一段的数字加权
     * 第一段的权为2的24次方
     * 第二段的权为2的16次方
     * 第三段的权为2的8次方
     * 最后一段的权为1
     *
     * @param ip
     * @return int
     */
    public static Long ipToLong(String ip) {
        String[] ips = ip.split("\\.");
        return (Long.parseLong(ips[0]) << 24) + (Long.parseLong(ips[1]) << 16)
                + (Long.parseLong(ips[2]) << 8) + Long.parseLong(ips[3]);
    }

    /**
     * 初始化参数
     */
    public void doStart() {
        String deploypath = ConfigLoader.getInstance().getProperty(DEPLOY_PATH_KEY);
        String deployfile = ConfigLoader.getInstance().getProperty(DEPLOY_FILE_KEY);
        String metaFile = null;
        boolean isFromClasspath = false;
        if(!Strings.isNullOrEmpty(deploypath)) {
            metaFile = deploypath + APP_METADATA_FILE;
        } else if(!Strings.isNullOrEmpty(deployfile)) {
            metaFile = deployfile;
        } else {
            isFromClasspath = true;
        }

        Properties properties = new Properties();

        try {
            if(!isFromClasspath) {
                properties.load(new FileInputStream(new File(metaFile)));
            } else {
                InputStream e = this.getClass().getClassLoader().getResourceAsStream(APP_METADATA_FILE);
                if(e == null) {
                    e = this.getClass().getClassLoader().getResourceAsStream(APP_METADATA_FILE_NOHUB);
                }

                InputStreamReader isr = new InputStreamReader(e, "UTF-8");
                properties.load(isr);
            }

            this.applicationName = properties.getProperty(APP_NAME);
            this.id = ipToLong(NetUtil.getIpByHost(NetUtil.getLocalHost())).toString();
            this.appType = AppType.valueOf(properties.getProperty(APP_TYPE).toUpperCase());
            this.owner = properties.getProperty(APP_OWNER);
            this.ownerContact = properties.getProperty(APP_OWNERCONTACT);
            this.description = properties.getProperty(APP_DESCRIPTION);
            this.zhName = properties.getProperty(APP_ZHNAME);
            if(!Strings.isNullOrEmpty(properties.getProperty(APP_PORT))) {
                this.port = Integer.valueOf(properties.getProperty(APP_PORT));
            }

            if(!Strings.isNullOrEmpty(properties.getProperty(APP_HTTP_PORT))) {
                this.httpPort = Integer.valueOf(properties.getProperty(APP_HTTP_PORT));
            }

            this.product = properties.getProperty(PRODUCT);
            this.productCode = properties.getProperty(PRODUCT_CODE);
        } catch (IOException var8) {
            logger.error("CloudContextImpl init error! load [{}] error", metaFile, var8);
            System.exit(-1);
        }

    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
    }

    public void setOwnerContact(String ownerContact) {
        this.ownerContact = ownerContact;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getId() {
        return this.id;
    }

    public String getApplicationZhName() {
        return this.zhName;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getAppType() {
        return this.appType.name();
    }

    public String getOwnerContact() {
        return this.ownerContact;
    }

    public String getDescription() {
        return this.description;
    }

    public int getPort() {
        return this.port;
    }

    public int getHttpPort() {
        return this.httpPort;
    }

    public String getProduct() {
        return this.product;
    }

    public String getProductCode() {
        return this.productCode;
    }

    public void stop() {
    }
}
