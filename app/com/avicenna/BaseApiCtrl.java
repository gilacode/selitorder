package com.avicenna;

import com.avicenna.apiclient.ApiClientAction;
import com.avicenna.monitor.Monitor;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecAction;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.template.TmplMgr;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Security;

@Security.Authenticated(ApiClientAction.class)
@Monitor
public abstract class BaseApiCtrl extends Controller {

    protected final SecMgr secMgr;

    public BaseApiCtrl(SecMgr secMgr) {

        this.secMgr = secMgr;

    }

    protected SecUserDto currentUser() {

        try {

            SecUserDto user = secMgr.getCurrentUser(ctx());

            if(user!=null) {
                return user;
            }

            String clientId = ctx().flash().get("clientId");

            if(StringUtils.isNotBlank(clientId)) {

                user = secMgr.findUser(clientId);

                if(user!=null) {
                    return user;
                }
            }

        } catch (SecException e) {
            Logger.error(this.getClass().getSimpleName(), e);
        }

        return null;
    }
}
