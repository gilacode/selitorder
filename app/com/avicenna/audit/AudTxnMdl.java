package com.avicenna.audit;

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
@Table(name = "audit_txn")
public class AudTxnMdl extends Model {

    public static class AudTxnProv implements Provider<Finder<Long, AudTxnMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject AudTxnProv(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, AudTxnMdl> get() {
            return new Finder<>(AudTxnMdl.class);
        }
    }

    @Id
    private Long id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Index(unique = false)
    @NotNull
    private String createdBy;

    @Index(unique = false)
    @NotNull
    private String auditMod; // eg: Security

    @Index(unique = false)
    @NotNull
    private String auditAction; // eg: Change Password

    @Index(unique = false)
    @NotNull
    private String auditRefKey; // eg: 008999678 (No RM)

    @Lob
    private String auditLog;

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAuditMod() {
        return auditMod;
    }

    public void setAuditMod(String auditMod) {
        this.auditMod = auditMod;
    }

    public String getAuditRefKey() {
        return auditRefKey;
    }

    public void setAuditRefKey(String auditRefKey) {
        this.auditRefKey = auditRefKey;
    }

    public String getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    public String getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(String auditLog) {
        this.auditLog = auditLog;
    }
}
