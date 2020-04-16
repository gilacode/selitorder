package com.avicenna.config;

import com.avicenna.nav.NavRegistry;
import com.avicenna.security.SecUserMdl;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AppCfgNav extends NavRegistry {

    public enum Header {

        SYS_CFG("System", "fa fa-gears")
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

        SYS_CFG("System Config", "/cfg/configs", "fa fa-gears", Header.SYS_CFG, 90)
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

        SYS_CFG_VIEW_PAGE("View Configs", SecUserMdl.SecUserType.USER, Menu.SYS_CFG),
        SYS_CFG_UPDATE_CONFIG("Update Configs", SecUserMdl.SecUserType.USER, Menu.SYS_CFG),
        SYS_CFG_CLEAR_CACHE("Clear Cache", SecUserMdl.SecUserType.USER, Menu.SYS_CFG)
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

    @Override
    public List<HeadDto> leftSideBarMenus() {

        List<HeadDto> heads = new ArrayList<>();

        List<PermDto> perms = new ArrayList<>();
        for(Permission perm : Permission.values()) {
            perms.add(new PermDto(perm.name(), perm.getTitle(), perm.getUserType()));
        }

        List<MenuDto> menus = new ArrayList<>();
        for(Menu menu : Menu.values()) {
            menus.add(new MenuDto(menu.name(), menu.getTitle(), menu.getFullPath(), menu.getIconClass(), menu.getSequence(), perms));
        }

        for(Header head : Header.values()) {
            heads.add(new HeadDto(head.getTitle(), head.getIconClass(), menus));
        }

        return heads;
    }
}
