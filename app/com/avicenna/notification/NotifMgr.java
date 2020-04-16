package com.avicenna.notification;

import akka.actor.ActorSystem;
import com.avicenna.config.AppCfgMgr;
import com.avicenna.email.EmailMgr;
import com.avicenna.logger.LogMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.security.SecUserDto;
import com.avicenna.util.DateTimeStatic;
import com.avicenna.util.DateTimeUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Singleton;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class NotifMgr {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    private final LogMgr logMgr;
    private final SecMgr secMgr;
    private final EmailMgr emailMgr;
    private final AppCfgMgr cfgMgr;

    private final DateTimeUtil dateTimeUtil;

    private final NotifTxnMdl.NotifTxnProv notifTxnProv;

    @Inject
    NotifMgr(ActorSystem actorSystem,
             ExecutionContext executionContext,
             LogMgr logMgr,
             SecMgr secMgr,
             EmailMgr emailMgr,
             AppCfgMgr cfgMgr,
             NotifTxnMdl.NotifTxnProv notifTxnProv,
             DateTimeUtil dateTimeUtil) {

        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.logMgr = logMgr;
        this.secMgr = secMgr;
        this.emailMgr = emailMgr;
        this.cfgMgr = cfgMgr;

        this.cfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("notif.max.days", "30", "No maximum days notifications to be fetch","Notification"));

        this.notifTxnProv = notifTxnProv;

        this.dateTimeUtil = dateTimeUtil;
    }

    /**
     * Method to send notification
     * @param channelDesc Indicator to describe the channel intention
     * @param fromUserName From email address
     * @param toUserName Recipient email address
     * @param topicSubject Email subject. Can be null for channel like SMS
     * @param message The message to be sent. Truncation might occur
     * @param categories Channel Category, WEB, SMS, EMAIL, etc
     */
    public void send(String channelDesc, String fromUserName, String toUserName, String topicSubject, String message, ChannelCategory... categories) {

        SecUserDto fromUser = secMgr.findUser(fromUserName);
        SecUserDto toUser = secMgr.findUser(toUserName);

        if(fromUser!=null && toUser!=null) {
            this.actorSystem.scheduler().scheduleOnce(
                    Duration.create(5, TimeUnit.SECONDS),
                    () -> {

                        for (ChannelCategory category : categories) {

                            switch (category) {
                                case SMS:
                                    break;
                                case WEB:
                                    logNotif(fromUserName, toUserName, category, channelDesc, toUser.getEmail(),  topicSubject, message);
                                    break;
                                case EMAIL:
                                    sendEmail(channelDesc, toUser.getEmail(), topicSubject, message);
                                    break;
                                case MOBILE_APP:
                                    break;
                            }


                        }
                    },
                    this.executionContext);
        }

    }

    public List<NotifDto> get(ChannelCategory category, String channelDesc, String recipientUsername, Boolean alreadyRead, int maxRows) {

        Query<NotifTxnMdl> query = notifTxnProv.get().query();
        query = query.orderBy("createdAt asc");
        query = query.setMaxRows(maxRows);

        ExpressionList<NotifTxnMdl> where = query.where();

        if(category!=null) {
            where = where.eq("category", category.name());
        }

        if(StringUtils.isNotBlank(channelDesc)) {
            where = where.eq("channelDesc", channelDesc);
        }

        if(StringUtils.isNotBlank(recipientUsername)) {
            where = where.eq("recipientUsername", recipientUsername);
        }

        if(alreadyRead!=null) {
            where = where.eq("alreadyRead", alreadyRead);
        }

        return ListUtils.emptyIfNull(where.findList()).stream()
                .map(n -> new NotifDto(n, secMgr)).collect(Collectors.toList());
    }

    public NotifDto find(String uuid) {
        return new NotifDto(notifTxnProv.get().query()
                .where().eq("id", uuid).findOne(), secMgr);
    }

    public void setAlreadyRead(String... uuids) {
        for(String uuid : uuids) {
            NotifTxnMdl db = notifTxnProv.get().query()
                    .where().eq("id", uuid).findOne();
            if(db!=null) {
                db.setAlreadyRead(true);
                db.update();
            }
        }

    }

    private void sendEmail(String channelDesc, String recipientId, String topicSubject, String message) {

        EmailMgr.Address from = new EmailMgr.Address(cfgMgr.getString("email.from.address"), cfgMgr.getString("email.from.name"));

        List<EmailMgr.Address> tos = new ArrayList<>();
        tos.add(new EmailMgr.Address(recipientId, null));

        EmailMgr.EmailDto emailDto = new EmailMgr.EmailDto(channelDesc, UUID.randomUUID().toString(), from, tos, topicSubject, message);

        emailMgr.scheduleEmailToBeSent(emailDto);
    }

    private void logNotif(String fromUsername, String toUsername, ChannelCategory category,
                          String channelDesc, String recipientId,
                          String topicSubject, String message) {
        logNotif(fromUsername, toUsername, category, channelDesc, recipientId, topicSubject, message, null);
    }

    private void logNotif(String fromUsername, String toUsername, ChannelCategory category,
                          String channelDesc, String recipientId,
                          String topicSubject, String message, String redirectUrl) {

        NotifTxnMdl db = new NotifTxnMdl();

        db.setId(UUID.randomUUID());
        db.setCreatedAt(new Date());
        db.setCreatedBy(fromUsername);
        db.setRecipientUsername(toUsername);
        db.setCategory(category);
        db.setChannelDesc(channelDesc);
        db.setRecipientId(recipientId);
        db.setTopicSubject(topicSubject);
        db.setNotifMessage(message);
        db.setDeliveryStatus(NotifDeliveryStatus.QUEUE);
        db.setRedirectUrl(redirectUrl);

        db.insert();
    }

    public enum NotifDeliveryStatus {

        QUEUE("Queued"),
        SENT("Sent"),
        UNKNOWN("Unknown")
        ;

        private final String message;

        NotifDeliveryStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }

    public enum ChannelCategory {

        SMS("SMS / Short Message System"),
        EMAIL("Email"),
        MOBILE_APP("Mobile App Notification"),
        WEB("Web Notification")
        ;

        private final String message;

        ChannelCategory(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class NotifDto {

        private final UUID id;
        private final Date createdAt;
        private final String createdAtFormatted;
        private final SecUserDto createdBy;
        private final NotifMgr.ChannelCategory category; // eg: SMS, WEB, EMAIL
        private final String channelDesc; // eg: Change Password
        private final String recipientId; // eg: 0165000462/ rashidee@sc-avicenna.com
        private final SecUserDto recipientUser; // eg: user1
        private final String topicSubject; // SMS will be null
        private final String notifMessage;
        private final NotifMgr.NotifDeliveryStatus deliveryStatus;
        private final String redirectUrl;
        private final boolean alreadyRead;

        @JsonCreator
        public NotifDto(UUID id, Date createdAt, String createdAtFormatted, SecUserDto createdBy, ChannelCategory category, String channelDesc,
                        String recipientId, SecUserDto recipientUser, String topicSubject, String notifMessage,
                        NotifDeliveryStatus deliveryStatus, String redirectUrl, boolean alreadyRead) {
            this.id = id;
            this.createdAt = createdAt;
            this.createdAtFormatted = DateTimeStatic.getDateTimeUtil().getDateTime(createdAt);
            this.createdBy = createdBy;
            this.category = category;
            this.channelDesc = channelDesc;
            this.recipientId = recipientId;
            this.recipientUser = recipientUser;
            this.topicSubject = topicSubject;
            this.notifMessage = notifMessage;
            this.deliveryStatus = deliveryStatus;
            this.redirectUrl = redirectUrl;
            this.alreadyRead = alreadyRead;
        }

        public NotifDto(NotifTxnMdl db, SecMgr secMgr) {
            this.id = db.getId();
            this.createdAt = db.getCreatedAt();
            this.createdAtFormatted = DateTimeStatic.getDateTimeUtil().getDateTime(db.getCreatedAt());
            this.createdBy = secMgr.findUser(db.getCreatedBy());
            this.category = db.getCategory();
            this.channelDesc = db.getChannelDesc();
            this.recipientId = db.getRecipientId();
            this.recipientUser = secMgr.findUser(db.getRecipientUsername());
            this.topicSubject = db.getTopicSubject();
            this.notifMessage = db.getNotifMessage();
            this.deliveryStatus = db.getDeliveryStatus();
            this.redirectUrl = db.getRedirectUrl();
            this.alreadyRead = db.isAlreadyRead();
        }

        public UUID getId() {
            return id;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public SecUserDto getCreatedBy() {
            return createdBy;
        }

        public ChannelCategory getCategory() {
            return category;
        }

        public String getChannelDesc() {
            return channelDesc;
        }

        public String getRecipientId() {
            return recipientId;
        }

        public SecUserDto getRecipientUser() {
            return recipientUser;
        }

        public String getTopicSubject() {
            return topicSubject;
        }

        public String getNotifMessage() {
            return notifMessage;
        }

        public NotifDeliveryStatus getDeliveryStatus() {
            return deliveryStatus;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public boolean isAlreadyRead() {
            return alreadyRead;
        }

        public String getCreatedAtFormatted() {
            return createdAtFormatted;
        }
    }
}
