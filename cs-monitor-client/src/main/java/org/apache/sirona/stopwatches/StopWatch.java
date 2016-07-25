package org.apache.sirona.stopwatches;

/**
 * Created by kong on 2016/1/24.
 */
public interface StopWatch {
    long getElapsedTime();

    StopWatch stop();
}
