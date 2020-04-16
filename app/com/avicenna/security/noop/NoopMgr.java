package com.avicenna.security.noop;

import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.security.SecUserMdl;
import com.google.inject.Inject;
import play.mvc.Http;

import java.util.Collections;
import java.util.List;

public class NoopMgr extends SecMgr {

    @Inject NoopMgr() {
    }

    @Override
    public void changePassword(String username, String currPassword, String newPassword) throws SecException {

    }

    @Override
    public String resetPassword(String username) throws SecException {
        return null;
    }

    @Override
    public void reloadCurrUser(String username) throws SecException {

    }

    @Override
    public SecUserDto findUserByEmail(String username) {
        return null;
    }

    @Override
    public SecUserDto getCurrentUser(Http.Context ctx) throws SecException {

        SecUserDto user = new SecUserDto();
        user.setUsername("noop");
        user.setDisplayName("Noop");
        user.setAllowSignup(false);
        user.setRememberMe(true);
        user.setStatus(SecUserMdl.SecUserStatus.ACTIVE);

        return user;
    }

    @Override
    public String loginUrl() {
        return "/";
    }

    @Override
    public String logoutUrl() {
        return "/";
    }

    @Override
    public void createUser(SecUserDto user) throws SecException  {

    }

    @Override
    public List<SecUserDto> getUsers(UserParam param) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public SecUserDto findUser(String username) {
        return null;
    }

    @Override
    public void makeSuperAdmin(String username) {

    }

    @Override
    public void downgradeSuperAdmin(String username) {

    }

    @Override
    public SecUserDto updateProfile(String username, String email, String displayName, String mobileNo, String photoUrl) {
        return null;
    }

    @Override
    public void lockUser(String username) {
    }

    @Override
    public void unlockUser(String username) {
    }

    @Override
    public void activateUser(String username) {

    }

    @Override
    public void deactivateUser(String username) {

    }
}
