package com.avicenna.security;

public class SecException extends Exception {

    public SecException(String message) {
        super(message);
    }

    public SecException(String message, Throwable cause) {
        super(message, cause);
    }
}
