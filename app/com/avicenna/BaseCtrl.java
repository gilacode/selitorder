package com.avicenna;

import com.avicenna.monitor.Monitor;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecAction;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.apis.ApisClient;
import com.avicenna.security.apis.ApisPrincipal;
import com.avicenna.security.apis.ApisToken;
import com.avicenna.template.TmplMgr;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Security;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Security.Authenticated(SecAction.class)
@Monitor
public abstract class BaseCtrl extends Controller {

    protected TmplMgr tmplMgr;
    protected SecMgr secMgr;
    protected NavMgr navMgr;

    public BaseCtrl(TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr) {
        this.tmplMgr = tmplMgr;
        this.secMgr = secMgr;
        this.navMgr = navMgr;
    }

    protected SecUserDto currentUser() {
        try {
            return secMgr.getCurrentUser(ctx());
        } catch (SecException e) {
            return null;
        }
    }
}
