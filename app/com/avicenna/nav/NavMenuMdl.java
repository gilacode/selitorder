package com.avicenna.nav;

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
@Table(name = "nav_menu")
public class NavMenuMdl extends Model {

    public static class NavMenuProv implements Provider<Finder<Long, NavMenuMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject NavMenuProv(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, NavMenuMdl> get() {
            return new Finder<>(NavMenuMdl.class);
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
    private String fullPath;

    @NotNull
    private String iconClass;

    @Enumerated(EnumType.STRING)
    private NavMgr.MenuLocation menuLocation;

    @ManyToOne(optional = false)
    private NavHeadMdl header;

    @Enumerated(EnumType.STRING)
    private NavMgr.MenuStatus menuStatus;

    @NotNull
    private int sequence;

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

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public NavMgr.MenuLocation getMenuLocation() {
        return menuLocation;
    }

    public void setMenuLocation(NavMgr.MenuLocation menuLocation) {
        this.menuLocation = menuLocation;
    }

    public NavMgr.MenuStatus getMenuStatus() {
        return menuStatus;
    }

    public void setMenuStatus(NavMgr.MenuStatus menuStatus) {
        this.menuStatus = menuStatus;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public NavHeadMdl getHeader() {
        return header;
    }

    public void setHeader(NavHeadMdl header) {
        this.header = header;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
