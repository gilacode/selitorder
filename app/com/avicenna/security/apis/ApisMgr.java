package com.avicenna.security.apis;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ApisMgr extends SecMgr {

    private final ObjectMapper mapper;
    private final WSClient ws;

    private final AppCfgMgr appCfg;

    @Inject ApisMgr(ObjectMapper mapper, WSClient ws, AppCfgMgr appCfg) {

        this.mapper = mapper;
        this.ws = ws;
        this.appCfg = appCfg;

        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.redirect_uri", "http://localhost:9004/sec/oauth2/redirect", "APIS Redirect URL", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.token_uri", "http://localhost:9000/oauth2/token", "APIS Token URL", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.client_id", "client_id", "APIS Client ID", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.client_secret", "client_secret", "APIS Client Secret", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.resource_id", "resource_id", "APIS Resource Server ID", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.resource_secret", "resource_secret", "APIS Resource Server Secret", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.authorize_url", "http://localhost:9000/oauth2/authorize", "APIS Authorize URL", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.verification_url", "http://localhost:9000/v1/tokeninfo", "APIS Verification URL", "APIS"));
        appCfg.registerConfig(new AppCfgMgr.AppCfgDto("apis.scopes", "read,write", "APIS oAuth Scopes (Comma Separated)", "APIS"));
    }

    @Override
    public void changePassword(String username, String currPassword, String newPassword) throws SecException {

    }

    @Override
    public String resetPassword(String username) throws SecException {
        return null;
    }

    @Override
    public void reloadCurrUser(String username) throws SecException {

    }

    @Override
    public SecUserDto findUser(String username) {
        return null;
    }

    @Override
    public void makeSuperAdmin(String username) {

    }

    @Override
    public void downgradeSuperAdmin(String username) {

    }

    @Override
    public SecUserDto updateProfile(String username, String email, String displayName, String mobileNo, String photoUrl) {
        return null;
    }

    @Override
    public void lockUser(String username) {
    }

    @Override
    public void unlockUser(String username) {
    }

    @Override
    public void activateUser(String username) {

    }

    @Override
    public void deactivateUser(String username) {

    }

    @Override
    public SecUserDto findUserByEmail(String username) {
        return null;
    }

    @Override
    public SecUserDto getCurrentUser(Http.Context ctx) throws SecException {
        try {
            // get access token from session
            // this ise set in SecurityController when FlexAuth perform callback during login
            String accessToken = ctx.session().get("X-AUTH-TOKEN");

            // if token is missing redirect to login page
            if (accessToken==null) {
                return null;
            }

            ApisClient client = getClientSetting();
            if(client==null) {
                ctx.session().remove("X-AUTH-TOKEN");
                throw new SecException("Error getting current user. Apis client settings is null");
            }

            // web service to verify token
            WSResponse wsResponse = ws.url(client.getVerificationUrl().concat("?access_token="+accessToken))
                    .setAuth(client.getResourceId(), client.getResourceSecret())
                    .addHeader("Accept", "application/json")
                    .get().toCompletableFuture().get();
            String jsonResponse = wsResponse.getBody();
            ApisToken tokenResponse = mapper.readValue(jsonResponse, ApisToken.class);

            // if token is missing redirect to login page
            if(tokenResponse==null) {
                ctx.session().remove("X-AUTH-TOKEN");
                throw new SecException("Error getting current user. Token response is null");
            }
            if(tokenResponse!=null && StringUtils.isNotBlank(tokenResponse.getError())) {
                ctx.session().remove("X-AUTH-TOKEN");
                throw new SecException("Error getting current user. Token response contain error. "+tokenResponse.getError());
            }

            // get user name
            ApisPrincipal principal = tokenResponse.getPrincipal();

            // set in cache principal
            SecUserDto secUser = fromPrincipal(principal);

            // return the user name
            return secUser;

        } catch (InterruptedException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception while calling token verification endpoint", e);
        } catch (ExecutionException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception while calling token verification endpoint", e);
        } catch (JsonParseException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception while parsing JSON response from verification endpoint", e);
        } catch (JsonMappingException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception while mappping JSON response from verification endpoint", e);
        } catch (IOException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception while calling verification endpoint", e);
        }
    }

    @Override
    public String loginUrl() throws SecException {

        // get client settings
        ApisClient client = getClientSetting();
        if(client==null) {
            throw new SecException("Error getting login form URL. Apis client settings is null");
        }

        // generate authorization url
        String authUrl = client.getAuthorizationURL() + "?response_type=code&client_id=" + client.getClientId()
                + "&redirect_uri=" + client.getRedirectUri() + "&scope=" + client.getScopes() + "&state=" + client.getClientId();

        return authUrl;
    }

    @Override
    public String logoutUrl() throws SecException {
        return "/sec/oauth2/logout";
    }

    public void oAuthRedirect(Http.Context ctx) throws SecException {

        try {

            // get client settings
            ApisClient client = getClientSetting();

            // validate state first
            boolean stateFound = false;
            final String state = ctx.request().getQueryString("state");
            if (StringUtils.isNotBlank(state) && state.equals(client.getClientId())) {
                stateFound = true;
            }

            // redirect if state does not match
            if (!stateFound) {
                throw new SecException("Exception during oAuth redirect. Authorization  state does not match with any state in this resource server");
            }

            // validate if code exist
            String code = ctx.request().getQueryString("code");
            if (StringUtils.isBlank(code)) {
                throw new SecException("Exception during oAuth redirect. Authorization code is missing from response. Can happen due to new deployment");
            }

            // submit authorization form
            StringBuilder formData = new StringBuilder();
            formData.append("grant_type=authorization_code");
            formData.append("&code=" + code);
            formData.append("&redirect_uri=" + client.getRedirectUri());

            // post form
            String strJsonResp = ws.url(client.getAccessTokenEndPoint())
                    .setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS).toMillis())
                    .setContentType("application/x-www-form-urlencoded")
                    .setAuth(client.getClientId(), client.getClientSecret())
                    .post(formData.toString())
                    .toCompletableFuture().get().getBody();

            // map to Map
            HashMap<String, String> map = mapper.readValue(strJsonResp, HashMap.class);

            // get access token
            String accessToken = map.get("access_token");

            // if access token is blank then throw error
            if (StringUtils.isBlank(accessToken)) {
                throw new SecException("Exception during oAuth redirect. Access token is missing from response");
            }

            // store access token
            ctx.session().put("X-AUTH-TOKEN", accessToken);

        } catch (IOException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception during oAuth redirect. Error while calling access token endpoint", e);
        } catch (ExecutionException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception during oAuth redirect. Error while calling access token endpoint", e);
        } catch (InterruptedException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException("Exception during oAuth redirect. Error while calling access token endpoint", e);
        }
    }

    @Override
    public void createUser(SecUserDto user) throws SecException  {

    }

    @Override
    public List<SecUserDto> getUsers(UserParam param) {
        return null;
    }

    private SecUserDto fromPrincipal(ApisPrincipal principal) {

        SecUserDto currentUser = new SecUserDto();
        currentUser.setUsername(principal.getName());
        currentUser.setDisplayName(principal.getAttribute("displayName"));
        currentUser.setPhotoUrl(principal.getAttribute("photoUrl"));
        currentUser.setEmail(principal.getAttribute("email"));

        return currentUser;
    }

    private ApisClient getClientSetting() {

        // create default settings
        String redirectUri = appCfg.getString("apis.redirect_uri");
        String tokenUri = appCfg.getString("apis.token_uri");
        String clientId = appCfg.getString("apis.client_id");
        String clientSecret = appCfg.getString("apis.client_secret");
        String resourceId = appCfg.getString("apis.resource_id");
        String resourceSecret = appCfg.getString("apis.resource_secret");
        String authorizeUrl = appCfg.getString("apis.authorize_url");
        String verificationUrl = appCfg.getString("apis.verification_url");
        String scopes = appCfg.getString("apis.scopes");

        // validate
        if(StringUtils.isAnyBlank(redirectUri, tokenUri, clientId, clientSecret,
                resourceId, resourceSecret, authorizeUrl, verificationUrl, scopes)) {
            return null;
        }

        // get client config
        ApisClient client = new ApisClient(tokenUri, clientId, clientSecret,
                resourceId, resourceSecret, authorizeUrl, redirectUri, verificationUrl, scopes);

        return client;
    }
}
