package com.avicenna.security.basic;

import com.avicenna.BaseCtrl;
import com.avicenna.audit.AuditMgr;
import com.avicenna.config.AppCfgMgr;
import com.avicenna.email.EmailMgr;
import com.avicenna.file.FileException;
import com.avicenna.file.FileMgr;
import com.avicenna.nav.NavMgr;
import com.avicenna.notification.NotifMgr;
import com.avicenna.security.*;
import com.avicenna.security.basic.html.BasicUserList;
import com.avicenna.security.basic.html.BasicProfile;
import com.avicenna.security.basic.html.BasicUserForm;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.LangUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BasicUserCtrl extends BaseCtrl {

    private final FormFactory formFactory;

    private final FileMgr fileMgr;
    private final AuditMgr auditMgr;
    private final NotifMgr notifMgr;
    private final EmailMgr emailMgr;
    private final AppCfgMgr cfgMgr;

    private final NotifPrv notifPrv;
    private final DateTimeUtil dateTimeUtil;
    private final LangUtil langUtil;

    @Inject
    BasicUserCtrl(FormFactory formFactory, TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr, FileMgr fileMgr, AuditMgr auditMgr, NotifMgr notifMgr,
                  EmailMgr emailMgr, AppCfgMgr cfgMgr, NotifPrv notifPrv, DateTimeUtil dateTimeUtil, LangUtil langUtil) {
        super(tmplMgr, secMgr, navMgr);

        this.formFactory = formFactory;

        this.fileMgr = fileMgr;
        this.auditMgr = auditMgr;
        this.notifMgr = notifMgr;
        this.emailMgr = emailMgr;
        this.cfgMgr = cfgMgr;

        this.notifPrv = notifPrv;
        this.dateTimeUtil = dateTimeUtil;
        this.langUtil = langUtil;
    }

    public Result showUsers() {
        return _showUsers(BasicNav.Permission.SEC_USR_VIEW_LIST.name(), false, "/sec/basic/users", "/sec/basic/users");
    }

    public Result _showUsers(String permission, boolean hideNewButton, String redirectUrl, String filterUrl, SecUserMdl.SecUserType... allowedUserTypes) {

        if(navMgr.hasPermission(permission, currentUser())) {

            SecUserMdl.SecUserStatus status = SecUserMdl.SecUserStatus.ACTIVE;
            String strStatus = request().getQueryString("status");
            if(StringUtils.isNotBlank(strStatus)) {
                status = SecUserMdl.SecUserStatus.valueOf(strStatus);
            }

            SecUserMdl.SecUserType type = SecUserMdl.SecUserType.USER;
            String strType = request().getQueryString("type");
            if(StringUtils.isNotBlank(strType)) {
                type = SecUserMdl.SecUserType.valueOf(strType);
            }

            SecMgr.UserParam param = new SecMgr.UserParam();

            if(status!=null) {
                param = param.filterByUserStatus(status);
            }

            if(type!=null) {
                param = param.filterByUserType(type);
            }

            List<SecUserDto> users = secMgr.getUsers(param);
            users.stream().forEach(u -> {
                u.setRedirectUrl(redirectUrl+"/"+u.getUsername());
            });

            final SecUserMdl.SecUserStatus finalStatus = status;
            List<Filter> statusFilter = Arrays.stream(SecUserMdl.SecUserStatus.values())
                    .map(s -> {
                        Filter filter = null;
                        if(finalStatus!=null && s.name().equals(finalStatus.name())) {
                            filter = new Filter(s.name(), langUtil.at(s.getMessageKey()), true, false);
                        } else {
                            filter = new Filter(s.name(), langUtil.at(s.getMessageKey()), false, false);
                        }
                        return filter;
                    })
                    .collect(Collectors.toList());

            if(allowedUserTypes==null || allowedUserTypes.length == 0) {
                allowedUserTypes = SecUserMdl.SecUserType.values();
            }
            List<SecUserMdl.SecUserType> lstAllowedUserTypes = Arrays.asList(allowedUserTypes);

            final SecUserMdl.SecUserType finalType = type;
            List<Filter> typeFilter = Arrays.stream(SecUserMdl.SecUserType.values())
                    .filter(t -> lstAllowedUserTypes.contains(t))
                    .map(t -> {
                        Filter filter;
                        if(finalType!=null && t.name().equals(finalType.name())) {
                            filter = new Filter(t.name(), langUtil.at(t.getMessageKey()), true, t.isInternal());
                        } else {
                            filter = new Filter(t.name(), langUtil.at(t.getMessageKey()), false, t.isInternal());
                        }
                        return filter;
                    })
                    .collect(Collectors.toList());

            TmplDto tmpl = tmplMgr.getTmpl("User List", "js/basic/basic.js", "css/nav/nav.css");
            return ok(BasicUserList.render(currentUser(), users, statusFilter, typeFilter, tmpl, dateTimeUtil, hideNewButton, filterUrl));

        } else {

            notifPrv.get().addError("You do not have permission to view user information").flash(ctx());
            return redirect("/");

        }

    }

    public Result newUserForm() {

        TmplDto tmpl = tmplMgr.getTmpl("Create New User", "js/basic/basic.js", "css/nav/nav.css");
        return ok(BasicUserForm.render(currentUser(), new SecUserDto(), tmpl));
    }

    public Result saveUser() {

        try {
            if (navMgr.hasPermission(BasicNav.Permission.SEC_USR_CREATE_USR.name(), currentUser())) {

                TmplDto tmpl = tmplMgr.getTmpl("Create New User", "js/basic/basic.js", "css/nav/nav.css");

                Form<SecUserDto> form = formFactory.form(SecUserDto.class);
                form = form.bindFromRequest();
                SecUserDto user = form.get();

                if (StringUtils.isBlank(user.getEmail())) {

                    notifPrv.get().addError("Email cannot be blank").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                if (StringUtils.isBlank(user.getDisplayName())) {

                    notifPrv.get().addError("Display name cannot be blank").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                if (StringUtils.isBlank(user.getPassword())) {

                    notifPrv.get().addError("Password cannot be blank").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                if (StringUtils.isBlank(user.getConfirmPassword())) {

                    notifPrv.get().addError("Confirm password field cannot be blank").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                if (!user.getPassword().equals(user.getConfirmPassword())) {

                    notifPrv.get().addError("Password does not match").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                SecUserDto newUser = secMgr.findUser(user.getUsername());
                if (newUser != null) {

                    notifPrv.get().addError("User with username "+user.getUsername()+" already exist").flash(ctx());
                    return ok(BasicUserForm.render(currentUser(), user, tmpl));
                }

                newUser = new SecUserDto();
                newUser.setUsername(user.getUsername());
                newUser.setEmail(user.getEmail());
                newUser.setDisplayName(user.getDisplayName());
                newUser.setPassword(user.getPassword());
                newUser.setStatus(SecUserMdl.SecUserStatus.ACTIVE);
                newUser.setUserType(SecUserMdl.SecUserType.USER);
                newUser.setRequireResetPwd(user.isRequireResetPwd());

                secMgr.createUser(newUser);

                return redirect(routes.BasicUserCtrl.editProfile(newUser.getUsername()));
            } else {

                notifPrv.get().addError("You do not have permission to create new user").flash(ctx());
                return redirect("/");
            }
        } catch (SecException e) {

            notifPrv.get().addError("Error while creating user").flash(ctx());
            return redirect("/");
        }
    }

    public Result editProfile(String username) {

        if(navMgr.hasPermission(BasicNav.Permission.SEC_USR_VIEW_LIST.name(), currentUser()) || currentUser().getUsername().equals(username)) {

            SecUserDto user = secMgr.findUser(username);

            if (user == null) {

                notifPrv.get().addError("User does not exist").flash(ctx());
                return redirect("/");
            }

            List<AuditMgr.AuditDto> audits = auditMgr.get(username, null, null, null);

            List<NotifMgr.NotifDto> notifs = ListUtils.emptyIfNull(notifMgr.get(NotifMgr.ChannelCategory.WEB, null, username, null, 30));

            return ok(BasicProfile.render(currentUser(), user, audits, notifs,
                    tmplMgr.getTmpl(langUtil.at("profile.title.myprofile"), "js/basic/basic.js", "css/nav/nav.css"), dateTimeUtil));
        } else {

            notifPrv.get().addError("You do not have permission to view user information").flash(ctx());
            return redirect("/");
        }
    }

    public Result updateProfile(String username) {

        if(navMgr.hasPermission(BasicNav.Permission.SEC_USR_UPD_PROF.name(), currentUser()) || currentUser().getUsername().equals(username)) {

            SecUserDto user = secMgr.findUser(username);

            if (user == null) {

                notifPrv.get().addError("User does not exist").flash(ctx());
                return redirect("/");
            }

            try {

                DynamicForm form = formFactory.form();
                form = form.bindFromRequest();

                // display name
                String displayName = form.get("displayName");
                if(StringUtils.isBlank(displayName)) {
                    notifPrv.get().addError(langUtil.at("profile.error.displayname")).flash(ctx());
                }

                // email
                String email = form.get("email");
                if(StringUtils.isBlank(email)) {
                    notifPrv.get().addError(langUtil.at("profile.error.email")).flash(ctx());
                }

                // email
                String mobileNo = form.get("mobileNo");
                if(StringUtils.isBlank(mobileNo)) {
                    mobileNo = user.getMobileNo();
                }

                // photo upload

                String photoUrl = null;

                Http.MultipartFormData<File> body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart<File> photo = body.getFile("photo");

                if (photo != null) {

                    String fileName = photo.getFilename();

                    if (StringUtils.isNotBlank(fileName)) {

                        String contentType = photo.getContentType();

                        String extension = FilenameUtils.getExtension(fileName);
                        if (StringUtils.isBlank(extension)) {
                            extension = "";
                        }
                        extension = StringUtils.lowerCase(extension);

                        File file = photo.getFile();

                        BufferedImage originalImg = ImageIO.read(file);
                        BufferedImage scaledImg  = Scalr.resize(originalImg, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 300, 300);
                        File destFile = new File(file.getAbsolutePath());
                        ImageIO.write(scaledImg, "jpg", destFile);

                        FileMgr.FileDto fileDto = fileMgr.upload("profilePhoto", username, contentType, extension, destFile);
                        photoUrl = fileDto.getFileAttribute().getFileUrl();
                    }
                }

                secMgr.updateProfile(username, email, displayName, mobileNo, photoUrl);

                auditMgr.audit(currentUser().getUsername(), "Profile", "Update Profile",
                        username, username + " profile updated");

                secMgr.reloadCurrUser(username);

                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));

            } catch (SecException e) {

                Logger.error(this.getClass().getSimpleName(), e);
                notifPrv.get().addError(langUtil.at("profile.error.updateprofile")).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            } catch (FileException e) {

                Logger.error(this.getClass().getSimpleName(), e);
                notifPrv.get().addError(langUtil.at("profile.error.uploadphoto")).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            } catch (IOException e) {

                Logger.error(this.getClass().getSimpleName(), e);
                notifPrv.get().addError(langUtil.at("profile.error.uploadphoto")).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            }

        } else {

            notifPrv.get().addError("You do not have permission to update user information").flash(ctx());
            return redirect("/");
        }
    }

    public Result changePassword(String username) {

        if(currentUser().getUsername().equals(username)) {

            try {

                SecUserDto user = secMgr.findUser(username);

                if (user == null) {

                    notifPrv.get().addError("User does not exist").flash(ctx());
                    return redirect("/");
                }

                DynamicForm form = formFactory.form();
                form = form.bindFromRequest();

                String currPassword = form.get("currentPassword");
                String confirmPassword = form.get("confirmPassword");
                String newPassword = form.get("newPassword");

                if (StringUtils.isBlank(currPassword)) {
                    notifPrv.get().addError(langUtil.at("profile.error.currpwdblank")).flash(ctx());
                    return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
                }

                if (StringUtils.isBlank(newPassword)) {
                    notifPrv.get().addError(langUtil.at("profile.error.newpwdblank")).flash(ctx());
                    return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
                }

                if (StringUtils.isBlank(confirmPassword)) {
                    notifPrv.get().addError(langUtil.at("profile.error.confpwdblank")).flash(ctx());
                    return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
                }

                if (!newPassword.equals(confirmPassword)) {
                    notifPrv.get().addError(langUtil.at("profile.error.pwdnotmatch")).flash(ctx());
                    return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
                }

                auditMgr.audit(username, "Security", "Change Password", username, username+" requested to change password");

                secMgr.changePassword(username, currPassword, newPassword);

                auditMgr.audit(username, "Security", "Change Password", username, username+" request to change password successful");

                notifMgr.send("Change Password", currentUser().getUsername(), username, "Password has been changed",
                        "Your request to change password is successful. Please report to us if you have not requested to change your password",
                        NotifMgr.ChannelCategory.EMAIL, NotifMgr.ChannelCategory.WEB);

                notifPrv.get().addSuccess(langUtil.at("profile.success.changepwd")).flash(ctx());

                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));

            } catch (SecException e) {

                notifPrv.get().addError(langUtil.at("profile.error.changepwd", e)).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            }

        } else {

            notifPrv.get().addError("You do not have permission to change password").flash(ctx());
            return redirect("/");
        }
    }

    public Result resetPassword(String username) {

        if(navMgr.hasPermission(BasicNav.Permission.SEC_USR_RESET_PWD.name(), currentUser())) {

            try {

                SecUserDto user = secMgr.findUser(username);

                if (user == null) {

                    notifPrv.get().addError("User does not exist").flash(ctx());
                    return redirect("/");
                }

                auditMgr.audit(currentUser().getUsername(), "Security", "Reset Password", username, currentUser().getUsername()+" reset "+username+" password");

                String newPassword = secMgr.resetPassword(username);

                EmailMgr.EmailDto email = resetPasswordEmail(username, user.getDisplayName(), user.getEmail(), newPassword);

                emailMgr.scheduleEmailToBeSent(email);

                notifMgr.send("Reset Password", currentUser().getUsername(), username, "Password reset",
                        currentUser().getDisplayName()+" has reset your password. An email with your new password will be sent shortly.",
                        NotifMgr.ChannelCategory.EMAIL, NotifMgr.ChannelCategory.WEB);

                notifPrv.get().addSuccess(langUtil.at("profile.success.resetpwd")).flash(ctx());

                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));

            } catch (SecException e) {

                notifPrv.get().addError(langUtil.at("profile.error.resetpwd", e)).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            } catch (UnsupportedEncodingException e) {

                notifPrv.get().addError(langUtil.at("profile.error.resetpwd", e)).flash(ctx());
                return redirect(com.avicenna.security.basic.routes.BasicUserCtrl.editProfile(username));
            }

        } else {

            notifPrv.get().addError("You do not have permission to update user password").flash(ctx());
            return redirect("/");
        }
    }

    private EmailMgr.EmailDto resetPasswordEmail(String username, String name, String email, String newPassword) throws UnsupportedEncodingException {

        EmailMgr.Address from = new EmailMgr.Address(cfgMgr.getString("email.from.address"), cfgMgr.getString("email.from.name"));

        List<EmailMgr.Address> tos = new ArrayList<>();
        tos.add(new EmailMgr.Address(email, name));

        String subject = "Your new "+cfgMgr.getString(TmplMgr.BrandingDefaultConf.APP_NAME.getKey()) + " password";

        StringBuilder bodyBd = new StringBuilder();
        bodyBd.append("Dear "+ name + ", \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("Here is you new password. \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("Password " + newPassword + " \r\n");
        bodyBd.append("\r\n");
        bodyBd.append("Please change your password immediately upon logging in. \r\n");

        return new EmailMgr.EmailDto("Reset Password", username, from, tos, subject, bodyBd.toString());
    }

    public Result makeSuperAdmin(String username) {

        if(navMgr.hasPermission(BasicNav.Permission.SEC_USR_MAKE_ADMIN.name(), currentUser())) {

            SecUserDto user = secMgr.findUser(username);

            if (user == null) {

                notifPrv.get().addError("User does not exist").flash(ctx());
                return redirect("/");
            }

            secMgr.makeSuperAdmin(username);

            notifPrv.get().addSuccess("User "+username+" has been upgraded to Super Admin").flash(ctx());
            return redirect(com.avicenna.nav.routes.NavCtrl.showUserNav(username));

        } else {

            notifPrv.get().addError("You do not have permission to view access level configuration").flash(ctx());
            return redirect("/");

        }
    }

    public Result downgradeSuperAdmin(String username) {

        if(navMgr.hasPermission(BasicNav.Permission.SEC_USR_MAKE_ADMIN.name(), currentUser())) {

            SecUserDto user = secMgr.findUser(username);

            if (user == null) {

                notifPrv.get().addError("User does not exist").flash(ctx());
                return redirect("/");
            }

            secMgr.downgradeSuperAdmin(username);

            notifPrv.get().addSuccess("User "+username+" has been downgraded to normal user").flash(ctx());
            return redirect(com.avicenna.nav.routes.NavCtrl.showUserNav(username));

        } else {

            notifPrv.get().addError("You do not have permission to view access level configuration").flash(ctx());
            return redirect("/");

        }
    }

    public static class Filter {

        private final String code;
        private final String desc;
        private final boolean checked;
        private final boolean internal;

        public Filter(String code, String desc, boolean checked, boolean internal) {
            this.code = code;
            this.desc = desc;
            this.checked = checked;
            this.internal = internal;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public boolean isChecked() {
            return checked;
        }

        public boolean isInternal() {
            return internal;
        }
    }
}
