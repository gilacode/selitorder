package com.avicenna.config.dbcfg;

import com.avicenna.config.AppCfgMgr;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.Index;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "configuration")
public class DbCfgMdl extends Model {

    public static class DbCfgProvider implements Provider<Finder<Long, DbCfgMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject DbCfgProvider(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, DbCfgMdl> get() {
            return new Finder<>(DbCfgMdl.class);
        }
    }

    @Id
    private Long id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Index(unique = true)
    private String propertyKey;

    private String propertyDesc;

    private String stringValue;

    @DbJson
    private AppCfgMgr.AppCfgAddProp additionalProperties;

    @Index
    private String parentKey;

    @Transient
    private List<DbCfgMdl> childProps;

    @NotNull
    private boolean deletable = false;

    @Version
    private Long version;

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

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyDesc() {
        return propertyDesc;
    }

    public void setPropertyDesc(String propertyDesc) {
        this.propertyDesc = propertyDesc;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public AppCfgMgr.AppCfgAddProp getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(AppCfgMgr.AppCfgAddProp additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public List<DbCfgMdl> getChildProps() {
        return childProps;
    }

    public void setChildProps(List<DbCfgMdl> childProps) {
        this.childProps = childProps;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }
}
