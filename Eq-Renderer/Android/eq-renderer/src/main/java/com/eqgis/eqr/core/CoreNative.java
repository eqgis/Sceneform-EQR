package com.eqgis.eqr.core;

import androidx.annotation.Keep;

/**
 * @author tanyx 2023/6/19
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/

@Keep
class CoreNative {
    public native static String jni_GetVersion();

    public native static boolean jni_CheckCoreStatus();

    public native static String jni_GetFilamentVersion();
}
