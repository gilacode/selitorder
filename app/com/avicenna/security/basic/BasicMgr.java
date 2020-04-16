package com.avicenna.security.basic;

import com.avicenna.audit.AuditMgr;
import com.avicenna.config.AppCfgMgr;
import com.avicenna.encryption.EncException;
import com.avicenna.encryption.EncMgr;
import com.avicenna.nav.NavMgr;
import com.avicenna.notification.NotifMgr;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.SecUserMdl;
import com.avicenna.uqcode.UQCodeMgr;
import com.avicenna.util.IdUtil;
import com.avicenna.util.LangUtil;
import com.google.inject.Inject;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.engine.UserDataType;
import play.Logger;
import play.cache.SyncCacheApi;
import play.mvc.Http;

import javax.transaction.UserTransaction;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BasicMgr extends SecMgr {

    private final LangUtil langUtil;
    private final IdUtil idUtil;

    private final SyncCacheApi cache;

    private final EncMgr encMgr;
    private final UQCodeMgr uqCodeMgr;
    private final NavMgr navMgr;

    private final SecUserMdl.SecUserProvider secUserProvider;

    @Inject BasicMgr(LangUtil langUtil, IdUtil idUtil, SyncCacheApi cache, EncMgr encMgr, UQCodeMgr uqCodeMgr, NavMgr navMgr,
                    SecUserMdl.SecUserProvider basicProvider) {

        this.langUtil = langUtil;
        this.idUtil = idUtil;

        this.cache = cache;

        this.encMgr = encMgr;
        this.uqCodeMgr = uqCodeMgr;
        this.navMgr = navMgr;

        this.secUserProvider = basicProvider;
    }

    @Override
    public SecUserDto getCurrentUser(Http.Context ctx) throws SecException {

        String username = ctx.session().get("secUser");
        if(username!=null) {
            SecUserDto secUser = this.cache.get("USER_"+username);
            if(secUser!=null) {
                return secUser;
            }
        }

        Http.Cookie rememberMe = ctx.request().cookie("rme");
        if(rememberMe!=null) {

            SecUserMdl db = secUserProvider.get().query().where().eq("rememberMeKey", rememberMe.value()).findOne();
            if(db!=null && db.getUserStatus().equals(SecUserMdl.SecUserStatus.ACTIVE)) {

                return new SecUserDto(db);
            }
        }

        return null;
    }

    @Override
    public void changePassword(String username, String currPassword, String newPassword) throws SecException {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        try {

            String currPwdEnc = encMgr.encrypt(currPassword);
            if(!currPwdEnc.equals(dbUser.getPassword())) {
                throw new SecException(langUtil.at("security.error.changepwd.pwdnotmatch"));
            }

            dbUser.setPassword(encMgr.encrypt(newPassword));
            dbUser.update();

        } catch (EncException e) {

            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException(langUtil.at("security.error.changepwd.encryption"), e);
        }
    }

    @Override
    public String resetPassword(String username) throws SecException {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        try {

            String newPassword = idUtil.getShortUniqueId();
            dbUser.setPassword(encMgr.encrypt(newPassword));
            dbUser.setRequireChangePwd(true);
            dbUser.update();

            return newPassword;

        } catch (EncException e) {

            Logger.error(this.getClass().getSimpleName(), e);
            throw new SecException(langUtil.at("security.error.changepwd.encryption"), e);
        }
    }

    @Override
    public void reloadCurrUser(String username) throws SecException {

        SecUserMdl dbUser = secUserProvider.get().query()
                .where().eq("username", username).findOne();

        SecUserDto userDto = new SecUserDto(dbUser);

        this.cache.set("USER_"+username, userDto);

    }

    @Override
    public void createUser(SecUserDto secUser) throws SecException  {

        try {

            if (StringUtils.isAnyBlank(secUser.getUsername(), secUser.getDisplayName(), secUser.getPassword(), secUser.getEmail())) {
                throw new SecException(langUtil.at("security.error.dosignup.blank"));
            }

            String cleanUsername = secUser.getUsername().replaceAll("[^a-zA-Z0-9]", "");
            if (!cleanUsername.equals(secUser.getUsername())) {
                throw new SecException(langUtil.at("security.error.dosignup.specialchar"));
            }

            SecUserMdl db = secUserProvider.get().query().where().eq("username", secUser.getUsername()).findOne();
            if (db != null) {
                throw new SecException(langUtil.at("security.error.dosignup.usernameexist"));
            }

            final String encPassword = encMgr.encrypt(secUser.getPassword());

            db = new SecUserMdl();
            db.setUsername(secUser.getUsername());
            db.setPassword(encPassword);
            db.setDisplayName(secUser.getDisplayName());
            db.setUserStatus(secUser.getStatus());
            db.setEmail(secUser.getEmail());
            db.setUserType(secUser.getUserType());
            db.insert();

            this.cache.set("USER_"+db.getUsername(), new SecUserDto(db));

            this.navMgr.autoRegisterPermission(secUser.getUsername(), secUser.getUserType());

        } catch (EncException e) {

            throw new SecException(langUtil.at("security.error.dosignup.encrypt"), e);
        }
    }

    @Override
    public List<SecUserDto> getUsers(UserParam param) {

        Query<SecUserMdl> query = secUserProvider.get().query();

        for(UserField userField : param.getUserFields()) {
            query = query.orderBy(userField.getFieldName()+" "+userField.getOrderBy().name());
        }

        ExpressionList<SecUserMdl> where = query.where();

        for(UserField userField : param.getUserFields()) {
            String[] fieldValues = userField.getFieldValues();
            if(fieldValues!=null && fieldValues.length==1) {
                where = where.eq(userField.getFieldName(), userField.getFieldValues()[0]);
            } else if(fieldValues!=null && fieldValues.length>1) {
                where = where.in(userField.getFieldName(), userField.getFieldValues());
            }
        }

        return  ListUtils.emptyIfNull(where.findList()).stream().map(u -> new SecUserDto(u)).collect(Collectors.toList());
    }

    @Override
    public SecUserDto findUser(String username) {

        SecUserDto dto = cache.get("USER_"+username);

        if(dto==null) {

            SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

            if(dbUser!=null) {

                dto = new SecUserDto(dbUser);
                this.cache.set("USER_"+username, dto);
            }
        }

        return dto;
    }

    @Override
    public void makeSuperAdmin(String username) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserType(SecUserMdl.SecUserType.SUPER_ADMIN);
            dbUser.update();

        }

        this.cache.set("USER_"+username, new SecUserDto(dbUser));
    }

    @Override
    public void downgradeSuperAdmin(String username) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserType(SecUserMdl.SecUserType.USER);
            dbUser.update();

        }

        this.cache.set("USER_"+username, new SecUserDto(dbUser));

    }

    @Override
    public SecUserDto updateProfile(String username, String email, String displayName, String mobileNo, String photoUrl) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            if(StringUtils.isNotBlank(email)) {
                dbUser.setEmail(email);
            }

            if(StringUtils.isNotBlank(displayName)) {
                dbUser.setDisplayName(displayName);
            }

            if(StringUtils.isNotBlank(photoUrl)) {
                dbUser.setPhotoUrl(photoUrl);
            }

            if(StringUtils.isNotBlank(mobileNo)) {
                dbUser.setMobileNo(mobileNo);
            }

            dbUser.update();

            this.cache.set("USER_"+username, new SecUserDto(dbUser));

            return new SecUserDto(dbUser);
        }

        return null;
    }

    @Override
    public void lockUser(String username) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserStatus(SecUserMdl.SecUserStatus.LOCKED);
            dbUser.update();

            this.cache.set("USER_"+username, new SecUserDto(dbUser));
        }
    }

    @Override
    public void unlockUser(String username) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserStatus(SecUserMdl.SecUserStatus.ACTIVE);
            dbUser.update();

            this.cache.set("USER_"+username, new SecUserDto(dbUser));
        }
    }

    @Override
    public void activateUser(String username) {
        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserStatus(SecUserMdl.SecUserStatus.ACTIVE);
            dbUser.update();

            this.cache.set("USER_"+username, new SecUserDto(dbUser));
        }
    }

    @Override
    public void deactivateUser(String username) {
        SecUserMdl dbUser = secUserProvider.get().query().where().eq("username", username).findOne();

        if(dbUser!=null) {

            dbUser.setUserStatus(SecUserMdl.SecUserStatus.ACTIVE);
            dbUser.update();

            this.cache.set("USER_"+username, new SecUserDto(dbUser));
        }
    }

    @Override
    public SecUserDto findUserByEmail(String email) {

        SecUserMdl dbUser = secUserProvider.get().query().where().eq("email", email).findOne();
        if(dbUser!=null) {
            return new SecUserDto(dbUser);
        }

        return null;
    }

    public Response doSignup(SecUserDto secUser) throws SecException {

        createUser(secUser);

        return new Response(true, langUtil.at("security.success.dosignup"));
    }

    public Response doVerifySignup(String signupCode, String pairKey) throws SecException {

        try {

            if (StringUtils.isAnyBlank(signupCode)) {
                return new Response(false, langUtil.at("security.error.verifysignup.codeblank"));
            }

            if(StringUtils.isBlank(pairKey)) {
                return new Response(false, langUtil.at("security.error.verifysignup.verifycode"));
            }

            String dbPairKey = uqCodeMgr.findReferenceKey(signupCode);
            if(StringUtils.isBlank(dbPairKey)) {
                return new Response(false, langUtil.at("security.error.verifysignup.missingpairkey"));
            }

            if(!dbPairKey.equals(pairKey)) {
                return new Response(false, langUtil.at("security.error.verifysignup.pairkeynotmatch"));
            }

            String username = encMgr.decrypt(pairKey);
            SecUserMdl db = secUserProvider.get().query().where().eq("username", username).findOne();

            if(db==null) {
                return new Response(false, langUtil.at("security.error.verifysignup.usernotfound"));
            }

            db.setUserStatus(SecUserMdl.SecUserStatus.ACTIVE);
            db.update();

            return new Response(true, langUtil.at("security.success.verifysignup"));

        } catch (EncException e) {
            throw new SecException(langUtil.at("security.error.verifysignup.encryption"), e);
        }
    }

    public Response doVerifyForgotPwd(String forgotPwdCode, String pairKey) throws SecException {

        try {

            if (StringUtils.isAnyBlank(forgotPwdCode)) {
                return new Response(false, langUtil.at("security.error.forgotpwd.pwdblank"));
            }

            if(StringUtils.isBlank(pairKey)) {
                return new Response(false, langUtil.at("security.error.forgotpwd.pkeynotprovided"));
            }

            String dbPairKey = uqCodeMgr.findReferenceKey(forgotPwdCode);
            if(StringUtils.isBlank(dbPairKey)) {
                return new Response(false, langUtil.at("security.error.forgotpwd.missingpairkey"));
            }

            if(!dbPairKey.equals(pairKey)) {
                return new Response(false, langUtil.at("security.error.forgotpwd.pairkeynotmatch"));
            }

            // compare to sign up verification forgot password the pair key can be either username or email

            String usernameOrEmail = encMgr.decrypt(pairKey);
            SecUserMdl db = secUserProvider.get().query().where()
                    .or(Expr.eq("username", usernameOrEmail),
                            Expr.eq("email", usernameOrEmail))
                    .findOne();

            if(db==null) {
                return new Response(false, langUtil.at("security.error.forgotpwd.usernotfound"));
            }

            return new Response(true, langUtil.at("security.success.forgotpwd"));

        } catch (EncException e) {
            throw new SecException(langUtil.at("security.error.forgotpwd.encryption"), e);
        }
    }


    @Override
    public String loginUrl() throws SecException {

        try {

            int cntUser = secUserProvider.get().query().findCount();
            if (cntUser == 0) {

                String encPassword = encMgr.encrypt("admin");

                SecUserMdl db = new SecUserMdl();
                db.setUsername("admin");
                db.setEmail("admin@company.com");
                db.setPassword(encPassword);
                db.setDisplayName("Administrator");
                db.setUserType(SecUserMdl.SecUserType.SUPER_ADMIN);
                db.setUserStatus(SecUserMdl.SecUserStatus.ACTIVE);
                db.insert();
            }
        } catch (EncException e) {
            Logger.debug(this.getClass().getSimpleName(), e);
            throw new SecException(langUtil.at("security.error.login.encryption"), e);
        }

        return "/sec/basic/login";
    }

    @Override
    public String logoutUrl() throws SecException {
        return "/sec/basic/logout";
    }

    public Response doLogin(String username, String password, boolean rememberMe, Http.Context ctx) throws SecException {

        try {

            if(StringUtils.isAnyBlank(username, password)) {
                return new Response(false, langUtil.at("security.error.dologin.blankunamepwd"));
            }

            String encPassword = encMgr.encrypt(password);

            SecUserMdl db = secUserProvider.get().query().where().eq("username", username).findOne();
            if (db != null && db.getPassword().equals(encPassword)) {

                SecUserDto user = new SecUserDto(db);
                this.cache.set("USER_"+user.getUsername(), user);
                ctx.session().put("secUser", user.getUsername());

                if(rememberMe) {
                    String uuid = UUID.randomUUID().toString();
                    ctx.response()
                            .setCookie(
                                    Http.Cookie.builder("rme", uuid)
                                            .withMaxAge(Duration.ofSeconds(432000))
                                            .withSecure(false)
                                            .withHttpOnly(true)
                                            .withSameSite(Http.Cookie.SameSite.STRICT)
                                            .build());
                    db.setRememberMeKey(uuid);
                }

                db.setLastLogin(new Date());
                db.update();

                return new Response(true, langUtil.at("security.success.login"));
            }
        } catch (EncException e) {
            Logger.debug(this.getClass().getSimpleName(), e);
            throw new SecException(langUtil.at("security.error.dologin.encryption"), e);
        }

        return new Response(false, langUtil.at("security.error.dologin.wrongpwd"));
    }

    public Response changePassword(String username, String password) {

        SecUserMdl db = secUserProvider.get().query().where().eq("username", username).findOne();
        if(db!=null) {

            db.setPassword(password);
            db.update();

            return new Response(true, langUtil.at("security.success.changepwd"));
        } else {

            return new Response(false, langUtil.at("security.error.changepwd.usernotfound"));
        }

    }
}
