package com.avicenna.template;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.nav.NavMgr;
import com.google.inject.Inject;
import com.typesafe.config.Config;

public class TmplMgr {

    private final Config config;
    private final AppCfgMgr appCfgMgr;
    private final NavMgr navMgr;

    @Inject TmplMgr(Config config, AppCfgMgr appCfgMgr, NavMgr navMgr) {

        this.config = config;

        this.appCfgMgr = appCfgMgr;
        this.navMgr = navMgr;

        for(TmplMgr.SystemConf cfg : TmplMgr.SystemConf.values()) {
            appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto(cfg.getKey(), cfg.getDefaultValue(), cfg.getDesc(), "System"));
        }

        for(BrandingDefaultConf cfg : BrandingDefaultConf.values()) {
            appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto(cfg.getKey(), cfg.getDefaultValue(), cfg.getDesc(),"Branding"));
        }
    }

    public TmplDto getTmpl(String title) {
        return new TmplDto(
                navMgr,
                title,
                appCfgMgr.getString(BrandingDefaultConf.LOGO_URL_DARK.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.LOGO_URL_WHITE.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.SPINNER_URL.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.APP_NAME.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.HEADER_COLOR.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.PRIMARY_COLOR.getKey()),
                config.getString("build.version"),
                config.getString("server.instance"),
                null, null);
    }

    public TmplDto getTmpl(String title, String jsPath, String cssPath) {
        return new TmplDto(
                navMgr,
                title,
                appCfgMgr.getString(BrandingDefaultConf.LOGO_URL_DARK.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.LOGO_URL_WHITE.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.SPINNER_URL.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.APP_NAME.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.HEADER_COLOR.getKey()),
                appCfgMgr.getString(BrandingDefaultConf.PRIMARY_COLOR.getKey()),
                config.getString("build.version"),
                config.getString("server.instance"),
                cssPath, jsPath);
    }

    public enum SystemConf {

        HOST("system.host", "http://localhost:9004", "Hostname"),
        ALLOW_SIGNUP("system.signup.allow", "true", "Allow User to Signup?"),
        ;

        private final String key;
        private final String defaultValue;
        private final String desc;

        SystemConf(String key, String defaultValue, String desc) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum BrandingDefaultConf {

        LOGO_URL_DARK("branding.logoUrlDark", "http://localhost:9004/assets/images/logo_48x48_color.png", "Logo URL (Dark)"),
        LOGO_URL_WHITE("branding.logoUrlWhite", "http://localhost:9004/assets/images/logo_48x48_light.png", "Logo URL (Light)"),
        SPINNER_URL("branding.spinnerUrl", "http://localhost:9004/assets/images/spinner64.gif", "Spinner URL"),
        APP_NAME("branding.appName", "APP", "Application name"),
        HEADER_COLOR("branding.headerColor", "#03a9f3", "Header HTML Color"),
        PRIMARY_COLOR("branding.primaryColor", "#03a9f3", "Primary HTML Color"),
        ;

        private final String key;
        private final String defaultValue;
        private final String desc;

        BrandingDefaultConf(String key, String defaultValue, String desc) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDesc() {
            return desc;
        }
    }
}
