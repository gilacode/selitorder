package com.avicenna.apiclient;

import com.avicenna.encryption.EncException;
import com.avicenna.encryption.EncMgr;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.SecUserMdl;
import com.avicenna.util.LangUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Inject;
import com.google.inject.internal.cglib.core.$ObjectSwitchCallback;
import com.sun.corba.se.pept.protocol.ClientDelegate;
import org.apache.commons.collections4.ListUtils;
import play.cache.SyncCacheApi;

import java.util.List;
import java.util.stream.Collectors;

public class ApiClientMgr {

    private final SecMgr secMgr;
    private final EncMgr encMgr;

    private LangUtil langUtil;

    private final ApiClientMdl.ApiClientProvider apiClientProv;

    private final SyncCacheApi cache;

    @Inject
    public ApiClientMgr(SecMgr secMgr, EncMgr encMgr, LangUtil langUtil,
                        ApiClientMdl.ApiClientProvider apiClientProv, SyncCacheApi cache) {

        this.secMgr = secMgr;
        this.encMgr = encMgr;

        this.langUtil = langUtil;

        this.apiClientProv = apiClientProv;

        this.cache = cache;
    }

    public List<ApiClientDto> get(ApiClientMdl.ClientStatus... statuses) {
        return ListUtils.emptyIfNull(apiClientProv.get().query().orderBy("clientId asc")
                .where()
                .in("status", statuses)
                .findList())
                .stream().map(c -> new ApiClientDto(c, secMgr))
                .collect(Collectors.toList());
    }

    public ApiClientDto find(String clientId) {

        ApiClientDto dto = cache.get("CLIENT_ID_"+clientId);

        if(dto==null) {

            ApiClientMdl db = apiClientProv.get().query()
                    .where().eq("clientId", clientId).findOne();

            if(db==null) {
                return null;
            }

            dto = new ApiClientDto(db, secMgr);

            cache.set("CLIENT_ID_"+clientId, dto);
        }

        return dto;
    }

    public void create(ApiClientDto dto) throws ApiClientException {

        ApiClientMdl dbClientApi = apiClientProv.get().query()
                .where().eq("clientId", dto.getClientId()).findOne();

        if(dbClientApi!=null) {
            throw new ApiClientException(langUtil.at("apiclient.error.create.alreadyexist"));
        }

        SecUserDto user = secMgr.findUser(dto.getClientId());

        if(user!=null) {
            throw new ApiClientException(langUtil.at("apiclient.error.create.alreadyexist"));
        }

        dbClientApi = new ApiClientMdl();
        dbClientApi.setClientId(dto.getClientId());
        dbClientApi.setClientSecret(dto.getClientSecret());
        dbClientApi.setAdminEmail(dto.getAdminEmail());
        dbClientApi.setStatus(ApiClientMdl.ClientStatus.ACTIVE);
        dbClientApi.setCreatedBy(dto.getCreatedBy().getUsername());

        dbClientApi.insert();

        dto = new ApiClientDto(dbClientApi, secMgr);
        cache.set("CLIENT_ID_"+dto.getClientId(), dto);

        // create user

        try {

            user = new SecUserDto(dto.getClientId(), dto.getClientId(), encMgr.encrypt(dto.getClientSecret()),
                    null, dto.getAdminEmail(), SecUserMdl.SecUserType.API_CLIENT, SecUserMdl.SecUserStatus.ACTIVE);

            secMgr.createUser(user);

        } catch (EncException e) {

            throw new ApiClientException(langUtil.at("apiclient.error.create.user"), e);
        } catch (SecException e) {

            throw new ApiClientException(langUtil.at("apiclient.error.create.user"), e);
        }
    }

    public void lock(String clientId) {

        ApiClientMdl dbClientApi = apiClientProv.get().query()
                .where().eq("clientId", clientId).findOne();

        if(dbClientApi!=null) {
            dbClientApi.setStatus(ApiClientMdl.ClientStatus.SUSPEND);
            dbClientApi.update();
        }

        secMgr.lockUser(clientId);

        cache.remove("CLIENT_ID_"+clientId);
    }

    public void unlock(String clientId) {

        ApiClientMdl dbClientApi = apiClientProv.get().query()
                .where().eq("clientId", clientId).findOne();

        if(dbClientApi!=null) {
            dbClientApi.setStatus(ApiClientMdl.ClientStatus.ACTIVE);
            dbClientApi.update();
        }

        secMgr.unlockUser(clientId);

        cache.remove("CLIENT_ID_"+clientId);
    }

    public void deactivate(String clientId) {

        ApiClientMdl dbClientApi = apiClientProv.get().query()
                .where().eq("clientId", clientId).findOne();

        if(dbClientApi!=null) {
            dbClientApi.deletePermanent();
        }

        secMgr.deactivateUser(clientId);

        cache.remove("CLIENT_ID_"+clientId);
    }

    public static class ApiClientDto {

        private final String adminEmail;
        private final String clientId;
        private final String clientSecret;
        private final ApiClientMdl.ClientStatus clientStatus;
        private final SecUserDto createdBy;

        @JsonCreator
        public ApiClientDto(String clientId, String clientSecret, String adminEmail, ApiClientMdl.ClientStatus clientStatus, SecUserDto createdBy) {
           this.adminEmail = adminEmail;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.clientStatus = clientStatus;
            this.createdBy = createdBy;
        }

        public ApiClientDto(ApiClientMdl db, SecMgr secMgr) {
            this.adminEmail = db.getAdminEmail();
            this.clientId = db.getClientId();
            this.clientSecret = db.getClientSecret();
            this.clientStatus = db.getStatus();
            this.createdBy = secMgr.findUser(db.getCreatedBy());
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public ApiClientMdl.ClientStatus getClientStatus() {
            return clientStatus;
        }

        public SecUserDto getCreatedBy() {
            return createdBy;
        }
    }

}
