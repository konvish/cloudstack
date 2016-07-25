package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.dynconfig.domain.Configuration;
import java.util.concurrent.Executor;
/**
 * 数据改变监听器
 * Created by kong on 2016/1/24.
 */
public interface IChangeListener {
    /**
     * 返回线程池执行器
     * @return
     */
    Executor getExecutor();

    /**
     * 接收到配置文件处理
     * @param configuration
     */
    void receiveConfigInfo(Configuration configuration);
}

