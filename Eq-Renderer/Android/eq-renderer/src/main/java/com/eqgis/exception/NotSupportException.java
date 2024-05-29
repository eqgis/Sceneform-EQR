package com.eqgis.exception;

/**
 * 异常:功能不支持
 **/
public class NotSupportException extends RuntimeException{
    public NotSupportException() {
        super("Not Support.");
    }

    public NotSupportException(String message) {
        super(message);
    }

    public NotSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportException(Throwable cause) {
        super(cause);
    }
}
