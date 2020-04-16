package com.avicenna.encryption;

public class EncException extends Exception {

    public EncException(String message) {
        super(message);
    }

    public EncException(String message, Throwable cause) {
        super(message, cause);
    }
}
