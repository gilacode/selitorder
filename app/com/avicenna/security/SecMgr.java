package com.avicenna.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.internal.cglib.core.$ProcessArrayCallback;
import io.ebean.OrderBy;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SecMgr {

    public SecMgr() {
    }

    public abstract String loginUrl() throws SecException;
    public abstract String logoutUrl() throws SecException;
    public abstract void createUser(SecUserDto user) throws SecException;
    public abstract List<SecUserDto> getUsers(UserParam param);
    public abstract SecUserDto findUser(String username);
    public abstract void makeSuperAdmin(String username);
    public abstract void downgradeSuperAdmin(String username);
    public abstract SecUserDto updateProfile(String username, String email, String displayName, String mobileNo, String photoUrl);
    public abstract void lockUser(String username);
    public abstract void unlockUser(String username);
    public abstract void activateUser(String username);
    public abstract void deactivateUser(String username);
    public abstract SecUserDto findUserByEmail(String username);
    public abstract SecUserDto getCurrentUser(Http.Context ctx) throws SecException;
    public abstract void changePassword(String username, String currPassword, String newPassword) throws SecException;
    public abstract String resetPassword(String username) throws SecException;
    public abstract void reloadCurrUser(String username) throws SecException;

    public static class UserParam {

        private List<UserField> userFields = new ArrayList<>();

        public UserParam() { }

        public UserParam filterByUserStatus(SecUserMdl.SecUserStatus... userStatuses) {
            this.userFields.add(new UserField(UserOrderBy.asc, "userStatus",
                    Arrays.stream(userStatuses).map(u -> u.name()).toArray(String[]::new)));
            return this;
        }

        public UserParam filterByUserType(SecUserMdl.SecUserType... userTypes) {
            this.userFields.add(new UserField(UserOrderBy.asc, "userType",
                    Arrays.stream(userTypes).map(u -> u.name()).toArray(String[]::new)));
            return this;
        }

        public UserParam filterDisplayName(String value, UserOrderBy orderBy) {
            this.userFields.add(new UserField(orderBy, "displayName", value));
            return this;
        }

        public UserParam filterEmail(UserOrderBy orderBy, String value) {
            this.userFields.add(new UserField(orderBy, "email", value));
            return this;
        }

        public UserParam filterMobileNo(UserOrderBy orderBy, String value) {
            this.userFields.add(new UserField(orderBy, "mobileNo", value));
            return this;
        }

        public List<UserField> getUserFields() {
            return userFields;
        }
    }

    public static class UserField {

        private final String fieldName;
        private final String[] fieldValues;
        private final UserOrderBy orderBy;

        public UserField(String fieldName, String... fieldValues) {
            this.fieldName = fieldName;
            this.fieldValues = fieldValues;
            this.orderBy = UserOrderBy.asc;
        }

        public UserField(UserOrderBy orderBy, String fieldName, String... fieldValues) {
            this.fieldName = fieldName;
            this.fieldValues = fieldValues;
            this.orderBy = orderBy;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String[] getFieldValues() {
            return fieldValues;
        }

        public UserOrderBy getOrderBy() {
            return orderBy;
        }
    }

    public enum UserOrderBy {
        asc, desc
    }

    public static class Response {

        private final boolean success;
        private final String message;

        @JsonCreator
        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
