package com.avicenna.nav;

import com.avicenna.security.SecUserMdl;
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
@Table(name = "nav_perm")
public class NavPermMdl extends Model {

    public static class NavPermProv implements Provider<Finder<Long, NavPermMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject NavPermProv(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, NavPermMdl> get() {
            return new Finder<>(NavPermMdl.class);
        }
    }

    @Id
    private Long id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Index(unique = true)
    @NotNull
    private String code;

    @NotNull
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SecUserMdl.SecUserType userType;

    @ManyToOne(optional = false)
    private NavMenuMdl menu;

    private boolean autoRegister;

    public NavMenuMdl getMenu() {
        return menu;
    }

    public void setMenu(NavMenuMdl menu) {
        this.menu = menu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SecUserMdl.SecUserType getUserType() {
        return userType;
    }

    public void setUserType(SecUserMdl.SecUserType userType) {
        this.userType = userType;
    }

    public boolean isAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }
}
