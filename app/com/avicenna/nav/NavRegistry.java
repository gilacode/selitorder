package com.avicenna.nav;

import com.avicenna.security.SecUserMdl;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public abstract class NavRegistry {

    public abstract List<HeadDto> leftSideBarMenus();

    public static class HeadDto {

        private final String title;
        private final String iconClass;

        private final List<MenuDto> menus;

        public HeadDto(NavHeadMdl db, List<MenuDto> menus) {
            this.title = db.getTitle();
            this.iconClass = db.getIconClass();

            this.menus = menus;
        }

        @JsonCreator
        public HeadDto(String title, String iconClass, List<MenuDto> menus) {
            this.title = title;
            this.iconClass = iconClass;

            this.menus = menus;
        }

        public String getCleanTitle() {
            return title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        public String getTitle() {
            return title;
        }

        public String getIconClass() {
            return iconClass;
        }

        public List<MenuDto> getMenus() {
            return menus;
        }
    }

    public static class MenuDto {

        private final String code;
        private final String title;
        private final String fullPath;
        private final String iconClass;
        private final int sequence;

        private final List<PermDto> perms;

        public MenuDto(NavMenuMdl db, List<PermDto> perms) {
            this.code = db.getCode();
            this.title = db.getTitle();
            this.fullPath = db.getFullPath();
            this.iconClass = db.getIconClass();
            this.sequence = db.getSequence();

            this.perms = perms;
        }

        @JsonCreator
        public MenuDto(String code, String title, String fullPath, String iconClass, int sequence, List<PermDto> perms) {
            this.code = code;
            this.title = title;
            this.fullPath = fullPath;
            this.iconClass = iconClass;
            this.sequence = sequence;

            this.perms = perms;
        }

        public List<PermDto> getPerms() {
            return perms;
        }

        public String getCode() {
            return code;
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

        public int getSequence() {
            return sequence;
        }
    }

    public static class PermDto {

        private final String code;
        private final String title;
        private final SecUserMdl.SecUserType userType;
        private final boolean autoRegister;

        private boolean checked;

        public PermDto(NavPermMdl db) {
            this.code = db.getCode();
            this.title = db.getTitle();
            this.userType = db.getUserType();
            this.autoRegister = db.isAutoRegister();
        }

        @JsonCreator
        public PermDto(String code, String title, SecUserMdl.SecUserType userType, boolean checked, boolean autoRegister) {
            this.code = code;
            this.title = title;
            this.userType = userType;
            this.checked = checked;
            this.autoRegister = autoRegister;
        }

        public PermDto(String code, String title, SecUserMdl.SecUserType userType) {
            this.code = code;
            this.title = title;
            this.userType = userType;
            this.checked = false;
            this.autoRegister = false;
        }

        public PermDto(String code, String title, SecUserMdl.SecUserType userType, boolean autoRegister) {
            this.code = code;
            this.title = title;
            this.userType = userType;
            this.checked = false;
            this.autoRegister = autoRegister;
        }

        public String getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }

        public SecUserMdl.SecUserType getUserType() {
            return userType;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean isAutoRegister() {
            return autoRegister;
        }
    }
}
