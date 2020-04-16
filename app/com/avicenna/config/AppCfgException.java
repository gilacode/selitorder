package com.avicenna.config;

public class AppCfgException extends Exception {

    public AppCfgException(String message) {
        super(message);
    }

    public AppCfgException(String message, Throwable cause) {
        super(message, cause);
    }
}
