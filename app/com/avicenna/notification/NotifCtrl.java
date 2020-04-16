package com.avicenna.notification;

import com.avicenna.BaseCtrl;
import com.avicenna.apiclient.ApiErrCode;
import com.avicenna.apiclient.ApiErrorResp;
import com.avicenna.nav.NavMgr;
import com.avicenna.notification.html.NotifDetail;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class NotifCtrl extends BaseCtrl {

    private final NotifMgr notifMgr;

    private final LangUtil langUtil;
    private final DateTimeUtil dateTimeUtil;

    private final NotifPrv notifPrv;

    @Inject
    public NotifCtrl(TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr, NotifMgr notifMgr,
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

        notifMgr.setAlreadyRead(uuid);

        return ok(NotifDetail.render(currUser, notif,
                tmplMgr.getTmpl(langUtil.at("notif.title.notification")), dateTimeUtil));
    }
}
