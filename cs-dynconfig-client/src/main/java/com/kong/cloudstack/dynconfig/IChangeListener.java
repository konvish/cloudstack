package com.kong.cloudstack.dynconfig;

import com.kong.cloudstack.dynconfig.domain.Configuration;
import java.util.concurrent.Executor;
/**
 * Created by kong on 2016/1/24.
 */
public interface IChangeListener {
    Executor getExecutor();

    void receiveConfigInfo(Configuration var1);
}

