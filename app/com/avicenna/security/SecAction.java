package com.avicenna.security;

import com.google.inject.Inject;
import play.Logger;
import play.cache.SyncCacheApi;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class SecAction extends Security.Authenticator {

    private final SecMgr secMgr;

    @Inject SecAction(SecMgr secMgr) {
        this.secMgr = secMgr;
    }

    @Override
    public String getUsername(Context ctx) {

        try {

            SecUserDto secUser = this.secMgr.getCurrentUser(ctx);

            if (secUser != null) {
                return secUser.getUsername();
            }
        } catch (SecException e) {
            Logger.error(this.getClass().getSimpleName(), e);
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {

        try {
            String loginFormUrl = this.secMgr.loginUrl();
            return redirect(loginFormUrl);
        } catch (SecException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            return redirect("/");
        }
    }
}