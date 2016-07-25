package org.apache.sirona.tracking;

import org.apache.sirona.tracking.Context;
/**
 * Created by kong on 2016/1/24.
 */
public interface PathTrackingInvocationListener {
    void startPath(Context var1);

    void enterMethod(Context var1);

    void exitMethod(Context var1);

    void endPath(Context var1);
}
