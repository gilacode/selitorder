package com.avicenna.apiclient;

import com.avicenna.util.LangUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiErrorResp {

    private final String errorCode;
    private final String errorMessage;

    @JsonCreator
    public ApiErrorResp(
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("errorMessage") String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ApiErrorResp(ApiErrCode errCode, LangUtil langUtil) {
        this.errorCode = errCode.name();
        this.errorMessage = langUtil.at(errCode.getMessageKey());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
