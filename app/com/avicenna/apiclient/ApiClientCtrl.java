package com.avicenna.apiclient;

import com.avicenna.BaseCtrl;
import com.avicenna.logger.LogMgr;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.IdUtil;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;
import com.avicenna.apiclient.html.ApiClientList;

import java.util.List;

/**
 * Notification API
 */
public class ApiClientCtrl extends BaseCtrl {

    private final ApiClientMgr apiClientMgr;
    private final LogMgr logMgr;

    private final NotifPrv notifPrv;
    private final LangUtil langUtil;
    private final DateTimeUtil dateTimeUtil;
    private final IdUtil idUtil;

    private final FormFactory formFactory;

    @Inject
    public ApiClientCtrl(TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr, ApiClientMgr apiClientMgr, LogMgr logMgr,
                         NotifPrv notifPrv, LangUtil langUtil, DateTimeUtil dateTimeUtil, IdUtil idUtil, FormFactory formFactory) {
        super(tmplMgr, secMgr, navMgr);

        this.apiClientMgr = apiClientMgr;
        this.logMgr = logMgr;

        this.notifPrv = notifPrv;
        this.langUtil = langUtil;
        this.dateTimeUtil = dateTimeUtil;
        this.idUtil = idUtil;

        this.formFactory = formFactory;
    }

    public Result getApiClients() {

        if(navMgr.hasPermission(ApiClientNav.Permission.APC_LIST_VIEW.name(), currentUser())) {

            List<ApiClientMgr.ApiClientDto> apiClients = ListUtils.emptyIfNull(apiClientMgr.get(ApiClientMdl.ClientStatus.ACTIVE, ApiClientMdl.ClientStatus.SUSPEND));

            TmplDto tmpl = tmplMgr.getTmpl(langUtil.at("apiclient.list.table"), "js/apiclient/apiclient.js", null);
            return ok(ApiClientList.render(currentUser(), apiClients, tmpl, dateTimeUtil));

        }  else {

            notifPrv.get().addError("You do not have permission to view API clients").flash(ctx());
            return redirect("/");
        }
    }

    public Result saveApiClient() {

        try {
            if (navMgr.hasPermission(ApiClientNav.Permission.APC_LIST_CREATE_UPDATE.name(), currentUser())) {

                DynamicForm form = formFactory.form();
                form = form.bindFromRequest();

                String clientId = form.get("clientId");

                if (StringUtils.isBlank(clientId)) {
                    notifPrv.get().addError("Client Id cannot be blank").flash(ctx());
                    return redirect(routes.ApiClientCtrl.getApiClients());
                }

                ApiClientMgr.ApiClientDto dto = apiClientMgr.find(clientId);

                if(dto!=null) {

                    notifPrv.get().addError("The Client Id selected already exist").flash(ctx());
                    return redirect(routes.ApiClientCtrl.getApiClients());
                }

                String email = form.get("email");

                if (StringUtils.isBlank(email)) {
                    notifPrv.get().addError("Email cannot be blank").flash(ctx());
                    return redirect(routes.ApiClientCtrl.getApiClients());
                }

                dto = new ApiClientMgr.ApiClientDto(clientId, idUtil.getShortUniqueId(), email, ApiClientMdl.ClientStatus.ACTIVE, currentUser());

                apiClientMgr.create(dto);

                logMgr.debug("API Client", clientId, "Client Id "+clientId+" created");

                return redirect(routes.ApiClientCtrl.getApiClients());

            } else {

                notifPrv.get().addError("You do not have permission to view API clients").flash(ctx());
                return redirect("/");
            }
        } catch (ApiClientException e) {

            notifPrv.get().addError("Error while creating API Client").flash(ctx());
            return redirect("/");
        }
    }

    public Result deleteApiClient(String clientId) {

        if (navMgr.hasPermission(ApiClientNav.Permission.APC_LIST_CREATE_UPDATE.name(), currentUser())) {

            apiClientMgr.deactivate(clientId);

            logMgr.debug("API Client", clientId, "Client Id "+clientId+" deleted");

            return redirect(routes.ApiClientCtrl.getApiClients());

        } else {

            notifPrv.get().addError("You do not have permission to view API clients").flash(ctx());
            return redirect("/");
        }
    }

    public Result lockApiClient(String clientId) {

        if (navMgr.hasPermission(ApiClientNav.Permission.APC_LIST_CREATE_UPDATE.name(), currentUser())) {

            apiClientMgr.lock(clientId);

            logMgr.debug("API Client", clientId, "Client Id "+clientId+" locked");

            return redirect(routes.ApiClientCtrl.getApiClients());

        } else {

            notifPrv.get().addError("You do not have permission to lock API clients").flash(ctx());
            return redirect("/");
        }
    }

    public Result unlockApiClient(String clientId) {

        if (navMgr.hasPermission(ApiClientNav.Permission.APC_LIST_CREATE_UPDATE.name(), currentUser())) {

            apiClientMgr.unlock(clientId);

            logMgr.debug("API Client", clientId, "Client Id "+clientId+" unlocked");

            return redirect(routes.ApiClientCtrl.getApiClients());

        } else {

            notifPrv.get().addError("You do not have permission to unlock API clients").flash(ctx());
            return redirect("/");
        }
    }
}
