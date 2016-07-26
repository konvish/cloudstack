package com.kong.cloudstack.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties文件读取
 * Created by kong on 2016/1/22.
 */
public class FileLoader {
    private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);

    public FileLoader() {
    }

    /**
     * 涉及到支持 junit测试   文件路径会自动  进行  /PP/PP  和 PP/PP的匹配
     *
     * @param file  文件路径，传入需要带 /前缀
     * @return
     */
    public static Properties getFile(String file) {
        Properties properties = new Properties();

        try {
            if(!Strings.isNullOrEmpty(file)) {//没有配置 从classloader获取
                InputStream e = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
                if(e == null) {
                    file = file.substring(1);
                    e = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
                }

                properties.load(e);
            }
        } catch (IOException var3) {
            logger.error("load properties {} error", file, var3);
            System.exit(-1);
        }

        return properties;
    }
}