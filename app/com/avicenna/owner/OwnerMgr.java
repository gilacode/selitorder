package com.avicenna.owner;

import com.avicenna.config.AppCfgMgr;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class OwnerMgr {

    private final AppCfgMgr appCfgMgr;

    @Inject
    OwnerMgr(AppCfgMgr appCfgMgr) {

        this.appCfgMgr = appCfgMgr;

        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.name", "", "Company Name", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.regno", "", "Company Registration No","Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.email", "", "Company Email Address", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.phoneno", "", "Company Phone No", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.faxno", "", "Company Fax No", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.website", "", "Company Website", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.address", "", "Company Address", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.tagline", "", "Company Tagline","Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.logourl", "", "Company Logo URL","Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.facebook", "", "Company Facebook Address", "Owner"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("owner.toc", "", "Terms and Conditions (HTML)", "Owner"));
    }

    public OwnerDto getOwner() {

        return new OwnerDto(
                appCfgMgr.getString("owner.name"),
                appCfgMgr.getString("owner.regno"),
                appCfgMgr.getString("owner.email"),
                appCfgMgr.getString("owner.phoneno"),
                appCfgMgr.getString("owner.faxno"),
                appCfgMgr.getString("owner.website"),
                appCfgMgr.getString("owner.address"),
                appCfgMgr.getString("owner.tagline"),
                appCfgMgr.getString("owner.logourl"),
                appCfgMgr.getString("owner.facebook"),
                appCfgMgr.getString("owner.toc"));

    }

    public String replace(String sourceContent) {

        if(StringUtils.isBlank(sourceContent)) {
            return sourceContent;
        }

        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.name\\}\\}", getOwner().getOwnerName());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.regno\\}\\}", getOwner().getOwnerRegno());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.email\\}\\}", getOwner().getOwnerEmail());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.phoneno\\}\\}", getOwner().getOwnerPhoneno());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.faxno\\}\\}", getOwner().getOwnerFaxno());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.website\\}\\}", getOwner().getOwnerWebsite());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.address\\}\\}", getOwner().getOwnerAddress());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.tagline\\}\\}", getOwner().getOwnerTagline());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.logourl\\}\\}", getOwner().getOwnerLogourl());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.facebook\\}\\}", getOwner().getOwnerFacebook());
        sourceContent = StringUtils.replaceAll(sourceContent, "\\{\\{owner.toc\\}\\}", getOwner().getOwnerToc());

        return sourceContent;
    }

    public static class OwnerDto {

        private final String ownerName;
        private final String ownerRegno;
        private final String ownerEmail;
        private final String ownerPhoneno;
        private final String ownerFaxno;
        private final String ownerWebsite;
        private final String ownerAddress;
        private final String ownerTagline;
        private final String ownerLogourl;
        private final String ownerFacebook;
        private final String ownerToc;

        @JsonCreator
        public OwnerDto(String ownerName, String ownerRegno, String ownerEmail, String ownerPhoneno,
                        String ownerFaxno, String ownerWebsite, String ownerAddress,
                        String ownerTagline, String ownerLogourl,
                        String ownerFacebook, String ownerToc) {
            this.ownerName = ownerName;
            this.ownerRegno = ownerRegno;
            this.ownerEmail = ownerEmail;
            this.ownerPhoneno = ownerPhoneno;
            this.ownerFaxno = ownerFaxno;
            this.ownerWebsite = ownerWebsite;
            this.ownerAddress = ownerAddress;
            this.ownerTagline = ownerTagline;
            this.ownerLogourl = ownerLogourl;
            this.ownerFacebook = ownerFacebook;
            this.ownerToc = ownerToc;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getOwnerRegno() {
            return ownerRegno;
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }

        public String getOwnerPhoneno() {
            return ownerPhoneno;
        }

        public String getOwnerFaxno() {
            return ownerFaxno;
        }

        public String getOwnerWebsite() {
            return ownerWebsite;
        }

        public String getOwnerAddress() {
            return ownerAddress;
        }

        public String getOwnerTagline() {
            return ownerTagline;
        }

        public String getOwnerLogourl() {
            return ownerLogourl;
        }

        public String getOwnerFacebook() {
            return ownerFacebook;
        }

        public String getOwnerToc() {
            return ownerToc;
        }
    }

}

