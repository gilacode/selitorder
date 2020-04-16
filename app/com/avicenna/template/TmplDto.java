package com.avicenna.template;

import com.avicenna.nav.NavMgr;
import com.avicenna.util.NotifPrv;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TmplDto {

    private final NavMgr navMgr;

    private final String title;

    private final String logoUrlDark;
    private final String logoUrlWhite;
    private final String spinnerUrl;
    private final String appName;
    private final String headerColor;
    private final String primaryColor;

    private final String version;
    private final String serverInstanceId;

    private final String cssPath;
    private final String jsPath;

    public TmplDto(NavMgr navMgr, String title, String logoUrlDark, String logoUrlWhite, String spinnerUrl, String appName,
                   String headerColor, String primaryColor,
                   String version, String serverInstanceId,
                   String cssPath, String jsPath) {
        this.navMgr = navMgr;
        this.title = title;
        this.logoUrlDark = logoUrlDark;
        this.logoUrlWhite = logoUrlWhite;
        this.spinnerUrl = spinnerUrl;
        this.appName = appName;
        this.headerColor = headerColor;
        this.primaryColor = primaryColor;
        this.version = version;
        this.serverInstanceId = serverInstanceId;
        this.cssPath = cssPath;
        this.jsPath = jsPath;
    }

    public List<NotifPrv.Notification> getNotifications(Http.Context ctx) {
        String json = ctx.flash().get("NOTIFICATION");
        try{
            if(StringUtils.isNotBlank(json)) {
                return ListUtils.emptyIfNull(Json.newDefaultMapper()
                        .readValue(json, new TypeReference<List<NotifPrv.Notification>>() {}));
            }
        } catch(JsonProcessingException e){
            Logger.error("Error while converting notification JSON to object. "+json);
        } catch (IOException e) {
            Logger.error("Error while converting notification JSON to object. "+json);
        }
        return Collections.EMPTY_LIST;
    }

    public String getGrid(int size) {
        String xs = "col-12";
        String sm = "col-sm-12";
        String md = "col-md-"+size;
        String lg = "col-lg-"+size;
        String xl = "col-xl-"+size;
        return xs + " " + sm + " " + md + " " + lg + " " + xl;
    }

    public String clean(String unclean) {

        if(StringUtils.isBlank(unclean)) {
            return "";
        }

        return unclean.replaceAll("/[^A-Za-z0-9 ]/", "_");
    }

    public String getTitle() {
        return title;
    }

    public String getLogoUrlDark() {
        return logoUrlDark;
    }

    public String getLogoUrlWhite() {
        return logoUrlWhite;
    }

    public String getSpinnerUrl() {
        return spinnerUrl;
    }

    public String getAppName() {
        return appName;
    }

    public String getHeaderColor() {
        return headerColor;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getVersion() {
        return version;
    }

    public String getServerInstanceId() {
        return serverInstanceId;
    }

    public String getCssPath() {
        return cssPath;
    }

    public String getJsPath() {
        return jsPath;
    }

    public NavMgr getNavMgr() {
        return navMgr;
    }
}
