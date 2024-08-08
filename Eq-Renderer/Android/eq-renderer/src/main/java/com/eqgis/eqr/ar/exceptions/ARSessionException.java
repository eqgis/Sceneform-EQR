package com.eqgis.eqr.ar.exceptions;

import android.util.Log;

/**
 * AR Session异常
 **/
public class ARSessionException extends Exception{
    public ARSessionException() {
        super();
    }

    public ARSessionException(String message) {
        super(message);
        Log.e(ARSessionException.class.getSimpleName(), "ARSessionException: \n" + message);
    }

    public ARSessionException(String message, Throwable cause) {
        super(message, cause);
        Log.e(ARSessionException.class.getSimpleName(), "ARSessionException: ", cause);
    }

    public ARSessionException(Throwable cause) {
        super(cause);
        Log.e(ARSessionException.class.getSimpleName(), "ARSessionException: ", cause);
    }
}
