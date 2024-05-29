package com.eqgis.ar.exceptions;

import android.util.Log;

/**
 * AR相机异常
 **/
public class ARCameraException extends Exception{
    public ARCameraException() {
        super();
    }

    public ARCameraException(String message) {
        super(message);
        Log.e(ARCameraException.class.getSimpleName(), "ARCameraException: \n"+message);
    }

    public ARCameraException(String message, Throwable cause) {
        super(message, cause);
        Log.e(ARCameraException.class.getSimpleName(), "ARCameraException: ",cause);
    }

    public ARCameraException(Throwable cause) {
        super(cause);
        Log.e(ARCameraException.class.getSimpleName(), "ARCameraException: ",cause);
    }
}
