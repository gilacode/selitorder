package com.avicenna.notification;

public class NotifException extends Exception {

    public NotifException(String message) {
        super(message);
    }

    public NotifException(String message, Throwable cause) {
        super(message, cause);
    }
}
