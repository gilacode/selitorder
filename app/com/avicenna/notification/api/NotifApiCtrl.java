package com.avicenna.notification.api;

import com.avicenna.BaseCtrl;
import com.avicenna.apiclient.ApiErrCode;
import com.avicenna.apiclient.ApiErrorResp;
import com.avicenna.nav.NavMgr;
import com.avicenna.notification.NotifMgr;
import com.avicenna.notification.html.NotifDetail;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class NotifApiCtrl extends BaseCtrl {

    private final NotifMgr notifMgr;

    private final LangUtil langUtil;
    private final DateTimeUtil dateTimeUtil;

    private final NotifPrv notifPrv;

    @Inject
    public NotifApiCtrl(TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr, NotifMgr notifMgr,
                        LangUtil langUtil, DateTimeUtil dateTimeUtil, NotifPrv notifPrv) {
        super(tmplMgr, secMgr, navMgr);

        this.notifMgr = notifMgr;

        this.langUtil = langUtil;
        this.dateTimeUtil = dateTimeUtil;

        this.notifPrv = notifPrv;
    }

    public Result viewDetail(String uuid) {

        SecUserDto currUser = currentUser();

        NotifMgr.NotifDto notif = notifMgr.find(uuid);

        return ok(NotifDetail.render(currUser, notif,
                tmplMgr.getTmpl(langUtil.at("notif.title.notification")), dateTimeUtil));
    }

    public Result get(String username) {

        Boolean alreadyRead = null;
        if(StringUtils.isNotBlank(request().getQueryString("onlyNew"))
                && request().getQueryString("onlyNew").equals("true")) {
            alreadyRead = false;
        }

        List<NotifMgr.NotifDto> notifs = ListUtils.emptyIfNull(
                notifMgr.get(null, null, username, alreadyRead, 30));

        if(notifs.size() == 0) {
            return notFound(Json.toJson(new ApiErrorResp(ApiErrCode.NOTE001, langUtil)));
        }

        return ok(Json.toJson(notifs));
    }

    public Result getByCategory(String username, String strCategory) {

        Boolean alreadyRead = null;
        if(StringUtils.isNotBlank(request().getQueryString("onlyNew"))
                && request().getQueryString("onlyNew").equals("true")) {
            alreadyRead = false;
        }

        NotifMgr.ChannelCategory category = NotifMgr.ChannelCategory.valueOf(strCategory);

        List<NotifMgr.NotifDto> notifs = ListUtils.emptyIfNull(
                notifMgr.get(category, null, username, alreadyRead, 30));

        if(notifs.size() == 0) {
            return notFound(Json.toJson(new ApiErrorResp(ApiErrCode.NOTE001, langUtil)));
        }

        return ok(Json.toJson(notifs));
    }
}
