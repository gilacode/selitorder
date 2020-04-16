package com.avicenna.security;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.Index;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "sec_user")
public class SecUserMdl extends Model {

    public static class SecUserProvider implements Provider<Finder<Long, SecUserMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject SecUserProvider(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, SecUserMdl> get() {
            return new Finder<>(SecUserMdl.class);
        }
    }

    @Id
    private String username;

    @Index
    private String userCategory;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @NotNull
    private String displayName;
    @NotNull
    @Index(unique = true)
    private String email;
    @NotNull
    private String password;

    private boolean requireChangePwd;

    private String photoUrl;
    private String mobileNo;

    @Index(unique = true)
    private String rememberMeKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SecUserType userType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Enumerated(EnumType.STRING)
    private SecUserStatus userStatus = SecUserStatus.NEW;

    @Version
    private Long version;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRememberMeKey() {
        return rememberMeKey;
    }

    public void setRememberMeKey(String rememberMeKey) {
        this.rememberMeKey = rememberMeKey;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public SecUserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(SecUserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUserCategory() {
        return userCategory;
    }

    public void setUserCategory(String userCategory) {
        this.userCategory = userCategory;
    }

    public SecUserType getUserType() {
        return userType;
    }

    public void setUserType(SecUserType userType) {
        this.userType = userType;
    }

    public boolean isRequireChangePwd() {
        return requireChangePwd;
    }

    public void setRequireChangePwd(boolean requireChangePwd) {
        this.requireChangePwd = requireChangePwd;
    }

    public enum SecUserStatus {

        NEW("security.status.new"),
        ACTIVE("security.status.active"),
        LOCKED("security.status.locked"),
        DEACTIVATE("security.status.deactivate"),

        ;

        private String messageKey;

        SecUserStatus(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    public enum SecUserType {

        SUPER_ADMIN("security.user.type.superadmin", false),
        USER("security.user.type.user", false),
        API_CLIENT("security.user.type.apiclient", true),
        API_USER("security.user.type.apiuser", false)

        ;

        private String messageKey;
        private boolean internal;

        SecUserType(String messageKey, boolean internal) {
            this.messageKey = messageKey;
            this.internal = internal;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public boolean isInternal() {
            return internal;
        }
    }
}
