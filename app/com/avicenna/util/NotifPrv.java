package com.avicenna.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import play.Logger;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;

public class NotifPrv implements Provider<NotifPrv.Builder> {

    private final ObjectMapper mapper;

    @Inject NotifPrv(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Builder get() {
        return new Builder(mapper);
    }

    public static class Builder {

        private final ObjectMapper mapper;

        private final List<Notification> notifications = new ArrayList<>();

        public Builder(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public Builder addError(String message) {
            notifications.add(new Notification(Type.DANGER.getBgClass(), Type.DANGER.getTextClass(), Type.DANGER.getIconClass(), message));
            return this;
        }

        public Builder addWarning(String message) {
            notifications.add(new Notification(Type.WARNING.getBgClass(), Type.WARNING.getTextClass(), Type.WARNING.getIconClass(), message));
            return this;
        }

        public Builder addInfo(String message) {
            notifications.add(new Notification(Type.INFO.getBgClass(), Type.INFO.getTextClass(), Type.INFO.getIconClass(), message));
            return this;
        }

        public Builder addSuccess(String message) {
            notifications.add(new Notification(Type.SUCCESS.getBgClass(), Type.SUCCESS.getTextClass(), Type.SUCCESS.getIconClass(), message));
            return this;
        }

        public void flash(Http.Context ctx)  {
            try {
                String json = mapper.writeValueAsString(notifications);
                ctx.flash().put("NOTIFICATION", json);
            } catch (JsonProcessingException e) {
                Logger.error(this.getClass().getSimpleName(), e);
            }
        }
    }

    public static class Notification {

        private String bgClass;
        private String textClass;
        private String iconClass;
        private String message;

        public Notification() { }

        public Notification(String bgClass, String textClass, String iconClass, String message) {
            this.bgClass = bgClass;
            this.textClass = textClass;
            this.iconClass = iconClass;
            this.message = message;
        }

        public String getBgClass() {
            return bgClass;
        }

        public void setBgClass(String bgClass) {
            this.bgClass = bgClass;
        }

        public String getTextClass() {
            return textClass;
        }

        public void setTextClass(String textClass) {
            this.textClass = textClass;
        }

        public String getIconClass() {
            return iconClass;
        }

        public void setIconClass(String iconClass) {
            this.iconClass = iconClass;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public enum Type {

        DANGER("alert alert-danger","text-danger","fa fa-times-circle"),
        INFO("alert alert-info","text-info","fa fa-exclamation-circle"),
        WARNING("alert alert-warning","text-warning","fa fa-exclamation-triangle"),
        SUCCESS("alert alert-success","text-success","fa fa-check-circle");

        private final String bgClass;
        private final String textClass;
        private final String iconClass;

        Type(String bgClass, String textClass, String iconClass) {
            this.bgClass = bgClass;
            this.textClass = textClass;
            this.iconClass = iconClass;
        }

        public String getBgClass() {
            return bgClass;
        }

        public String getTextClass() {
            return textClass;
        }

        public String getIconClass() {
            return iconClass;
        }

    }


}
