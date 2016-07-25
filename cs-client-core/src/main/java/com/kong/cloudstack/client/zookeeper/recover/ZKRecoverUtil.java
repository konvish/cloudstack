package com.kong.cloudstack.client.zookeeper.recover;

import com.google.common.io.Files;
import com.kong.cloudstack.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
/**
 * Created by kong on 2016/1/22.
 */
public class ZKRecoverUtil {
    public static final Logger logger = LoggerFactory.getLogger(ZKRecoverUtil.class);

    public ZKRecoverUtil() {
    }

    public static void doRecover(final byte[] content, final String path, final ConcurrentMap<String, Object> recoverDataCache) {
        Executors.newSingleThreadExecutor(new NamedThreadFactory("DYN-CONFIG-RECOVER")).execute(new Runnable() {
            public void run() {
                try {
                    Object e = recoverDataCache.putIfAbsent(path, content);
                    String parentDir;
                    File recoveryFile;
                    if(e == null) {
                        parentDir = System.getProperty("java.io.tmpdir");
                        recoveryFile = new File(parentDir + path);
                        Files.createParentDirs(recoveryFile);
                        Files.write(content, recoveryFile);
                    }

                    if(e != null && content != e) {
                        parentDir = System.getProperty("java.io.tmpdir");
                        recoveryFile = new File(parentDir + path);
                        Files.createParentDirs(recoveryFile);
                        Files.write(content, recoveryFile);
                    }

                    ZKRecoverUtil.logger.warn("path {} recover data", path);
                } catch (Exception var4) {
                    ZKRecoverUtil.logger.error("DYN-CONFIG-RECOVER error", var4);
                }

            }
        });
    }

    public static byte[] loadRecoverData(String path) throws IOException {
        String parentDir = System.getProperty("java.io.tmpdir");
        File recoveryFile = new File(parentDir + path);
        if(!recoveryFile.exists()) {
            Files.createParentDirs(recoveryFile);
        }

        return Files.toByteArray(recoveryFile);
    }
}