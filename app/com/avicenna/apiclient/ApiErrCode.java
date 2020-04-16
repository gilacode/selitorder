package com.avicenna.apiclient;

public enum ApiErrCode {

    /* Security */

    SECE001("notif.error.zeronotifs"),

    /* Notification */

    NOTE001("notif.error.zeronotifs")

    ;

    private final String messageKey;

    ApiErrCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
