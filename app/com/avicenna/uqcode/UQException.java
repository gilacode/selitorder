package com.avicenna.uqcode;

public class UQException extends Exception {

    public UQException(String message) {
        super(message);
    }

    public UQException(String message, Throwable cause) {
        super(message, cause);
    }
}
