package com.avicenna.security.apis;

import com.avicenna.monitor.Monitor;
import com.avicenna.notification.NotifMgr;
import com.avicenna.security.SecCtrl;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Controller;
import play.mvc.Result;

@Monitor
public class ApisCtrl extends Controller {

    private final SecMgr secMgr;

    @Inject ApisCtrl(SecMgr secMgr) {
        this.secMgr = secMgr;
    }

    public Result oauth2() throws SecException {
        ((ApisMgr) this.secMgr).oAuthRedirect(ctx());
        return redirect("/");
    }

    public Result logout() {
        String token = ctx().session().get("X-AUTH-TOKEN");
        if(StringUtils.isNotBlank(token)) {
            ctx().session().remove("X-AUTH-TOKEN");
        }
        return redirect("/");
    }

}
