package com.avicenna.apiclient;

import com.avicenna.security.SecAction;
import com.avicenna.security.SecMgr;
import com.avicenna.util.LangUtil;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.SyncCacheApi;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ApiClientAction extends Security.Authenticator {

    private final SyncCacheApi cache;

    private final SecMgr secMgr;
    private final ApiClientMgr apiClientMgr;

    private final LangUtil langUtil;

    private final SecAction secAction;

    @Inject
    ApiClientAction(SyncCacheApi cache, SecMgr secMgr, ApiClientMgr apiClientMgr, SecAction secAction, LangUtil langUtil) {

        this.cache = cache;

        this.secMgr = secMgr;
        this.apiClientMgr = apiClientMgr;

        this.secAction = secAction;

        this.langUtil = langUtil;
    }

    @Override
    public String getUsername(Context ctx) {

        String username = secAction.getUsername(ctx);

        if(username!=null) {
            return username;
        }

        String clientId = ctx.request().header("X-CLIENT-ID").orElse(null);
        if(StringUtils.isBlank(clientId)) {
            return null;
        }

        ApiClientMgr.ApiClientDto dto = apiClientMgr.find(clientId);
        if(dto == null) {
            return null;
        }

        String timestamp = ctx.request().header("X-TIMESTAMP").orElse(null);
        if(StringUtils.isBlank(timestamp)) {
            return null;
        }

        String clientAuthKey = ctx.request().header("X-AUTH-TOKEN").orElse(null);
        if(StringUtils.isBlank(clientAuthKey)) {
            return null;
        }

        String serverAuthKey = encode(clientId, dto.getClientSecret(), timestamp);
        if(!serverAuthKey.equals(clientAuthKey)) {
            return null;
        }

        ctx.flash().put("clientId", clientId);
        return dto.getClientId();
    }

    private String encode(String apiClientId, String apiClientSecret, String timestamp) {

        try {

            SecretKeySpec secretKey = new SecretKeySpec(apiClientSecret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            String dataSig = apiClientId + "&" + timestamp;
            byte[] hmacData = mac.doFinal(dataSig.getBytes("UTF-8"));
            Base64.Encoder encoder = Base64.getEncoder();

            return encoder.encodeToString(hmacData);

        } catch (NoSuchAlgorithmException e) {

            Logger.error(this.getClass().getSimpleName(), e);
        } catch (UnsupportedEncodingException e) {

            Logger.error(this.getClass().getSimpleName(), e);
        } catch (InvalidKeyException e) {

            Logger.error(this.getClass().getSimpleName(), e);
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return unauthorized(Json.toJson(new ApiErrorResp(ApiErrCode.SECE001, langUtil)));
    }
}