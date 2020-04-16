package com.avicenna.security.basic;

import com.avicenna.owner.OwnerMgr;
import com.avicenna.config.AppCfgMgr;
import com.avicenna.email.EmailMgr;
import com.avicenna.encryption.EncException;
import com.avicenna.encryption.EncMgr;
import com.avicenna.monitor.Monitor;
import com.avicenna.security.*;
import com.avicenna.security.basic.html.BasicChangePassword;
import com.avicenna.security.basic.html.BasicForgotPassword;
import com.avicenna.security.basic.html.BasicLogin;
import com.avicenna.security.basic.html.BasicSignup;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.uqcode.UQCodeMgr;
import com.avicenna.uqcode.UQException;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Monitor
public class BasicCtrl extends Controller {

    private final FormFactory formFactory;

    private final LangUtil langUtil;

    private final AppCfgMgr cfgMgr;
    private final TmplMgr tmplMgr;
    private final SecMgr secMgr;
    private final EncMgr encMgr;
    private final UQCodeMgr uqCodeMgr;
    private final EmailMgr emailMgr;
    private final OwnerMgr ownerMgr;

    private final NotifPrv notifPrv;

    @Inject BasicCtrl(FormFactory formFactory, LangUtil langUtil,
                     AppCfgMgr cfgMgr, TmplMgr tmplMgr, SecMgr secMgr, EncMgr encMgr,
                      UQCodeMgr uqCodeMgr, EmailMgr emailMgr, OwnerMgr ownerMgr,
                     NotifPrv notifPrv) {

        this.formFactory = formFactory;

        this.langUtil = langUtil;

        this.cfgMgr = cfgMgr;
        this.tmplMgr = tmplMgr;
        this.secMgr = secMgr;
        this.encMgr = encMgr;
        this.uqCodeMgr = uqCodeMgr;
        this.emailMgr = emailMgr;
        this.ownerMgr = ownerMgr;

        this.notifPrv = notifPrv;
    }

    public Result signup() throws SecException {

        SecUserDto user = new SecUserDto();
        TmplDto tmpl = tmplMgr.getTmpl(langUtil.at("security.title.signupform"), "js/basic/basic.js", "css/basic/basic.css");

        OwnerMgr.OwnerDto ownerDto = ownerMgr.getOwner();

        String toc = ownerMgr.replace(ownerDto.getOwnerToc());

        return ok(BasicSignup.render(tmpl, user, ownerDto, toc));
    }

    public Result doSignup() throws SecException {

        Form<SecUserDto> form = formFactory.form(SecUserDto.class);
        form = form.bindFromRequest();

        SecUserDto secUser = form.get();
        secUser.setStatus(SecUserMdl.SecUserStatus.NEW);
        secUser.setUserType(SecUserMdl.SecUserType.USER);

        SecMgr.Response response = ((BasicMgr) secMgr).doSignup(secUser);

        if(response.isSuccess()) {

            try {

                String pairKey = encMgr.encrypt(secUser.getUsername());
                String signupCode = uqCodeMgr.getUniqueCode(5, pairKey);

                EmailMgr.EmailDto email = signupEmail(secUser.getDisplayName(), secUser.getEmail(), pairKey, signupCode);
                emailMgr.scheduleEmailToBeSent(email);

            } catch (EncException e) {
                throw new SecException(langUtil.at("security.error.signup.genpairkey"), e);
            } catch (UQException e) {
                throw new SecException(langUtil.at("security.error.signup.getsignupcode"), e);
            } catch (UnsupportedEncodingException e) {
                throw new SecException(langUtil.at("security.error.signup.urlencoding"), e);
            }

            notifPrv.get().addSuccess(response.getMessage()).flash(ctx());
            return redirect(routes.BasicCtrl.login());
        } else {

            notifPrv.get().addError(response.getMessage()).flash(ctx());
            return redirect(routes.BasicCtrl.signup());
        }
    }

    private EmailMgr.EmailDto signupEmail(String name, String email, String pairKey, String signupCode) throws UnsupportedEncodingException {

        EmailMgr.Address from = new EmailMgr.Address(cfgMgr.getString("email.from.address"), cfgMgr.getString("email.from.name"));

        List<EmailMgr.Address> tos = new ArrayList<>();
        tos.add(new EmailMgr.Address(email, name));

        String subject = langUtil.at("security.email.content.signup.tq");

        StringBuilder bodyBd = new StringBuilder();
        bodyBd.append(langUtil.at("security.email.content.signup.greet")+ " "+name + ", \r\n");
        bodyBd.append("\r\n");
        bodyBd.append(langUtil.at("security.email.content.signup.intro")+" "+cfgMgr.getString(TmplMgr.BrandingDefaultConf.APP_NAME.getKey())+". \r\n");
        bodyBd.append("\r\n");
        bodyBd.append(langUtil.at("security.email.content.signup.usecode")+" "+signupCode+". \r\n");
        bodyBd.append("\r\n");
        bodyBd.append(langUtil.at("security.email.content.signup.cplink")+" \r\n");
        bodyBd.append("\r\n");
        bodyBd.append(cfgMgr.getString(TmplMgr.SystemConf.HOST.getKey())+routes.BasicCtrl.doVerifySignup()
                +"?signupCode="+URLEncoder.encode(signupCode, StandardCharsets.UTF_8.toString())
                +"&pairKey="+URLEncoder.encode(pairKey, StandardCharsets.UTF_8.toString())+" \r\n");

        return new EmailMgr.EmailDto("Signup Email", signupCode, from, tos, subject, bodyBd.toString());
    }

    public Result doVerifySignup() throws SecException {

        String signupCode = request().getQueryString("signupCode");
        String pairKey = request().getQueryString("pairKey");

        if(StringUtils.isAnyBlank(signupCode, pairKey)) {
            notifPrv.get().addError(langUtil.at("security.error.doverifysignup.blankcodekey")).flash(ctx());
        }

        SecMgr.Response response = ((BasicMgr) secMgr).doVerifySignup(signupCode, pairKey);

        if(response.isSuccess()) {
            notifPrv.get().addSuccess(response.getMessage()).addInfo(langUtil.at("security.success.doverifysignup")).flash(ctx());
        }

        return redirect(routes.BasicCtrl.login());
    }

    public Result login() throws SecException {

        SecUserDto user = new SecUserDto();
        TmplDto tmpl = tmplMgr.getTmpl(langUtil.at("security.title.login"), "js/basic/basic.js", "css/basic/basic.css");

        if(cfgMgr.getString(TmplMgr.SystemConf.ALLOW_SIGNUP.getKey()).equals("true")) {
            user.setAllowSignup(true);
        } else {
            user.setAllowSignup(false);
        }

        return ok(BasicLogin.render(tmpl, user));
    }

    public Result logout() throws SecException {

        ctx().session().remove("secUser");
        ctx().response().discardCookie("rme");

        return redirect("/");
    }

    public Result doLogin() throws SecException {

        Form<SecUserDto> form = formFactory.form(SecUserDto.class);
        form = form.bindFromRequest();

        SecUserDto secUser = form.get();

        SecMgr.Response response = ((BasicMgr) secMgr).doLogin(secUser.getUsername(), secUser.getPassword(), secUser.isRememberMe(), ctx());

        if(response.isSuccess()) {

            SecUserDto userDb = secMgr.getCurrentUser(ctx());
            SecUserMdl.SecUserStatus status = userDb.getStatus();

            if(status.equals(SecUserMdl.SecUserStatus.NEW)) {

                notifPrv.get().addError(langUtil.at("security.error.dologin.emailnotverified")).flash(ctx());
                return redirect(routes.BasicCtrl.login());
            }

            // user is active
            notifPrv.get().addSuccess(response.getMessage()).flash(ctx());
            return redirect("/");
        } else {

            notifPrv.get().addError(response.getMessage()).flash(ctx());
            return redirect(routes.BasicCtrl.login());
        }
    }

    public Result forgotPwd() throws SecException {

        SecUserDto user = new SecUserDto();

        TmplDto tmpl = tmplMgr.getTmpl(langUtil.at("security.title.forgotpwd"), "js/basic/basic.js", "css/basic/basic.css");
        return ok(BasicForgotPassword.render(tmpl, user));
    }

    public Result doForgotPwd() throws SecException {

        DynamicForm form = formFactory.form().bindFromRequest();
        String usernameOrEmail = form.get("usernameOrEmail");

        if(StringUtils.isBlank(usernameOrEmail)) {
            notifPrv.get().addError(langUtil.at("security.error.doforgotpwd.usernameemailblank")).flash(ctx());
            return redirect(routes.BasicCtrl.forgotPwd());
        }

        SecUserDto user = secMgr.findUser(usernameOrEmail);
        if(user==null) {
            user = secMgr.findUserByEmail(usernameOrEmail);
        }

        if(user==null) {

            notifPrv.get().addSuccess(langUtil.at("security.success.doforgotpwd")).flash(ctx());
            return redirect(routes.BasicCtrl.login());
        }

        if(StringUtils.isBlank(user.getEmail())) {

            notifPrv.get().addError(langUtil.at("security.error.doforgotpwd.noemail")).flash(ctx());
            return redirect(routes.BasicCtrl.signup());
        }

        try {

            String pairKey = encMgr.encrypt(usernameOrEmail);
            String forgotPwdCode = uqCodeMgr.getUniqueCode(10, pairKey);

            EmailMgr.EmailDto email = forgotPasswordEmail(
                    user.getUsername(), user.getDisplayName(), user.getEmail(), pairKey, forgotPwdCode);
            emailMgr.scheduleEmailToBeSent(email);

            notifPrv.get().addSuccess(langUtil.at("security.success.doforgotpwd")).flash(ctx());
            return redirect(routes.BasicCtrl.login());

        } catch (EncException e) {
            throw new SecException(langUtil.at("security.error.doforgotpwd.encryption"), e);
        } catch (UQException e) {
            throw new SecException(langUtil.at("security.error.doforgotpwd.uqcodegen"), e);
        } catch (UnsupportedEncodingException e) {
            throw new SecException(langUtil.at("security.error.doforgotpwd.urlenc"), e);
        }
    }

    public Result doVerifyForgotPwd() throws SecException {

        String forgotPwdCode = request().getQueryString("forgotPwdCode");
        String pairKey = request().getQueryString("pairKey");

        if(StringUtils.isAnyBlank(forgotPwdCode, pairKey)) {
            notifPrv.get().addError(langUtil.at("security.error.doverforpwd.pairkeyblank")).flash(ctx());
        }

        SecMgr.Response response = ((BasicMgr) secMgr).doVerifyForgotPwd(forgotPwdCode, pairKey);

        if(response.isSuccess()) {
            notifPrv.get().addSuccess(response.getMessage()).addInfo(langUtil.at("security.success.doverforpwd")).flash(ctx());

            SecUserDto user = new SecUserDto();
            TmplDto tmpl = tmplMgr.getTmpl("Change Password Form", "js/basic/basic.js", "css/basic/basic.css");

            try {
                String secretKey = encMgr.encrypt(forgotPwdCode + "|" + pairKey);
                return ok(BasicChangePassword.render(tmpl, user, secretKey));
            } catch (EncException e) {
                throw new SecException(langUtil.at("security.error.doverforpwd.encryption"), e);
            }
        } else {

            notifPrv.get().addError(response.getMessage()).flash(ctx());
            return redirect(routes.BasicCtrl.forgotPwd());
        }
    }

    private EmailMgr.EmailDto forgotPasswordEmail(String username, String name, String email, String pairKey, String forgotPwdCode) throws UnsupportedEncodingException {

        EmailMgr.Address from = new EmailMgr.Address(cfgMgr.getString("email.from.address"), cfgMgr.getString("email.from.name"));

        List<EmailMgr.Address> tos = new ArrayList<>();
        tos.add(new EmailMgr.Address(email, name));

        String subject = "Reset your "+cfgMgr.getString(TmplMgr.BrandingDefaultConf.APP_NAME.getKey()) + " password";

        StringBuilder bodyBd = new StringBuilder();
        bodyBd.append("Dear "+ name + ", \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("Forgot your new password? Let's get you a new one. \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("We got request to change password for the account with the username " + username + ". \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("If you didn't request this change, you can ignore this email. If you do, copy and paste the link below into your browser. \r\n");
        bodyBd.append("\r\n");
        bodyBd.append(cfgMgr.getString(TmplMgr.SystemConf.HOST.getKey())+"/sec/basic/verifyForgotPwd?"
                +"forgotPwdCode="+ URLEncoder.encode(forgotPwdCode, StandardCharsets.UTF_8.toString())
                +"&pairKey="+URLEncoder.encode(pairKey, StandardCharsets.UTF_8.toString())+" \r\n");

        return new EmailMgr.EmailDto("Forgot Password", forgotPwdCode, from, tos, subject, bodyBd.toString());
    }

    public Result doChangePwd() throws SecException {

        DynamicForm form = formFactory.form().bindFromRequest();

        String secretKey = form.get("secretKey");
        String newPassword = form.get("password");

        if(StringUtils.isBlank(newPassword)) {

            notifPrv.get().addError("Password cannot be blank").flash(ctx());
            return redirect(routes.BasicCtrl.forgotPwd());
        }

        try {
            String decSecretKey = encMgr.decrypt(secretKey);

            if(StringUtils.isBlank(decSecretKey) || StringUtils.indexOf(decSecretKey, "|") < 0) {

                notifPrv.get().addError("Opps. Invalid change password request detected").flash(ctx());
                return redirect(routes.BasicCtrl.forgotPwd());
            }

            String[] arySecretKey = decSecretKey.split("\\|");

            String pairKey = uqCodeMgr.findReferenceKey(arySecretKey[0]);

            if(StringUtils.isBlank(pairKey)) {

                notifPrv.get().addError("Error while performing change password request. Pairkey is null").flash(ctx());
                return redirect(routes.BasicCtrl.forgotPwd());
            }

            if(!pairKey.equals(arySecretKey[1])) {

                notifPrv.get().addError("Error while performing change password request. Pairkey does not match").flash(ctx());
                return redirect(routes.BasicCtrl.forgotPwd());
            }

            String username = encMgr.decrypt(pairKey);
            SecUserDto user = secMgr.findUser(username);
            if(user == null) {
                user = secMgr.findUserByEmail(username);
            }

            if(user == null) {

                notifPrv.get().addError("Error while performing change password request. User cannot be found").flash(ctx());
                return redirect(routes.BasicCtrl.forgotPwd());
            }

            // validation sucessfull
            String encPassword = encMgr.encrypt(newPassword);
            SecMgr.Response response = ((BasicMgr) secMgr).changePassword(user.getUsername(), encPassword);
            if(response.isSuccess()) {

                notifPrv.get().addSuccess(response.getMessage()).flash(ctx());
                return redirect(routes.BasicCtrl.login());
            } else {

                notifPrv.get().addError(response.getMessage()).flash(ctx());
                return redirect(routes.BasicCtrl.forgotPwd());
            }


        } catch (EncException e) {
            throw new SecException("Error while processing change password request. Encryption exception", e);
        }
    }

}
