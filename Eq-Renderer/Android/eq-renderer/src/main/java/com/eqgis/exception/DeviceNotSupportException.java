package com.eqgis.exception;

/**
 * 异常:设备不支持
 **/
public class DeviceNotSupportException extends Exception{
    public DeviceNotSupportException() {
        super("Not Support.");
    }

    public DeviceNotSupportException(String message) {
        super(message);
    }

    public DeviceNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotSupportException(Throwable cause) {
        super(cause);
    }
}
