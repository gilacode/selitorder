package com.avicenna.config;

import com.avicenna.BaseCtrl;
import com.avicenna.config.html.AppCfgMain;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class AppCfgCtrl extends BaseCtrl {

    private final LangUtil langUtil;

    private final FormFactory formFact;

    private final AppCfgMgr appCfgMgr;

    private final NotifPrv notifPrv;

    @Inject AppCfgCtrl(LangUtil langUtil, FormFactory formFact, AppCfgMgr appCfgMgr, SecMgr secMgr, NavMgr navMgr, TmplMgr tmplMgr, NotifPrv notifPrv) {

        super(tmplMgr, secMgr, navMgr);

        this.langUtil = langUtil;

        this.formFact = formFact;

        this.appCfgMgr = appCfgMgr;
        this.tmplMgr = tmplMgr;
        this.notifPrv = notifPrv;
    }

    public Result configs() {

        if(navMgr.hasPermission(AppCfgNav.Permission.SYS_CFG_VIEW_PAGE.name(), currentUser())) {

            List<String> groupNames = appCfgMgr.getGroups();

            if(groupNames.isEmpty()) {
                groupNames = new ArrayList<>();
            }

            String groupName = "";
            if(!groupNames.isEmpty()) {
                groupName = groupNames.get(0);
            }

            return showMain(groupName);

        } else {

            notifPrv.get().addError(langUtil.at("config.error.viewcfg.nopermission")).flash(ctx());
            return redirect("/");

        }
    }

    public Result config(String groupName) {

        if(navMgr.hasPermission(AppCfgNav.Permission.SYS_CFG_VIEW_PAGE.name(), currentUser())) {

            if(StringUtils.isBlank(groupName)) {
                notifPrv.get().addError(langUtil.at("config.error.viewcfg.groupblank")).flash(ctx());
                redirect(routes.AppCfgCtrl.configs());
            }

            List<String> groupNames = appCfgMgr.getGroups();

            if(groupNames.isEmpty()) {
                notifPrv.get().addError(langUtil.at("config.error.viewcfg.groupnotfound")).flash(ctx());
                redirect(routes.AppCfgCtrl.configs());
            }

            return showMain(groupName);

        } else {

            notifPrv.get().addError(langUtil.at("config.error.viewcfg.nopermission")).flash(ctx());
            return redirect("/");

        }
    }

    public Result saveConfig(String groupName) {

        if(navMgr.hasPermission(AppCfgNav.Permission.SYS_CFG_UPDATE_CONFIG.name(), currentUser())) {

            if (StringUtils.isBlank(groupName)) {
                notifPrv.get().addError(langUtil.at("config.error.savecfg.grpnameblank")).flash(ctx());
                redirect(routes.AppCfgCtrl.configs());
            }

            AppCfgMgr.AppCfgGrpDto group = appCfgMgr.getGroup(groupName);
            List<AppCfgMgr.AppCfgDto> props = ListUtils.emptyIfNull(group.getProperties());

            List<AppCfgMgr.AppCfgDto> newProps = new ArrayList<>();

            for (AppCfgMgr.AppCfgDto prop : props) {

                DynamicForm form = formFact.form().bindFromRequest();
                String newValue = form.get(clean(prop.getKey()));

                if (!StringUtils.equals(prop.getValue(), newValue)) {
                    newProps.add(new AppCfgMgr.AppCfgDto(prop.getKey(), newValue, prop.getParentKey(), prop.getDesc()));
                }
            }

            group.getProperties().clear();
            group.getProperties().addAll(newProps);

            appCfgMgr.updateGroup(group);

            notifPrv.get().addSuccess(langUtil.at("config.success.savecfg.cfggrpupd", groupName)).flash(ctx());
            return redirect(routes.AppCfgCtrl.config(groupName));

        } else {

            notifPrv.get().addError(langUtil.at("config.error.savecfg.permupdcfg")).flash(ctx());
            return redirect("/");

        }
    }

    public Result clearCache(String groupName) {

        if(navMgr.hasPermission(AppCfgNav.Permission.SYS_CFG_CLEAR_CACHE.name(), currentUser())) {

            if (StringUtils.isBlank(groupName)) {
                notifPrv.get().addError(langUtil.at("config.error.clearcache.grpnameblank")).flash(ctx());
                redirect(routes.AppCfgCtrl.config(groupName));
            }

            appCfgMgr.clearCache(groupName);

            notifPrv.get().addSuccess("Cache cleared").flash(ctx());
            return redirect(routes.AppCfgCtrl.config(groupName));

        } else {

            notifPrv.get().addError(langUtil.at("config.error.clearcache.nopermission")).flash(ctx());
            return redirect("/");

        }
    }

    public String clean(String unclean) {

        if(StringUtils.isBlank(unclean)) {
            return "";
        }

        return unclean.replaceAll("/[^A-Za-z0-9 ]/", "_");
    }

    private Result showMain(String groupName) {

        TmplDto tmpl = tmplMgr.getTmpl(langUtil.at("config.title.cfgform", groupName), "js/appcfg/appcfg.js", "css/appcfg/appcfg.css");
        return ok(AppCfgMain.render(appCfgMgr.getGroup(groupName), tmpl, currentUser()));
    }

}
