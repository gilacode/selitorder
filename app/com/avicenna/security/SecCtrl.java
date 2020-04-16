package com.avicenna.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

public class SecCtrl extends Controller {

    private final SecMgr secMgr;

    @Inject SecCtrl(SecMgr secMgr) {
        this.secMgr = secMgr;
    }

    public Result login() throws SecException {
        String loginFormUrl = this.secMgr.loginUrl();
        return redirect(loginFormUrl);
    }

    public Result logout() throws SecException {
        String logoutUrl = this.secMgr.logoutUrl();
        return redirect(logoutUrl);
    }
}
