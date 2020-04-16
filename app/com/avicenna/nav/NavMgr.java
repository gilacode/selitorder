package com.avicenna.nav;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.SecUserMdl;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class NavMgr {

    private final AppCfgMgr appCfgMgr;

    private final NavPermMdl.NavPermProv navPermProv;
    private final NavMenuMdl.NavMenuProv navMenuProv;
    private final NavHeadMdl.NavHeadProv navHeadProv;
    private final NavMapMdl.NavMapProv navMapProv;
    private final NavTmplMdl.NavTmplProv navTmplProv;

    @Inject NavMgr(AppCfgMgr appCfgMgr, Set<NavRegistry> navRegs, NavPermMdl.NavPermProv navPermProv,
                   NavMenuMdl.NavMenuProv navMenuProv, NavHeadMdl.NavHeadProv navHeadProv, NavTmplMdl.NavTmplProv navTmplProv,
                   NavMapMdl.NavMapProv navMapProv) {

        this.appCfgMgr = appCfgMgr;

        this.navPermProv = navPermProv;
        this.navMenuProv = navMenuProv;
        this.navHeadProv = navHeadProv;
        this.navMapProv = navMapProv;
        this.navTmplProv = navTmplProv;

        // add own menus
        List<NavRegistry.HeadDto> navMenus = ListUtils.emptyIfNull(navMenus());
        saveMenu(navMenus, MenuLocation.LEFT_SIDEBAR);

        if(navRegs!=null && navRegs.size() > 0) {

            for (NavRegistry navReg : navRegs) {

                List<NavRegistry.HeadDto> leftSideBarHeads = ListUtils.emptyIfNull(navReg.leftSideBarMenus());
                saveMenu(leftSideBarHeads, MenuLocation.LEFT_SIDEBAR);

            }
        }


    }

    public void updateUserPermissions(String username, List<String> permCodes) {

        // delete all mapping
        List<NavMapMdl> existingMaps = ListUtils.emptyIfNull(navMapProv.get().query()
                .where().eq("referenceKey", username).findList());
        for (NavMapMdl emap : existingMaps) {

            emap.deletePermanent();

        }

        List<NavPermMdl> perms = ListUtils.emptyIfNull(
                navPermProv.get().query().where().in("code", permCodes).findList());
        for(NavPermMdl perm : perms) {

            NavMapMdl map = new NavMapMdl();
            map.setPermission(perm);
            map.setReferenceKey(username);
            map.insert();

        }
    }

    public void updateTemplatePermissions(String templateName, List<String> permCodes) {

        // delete all mapping
        List<NavTmplMdl> existingTmplNavs = ListUtils.emptyIfNull(navTmplProv.get().query()
                .where().eq("templateName", templateName).findList());
        for (NavTmplMdl navTmpl : existingTmplNavs) {

            navTmpl.deletePermanent();

        }

        List<NavPermMdl> perms = ListUtils.emptyIfNull(
                navPermProv.get().query().where().in("code", permCodes).findList());
        for(NavPermMdl perm : perms) {

            NavTmplMdl tmplNav = new NavTmplMdl();
            tmplNav.setPermission(perm);
            tmplNav.setTemplateName(templateName);
            tmplNav.insert();

        }
    }

    public boolean hasPermission(String permCode, SecUserDto user) {

        if(user!=null) {

            if(user.getUserType().equals(SecUserMdl.SecUserType.SUPER_ADMIN)) {

                return true;

            } else {

                NavMapMdl map = navMapProv.get().query()
                        .fetch("permission")
                        .where()
                        .eq("permission.code", permCode)
                        .eq("referenceKey", user.getUsername()).findOne();

                if(map!=null) {

                    return true;

                }

            }

        }

        return false;
    }

    public List<NavRegistry.HeadDto> getProfileMenus(SecUserDto user) {
        return getMenusByUserAndLocation(user, MenuLocation.PROFILE);
    }

    public List<NavRegistry.HeadDto> getLeftSideMenusByUser(SecUserDto user) {
        return getMenusByUserAndLocation(user, MenuLocation.LEFT_SIDEBAR);
    }

    public List<NavRegistry.HeadDto> getLeftSideMenusByTemplate(String templateName) {
        return getMenusByTemplateAndLocation(templateName, MenuLocation.LEFT_SIDEBAR);
    }

    public List<NavRegistry.HeadDto> getActiveLeftSideMenus() {
        return getMenusByUserAndLocation(null, MenuLocation.LEFT_SIDEBAR);
    }

    public void autoRegisterPermission(String username, SecUserMdl.SecUserType userType) {

        List<NavPermMdl> perms = ListUtils.emptyIfNull(navPermProv.get()
                .query()
                .orderBy("sequence asc")
                .fetch("menu")
                .where()
                .eq("userType", userType.name())
                .eq("autoRegister", true)
                .eq("menu.menuStatus", MenuStatus.ACTIVE).findList());

        List<String> permCodes = perms.stream().map(p -> p.getCode()).collect(Collectors.toList());

        if(permCodes.size() > 0) {
            updateUserPermissions(username, permCodes);
        }
    }

    private List<NavRegistry.HeadDto> getMenusByUserAndLocation(SecUserDto user, MenuLocation menuLocation) {

        List<NavPermMdl> perms;

        if(user==null) {

            perms = ListUtils.emptyIfNull(navPermProv.get()
                    .query()
                    .orderBy("sequence asc")
                    .fetch("menu")
                    .where()
                    .eq("menu.menuLocation", menuLocation)
                    .eq("menu.menuStatus", MenuStatus.ACTIVE).findList());

        } else {

            if(user.getUserType().equals(SecUserMdl.SecUserType.SUPER_ADMIN)) {

                perms = ListUtils.emptyIfNull(navPermProv.get()
                        .query()
                        .orderBy("sequence asc")
                        .fetch("menu")
                        .where()
                        .eq("menu.menuLocation", menuLocation)
                        .eq("menu.menuStatus", MenuStatus.ACTIVE)
                        .findList());


            } else {

                List<NavMapMdl> maps = ListUtils.emptyIfNull(navMapProv.get()
                        .query()
                        .orderBy("permission.menu.sequence asc")
                        .fetch("permission")
                        .fetch("permission.menu")
                        .where().eq("referenceKey", user.getUsername())
                        .eq("permission.menu.menuLocation", menuLocation)
                        .eq("permission.menu.menuStatus", MenuStatus.ACTIVE).findList());

                perms = maps.stream().map(m -> m.getPermission()).collect(Collectors.toList());

            }
        }

        return mapPermissions(perms);
    }

    private List<NavRegistry.HeadDto> getMenusByTemplateAndLocation(String templateName, MenuLocation menuLocation) {

        List<NavTmplMdl> maps = ListUtils.emptyIfNull(navTmplProv.get()
                .query()
                .orderBy("permission.menu.sequence asc")
                .fetch("permission")
                .fetch("permission.menu")
                .where().eq("templateName", templateName)
                .eq("permission.menu.menuLocation", menuLocation)
                .eq("permission.menu.menuStatus", MenuStatus.ACTIVE).findList());

        List<NavPermMdl> perms = maps.stream().map(m -> m.getPermission()).collect(Collectors.toList());

        return mapPermissions(perms);
    }

    private List<NavRegistry.HeadDto> mapPermissions(List<NavPermMdl> perms) {

        List<NavRegistry.HeadDto> heads = new ArrayList<>();

        for(NavPermMdl permDb : perms) {

            NavRegistry.HeadDto head = heads.stream()
                    .filter(h -> h.getTitle().equals(permDb.getMenu().getHeader().getTitle()))
                    .findFirst().orElse(null);
            if(head==null) {
                head = new NavRegistry.HeadDto(permDb.getMenu().getHeader(), new ArrayList<>());
                heads.add(head);
            }

            NavRegistry.MenuDto menu = head.getMenus().stream()
                    .filter(m -> m.getCode().equals(permDb.getMenu().getCode()))
                    .findFirst().orElse(null);
            if(menu==null) {
                menu = new NavRegistry.MenuDto(permDb.getMenu(), new ArrayList<>());
                head.getMenus().add(menu);
            }

            menu.getPerms().add(new NavRegistry.PermDto(permDb));
        }

        return heads;
    }

    private void saveMenu(List<NavRegistry.HeadDto> heads, MenuLocation menuLocation) {

        for(NavRegistry.HeadDto head : heads) {

            NavHeadMdl headDb = navHeadProv.get().query().where().eq("title", head.getTitle()).findOne();
            if(headDb==null) {

                headDb = new NavHeadMdl();
                headDb.setTitle(head.getTitle());
                headDb.setIconClass(head.getIconClass());

                headDb.insert();

            }

            for(NavRegistry.MenuDto menu : ListUtils.emptyIfNull(head.getMenus())) {

                NavMenuMdl menuDb = navMenuProv.get().query().where().eq("code", menu.getCode()).findOne();
                if(menuDb==null) {

                    menuDb = new NavMenuMdl();
                    menuDb.setCode(menu.getCode());
                    menuDb.setTitle(menu.getTitle());
                    menuDb.setFullPath(menu.getFullPath());
                    menuDb.setIconClass(menu.getIconClass());
                    menuDb.setMenuStatus(MenuStatus.ACTIVE);
                    menuDb.setMenuLocation(menuLocation);

                    menuDb.setHeader(headDb);

                    menuDb.insert();

                }

                for(NavRegistry.PermDto perm : ListUtils.emptyIfNull(menu.getPerms())) {

                    NavPermMdl permDb = navPermProv.get().query().where().eq("code", perm.getCode()).findOne();
                    if (permDb == null) {

                        permDb = new NavPermMdl();
                        permDb.setCode(perm.getCode());
                        permDb.setTitle(perm.getTitle());
                        permDb.setUserType(perm.getUserType());

                        permDb.setMenu(menuDb);

                        permDb.insert();

                    }
                }


            }
        }

    }

    public List<String> getTemplates() {
        return ListUtils.emptyIfNull(navTmplProv.get().all())
                .stream().map(t -> t.getTemplateName()).distinct().sorted()
                .collect(Collectors.toList());
    }

    public void deleteTemplate(String templateName) {
        List<NavTmplMdl> dbs = ListUtils.emptyIfNull(navTmplProv.get().query().where().eq("templateName", templateName).findList());
        for(NavTmplMdl db : dbs) {
            db.deletePermanent();
        }
    }

    public enum MenuLocation {
        PROFILE, LEFT_SIDEBAR
    }

    public enum MenuStatus {
        ACTIVE, DISABLED
    }

    private List<NavRegistry.HeadDto> navMenus() {

        List<NavRegistry.HeadDto> heads = new ArrayList<>();

        List<NavRegistry.PermDto> perms = new ArrayList<>();
        for(Permission perm : Permission.values()) {
            perms.add(new NavRegistry.PermDto(perm.name(), perm.getTitle(), perm.getUserType()));
        }

        List<NavRegistry.MenuDto> menus = new ArrayList<>();
        for(Menu menu : Menu.values()) {
            menus.add(new NavRegistry.MenuDto(menu.name(), menu.getTitle(), menu.getFullPath(), menu.getIconClass(), menu.getSequence(), perms));
        }

        for(Header head : Header.values()) {
            heads.add(new NavRegistry.HeadDto(head.getTitle(), head.getIconClass(), menus));
        }

        return heads;
    }


    public enum Header {

        NAV_ACL("Navigation", "fa fa-bars")
        ;

        private final String title;
        private final String iconClass;

        Header(String title, String iconClass) {
            this.title = title;
            this.iconClass = iconClass;
        }

        public String getTitle() {
            return title;
        }

        public String getIconClass() {
            return iconClass;
        }
    }

    public enum Menu {

        NAV_ACL("Access Level", "/nav", "fa fa-bars", Header.NAV_ACL, 70),
        NAV_TMPL("ACL Template", "/nav/templates", "fa fa-bars", Header.NAV_ACL, 71)
        ;

        private final String title;
        private final String fullPath;
        private final String iconClass;
        private final int sequence;

        private final Header header;

        Menu(String title, String fullPath, String iconClass, Header header, int sequence) {
            this.title = title;
            this.fullPath = fullPath;
            this.iconClass = iconClass;
            this.header = header;
            this.sequence = sequence;
        }

        public String getTitle() {
            return title;
        }

        public String getFullPath() {
            return fullPath;
        }

        public String getIconClass() {
            return iconClass;
        }

        public Header getHeader() {
            return header;
        }

        public int getSequence() {
            return sequence;
        }
    }

    public enum Permission {

        NAV_ACL_VIEW_ACL("View Access Level", SecUserMdl.SecUserType.USER, Menu.NAV_ACL),
        NAV_ACL_UPDATE_ACL("Update Access Level", SecUserMdl.SecUserType.USER, Menu.NAV_ACL),
        NAV_ACL_VIEW_TMPL("View ACL Template", SecUserMdl.SecUserType.USER, Menu.NAV_TMPL),
        NAV_ACL_UPDATE_TMPL("Update ACL Template", SecUserMdl.SecUserType.USER, Menu.NAV_TMPL)

        ;

        private final String title;
        private final SecUserMdl.SecUserType userType;

        private final Menu menu;

        Permission(String title, SecUserMdl.SecUserType userType, Menu menu) {
            this.title = title;
            this.userType = userType;
            this.menu = menu;
        }

        public String getTitle() {
            return title;
        }

        public SecUserMdl.SecUserType getUserType() {
            return userType;
        }

        public Menu getMenu() {
            return menu;
        }
    }
}
