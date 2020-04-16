package com.avicenna.audit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class AuditMgr {

    private final AudTxnMdl.AudTxnProv audTxnProv;

    @Inject
    AuditMgr(AudTxnMdl.AudTxnProv audTxnProv) {

        this.audTxnProv = audTxnProv;

    }

    public void audit(String currUsername, String module, String action, String refKey, String log) {

        AudTxnMdl db = new AudTxnMdl();

        db.setCreatedAt(new Date());
        db.setCreatedBy(currUsername);
        db.setAuditMod(module);
        db.setAuditAction(action);
        db.setAuditRefKey(refKey);
        db.setAuditLog(log);

        db.insert();
    }

    public List<AuditDto> get(String username, String module, String action, String refKey) {

        Query<AudTxnMdl> query = audTxnProv.get().query();
        query = query.orderBy("createdAt desc");

        ExpressionList<AudTxnMdl> where = query.where();

        if(StringUtils.isNotBlank(username)) {
            where = where.eq("createdBy", username);
        }

        if(StringUtils.isNotBlank(module)) {
            where = where.eq("auditMod", module);
        }

        if(StringUtils.isNotBlank(action)) {
            where = where.eq("auditAction", action);
        }

        if(StringUtils.isNotBlank(refKey)) {
            where = where.eq("auditRefKey", refKey);
        }

        return ListUtils.emptyIfNull(where.findList()).stream()
                .map(a -> new AuditDto(a)).collect(Collectors.toList());
    }

    public static class AuditDto {

        private final String createdBy;
        private final Date createdAt;
        private final String module;
        private final String action;
        private final String refKey;
        private final String log;

        @JsonCreator
        public AuditDto(String createdBy, Date createdAt, String module, String action, String refKey, String log) {
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.module = module;
            this.action = action;
            this.refKey = refKey;
            this.log = log;
        }

        public AuditDto(AudTxnMdl db) {
            this.createdBy = db.getCreatedBy();
            this.createdAt = db.getCreatedAt();
            this.module = db.getAuditMod();
            this.action = db.getAuditAction();
            this.refKey = db.getAuditRefKey();
            this.log = db.getAuditLog();
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public String getModule() {
            return module;
        }

        public String getRefKey() {
            return refKey;
        }

        public String getAction() {
            return action;
        }

        public String getLog() {
            return log;
        }
    }
}
