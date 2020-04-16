package com.avicenna.nav;

import com.avicenna.BaseCtrl;
import com.avicenna.config.AppCfgMgr;
import com.avicenna.nav.html.NavTmplView;
import com.avicenna.nav.html.NavUserView;
import com.avicenna.notification.NotifMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.SecUserMdl;
import com.avicenna.security.basic.BasicUserCtrl;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import java.util.List;
import java.util.stream.Collectors;

public class NavCtrl extends BaseCtrl {

    private FormFactory formFact;

    private final NavMgr navMgr;
    private final NotifMgr notifMgr;

    private final NotifPrv notifPrv;

    private BasicUserCtrl userCtrl;

    @Inject
    NavCtrl(FormFactory formFact, NavMgr navMgr, SecMgr secMgr, NotifMgr notifMgr, TmplMgr tmplMgr, NotifPrv notifPrv, BasicUserCtrl userCtrl) {

        super(tmplMgr, secMgr, navMgr);

        this.formFact = formFact;

        this.navMgr = navMgr;
        this.notifMgr = notifMgr;

        this.notifPrv = notifPrv;

        this.userCtrl = userCtrl;
    }

    public Result UserList() {
        return userCtrl._showUsers(NavMgr.Permission.NAV_ACL_VIEW_ACL.name(), true,
                "/nav/users", "/nav", SecUserMdl.SecUserType.USER, SecUserMdl.SecUserType.API_USER);
    }

    public Result showUserNav(String username) {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_VIEW_ACL.name(), currentUser())) {

            SecUserDto selectedUser = secMgr.findUser(username);

            if(selectedUser==null) {
                notifPrv.get().addError("User "+username+" does not exist").flash(ctx());
                return redirect("/");
            }

            final List<NavRegistry.HeadDto> allHeads = ListUtils.emptyIfNull(navMgr.getActiveLeftSideMenus());

            final List<NavRegistry.HeadDto> ownedHeads = ListUtils.emptyIfNull(navMgr.getLeftSideMenusByUser(selectedUser));
            final List<String> ownedPerms = extractPermCodes(ownedHeads);

            allHeads.stream().forEach(ah -> {
                ah.getMenus().stream().forEach(m -> {
                    m.getPerms().stream().forEach(p -> {
                        if(ownedPerms.contains(p.getCode())) {
                            p.setChecked(true);
                        }
                    });
                });
            });


            TmplDto tmpl = tmplMgr.getTmpl("Access Level", "js/nav/nav.js", "css/nav/nav.css");
            return ok(NavUserView.render(currentUser(), navMgr.getTemplates(), allHeads, selectedUser, tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view access level configuration").flash(ctx());
            return redirect("/");

        }
    }

    public Result saveUserNav() {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_UPDATE_ACL.name(), currentUser())) {

            final DynamicForm form = formFact.form().bindFromRequest();

            List<NavRegistry.HeadDto> heads = ListUtils.emptyIfNull(navMgr.getLeftSideMenusByUser(currentUser()));

            List<String> allPermCodes = extractPermCodes(heads);

            List<String> selectedPermCodes = ListUtils.emptyIfNull(allPermCodes.stream()
                    .filter(p -> StringUtils.isNotBlank(form.get(p)))
                    .collect(Collectors.toList()));

            String selectedUser = form.get("selectedUser");
            navMgr.updateUserPermissions(selectedUser, selectedPermCodes);

            notifMgr.send("Navigation", currentUser().getUsername(), selectedUser,
                    "Permission Updated", "Your permission has been updated by "+currentUser().getDisplayName(),
                    NotifMgr.ChannelCategory.WEB);

            notifPrv.get().addSuccess("Access level updated").flash(ctx());
            return redirect(routes.NavCtrl.showUserNav(selectedUser));

        } else {

            notifPrv.get().addError("You do not have permission to update access level configuration").flash(ctx());
            return redirect("/");

        }

    }

    public Result newTmplNav() {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_VIEW_TMPL.name(), currentUser())) {

            final List<NavRegistry.HeadDto> allHeads = ListUtils.emptyIfNull(navMgr.getActiveLeftSideMenus());

            TmplDto tmpl = tmplMgr.getTmpl("Access Level", "js/nav/nav.js", "css/nav/nav.css");
            return ok(NavTmplView.render(allHeads, navMgr.getTemplates(), null, tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view ACL template").flash(ctx());
            return redirect("/");

        }
    }

    public Result showTmplNav(String templateName) {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_VIEW_TMPL.name(), currentUser())) {

            final List<NavRegistry.HeadDto> allHeads = ListUtils.emptyIfNull(navMgr.getActiveLeftSideMenus());

            if(StringUtils.isNotBlank(templateName)) {

                final List<NavRegistry.HeadDto> templHeads = ListUtils.emptyIfNull(navMgr.getLeftSideMenusByTemplate(templateName));
                final List<String> templPermCodes = extractPermCodes(templHeads);

                allHeads.stream().forEach(ah -> {
                    ah.getMenus().stream().forEach(m -> {
                        m.getPerms().stream().forEach(p -> {
                            if (templPermCodes.contains(p.getCode())) {
                                p.setChecked(true);
                            }
                        });
                    });
                });

            }

            TmplDto tmpl = tmplMgr.getTmpl("Access Level", "js/nav/nav.js", "css/nav/nav.css");
            return ok(NavTmplView.render(allHeads, navMgr.getTemplates(), templateName, tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view ACL template").flash(ctx());
            return redirect("/");

        }
    }

    public Result copyTmplNav(String username, String templateName) {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_UPDATE_ACL.name(), currentUser())) {

            final List<NavRegistry.HeadDto> allHeads = ListUtils.emptyIfNull(navMgr.getActiveLeftSideMenus());

            if(StringUtils.isNotBlank(templateName)) {

                final List<NavRegistry.HeadDto> tmplHeads = ListUtils.emptyIfNull(navMgr.getLeftSideMenusByTemplate(templateName));
                final List<String> templPermCodes = extractPermCodes(tmplHeads);

                navMgr.updateUserPermissions(username, templPermCodes);
            }

            notifPrv.get().addSuccess("Successfully copy permission from template "+templateName).flash(ctx());
           return redirect(routes.NavCtrl.showUserNav(username));

        } else {

            notifPrv.get().addError("You do not have permission to view ACL template").flash(ctx());
            return redirect("/");

        }
    }

    public Result deleteTmplNav(String templateName) {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_UPDATE_TMPL.name(), currentUser())) {

            final List<NavRegistry.HeadDto> allHeads = ListUtils.emptyIfNull(navMgr.getActiveLeftSideMenus());

            navMgr.deleteTemplate(templateName);

            notifPrv.get().addSuccess("Sucessfully deleted template "+templateName).flash(ctx());

            TmplDto tmpl = tmplMgr.getTmpl("Access Level", "js/nav/nav.js", "css/nav/nav.css");
            return ok(NavTmplView.render(allHeads, navMgr.getTemplates(), null, tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view ACL template").flash(ctx());
            return redirect("/");

        }
    }

    public Result saveTmplNav() {

        if(navMgr.hasPermission(NavMgr.Permission.NAV_ACL_UPDATE_TMPL.name(), currentUser())) {

            final DynamicForm form = formFact.form().bindFromRequest();

            List<NavRegistry.HeadDto> heads = ListUtils.emptyIfNull(navMgr.getLeftSideMenusByUser(currentUser()));

            List<String> allPermCodes = extractPermCodes(heads);

            List<String> selectedPermCodes = ListUtils.emptyIfNull(allPermCodes.stream()
                    .filter(p -> StringUtils.isNotBlank(form.get(p)))
                    .collect(Collectors.toList()));

            String templateName = form.get("templateName");
            navMgr.updateTemplatePermissions(templateName, selectedPermCodes);

            notifPrv.get().addSuccess("Access level updated").flash(ctx());
            return redirect(routes.NavCtrl.showTmplNav(templateName));

        } else {

            notifPrv.get().addError("You do not have permission to update ACL template").flash(ctx());
            return redirect("/");

        }

    }

    private List<String> extractPermCodes(List<NavRegistry.HeadDto> heads) {

        List<NavRegistry.MenuDto> menus = heads.stream()
                .map(h -> h.getMenus())
                .flatMap(m -> m.stream())
                .collect(Collectors.toList());

        List<NavRegistry.PermDto> perms = menus.stream()
                .map(m -> m.getPerms())
                .flatMap(m -> m.stream())
                .collect(Collectors.toList());

        return ListUtils.emptyIfNull(perms.stream()
                .map(p -> p.getCode()).collect(Collectors.toList()));
    }
}
