package com.avicenna.security;

public class SecUserDto {

    private String username;
    private String displayName;
    private String password; // this is supposed to be encrypted
    private String photoUrl;
    private String email;

    private String mobileNo;

    private String confirmPassword;
    private boolean requireResetPwd;

    private SecUserMdl.SecUserType userType;

    private SecUserMdl.SecUserStatus status;

    private boolean allowSignup = true;

    private boolean rememberMe;

    private String redirectUrl;

    public SecUserDto() { }

    public SecUserDto(String username, String displayName, String password, String photoUrl, String email,
                      SecUserMdl.SecUserType userType, SecUserMdl.SecUserStatus status) {
        this.username = username;
        this.displayName = displayName;
        this.password = password;
        this.photoUrl = photoUrl;
        this.email = email;
        this.userType = userType;
        this.status = status;
    }

    public SecUserDto(SecUserMdl userDb) {
        this.username = userDb.getUsername();
        this.displayName = userDb.getDisplayName();
        this.email = userDb.getEmail();
        this.photoUrl = userDb.getPhotoUrl();
        this.status = userDb.getUserStatus();
        this.userType = userDb.getUserType();
        this.mobileNo = userDb.getMobileNo();
    }

    public SecUserMdl.SecUserType getUserType() {
        return userType;
    }

    public void setUserType(SecUserMdl.SecUserType userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAllowSignup() {
        return allowSignup;
    }

    public void setAllowSignup(boolean allowSignup) {
        this.allowSignup = allowSignup;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public SecUserMdl.SecUserStatus getStatus() {
        return status;
    }

    public void setStatus(SecUserMdl.SecUserStatus status) {
        this.status = status;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isRequireResetPwd() {
        return requireResetPwd;
    }

    public void setRequireResetPwd(boolean requireResetPwd) {
        this.requireResetPwd = requireResetPwd;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }
}
