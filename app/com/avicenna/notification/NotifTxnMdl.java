package com.avicenna.notification;

import akka.io.ChannelRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.Index;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.ebean.EbeanConfig;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "notif_txn")
public class NotifTxnMdl extends Model {

    public static class NotifTxnProv implements Provider<Finder<Long, NotifTxnMdl>> {

        private final EbeanConfig ebeanConfig; // workaround to ensure ebean is loaded first by injecting this bean
        private final DynamicEvolutions dynamicEvolutions; // workaround to ensure ebean is loaded first by injecting this bean

        @Inject NotifTxnProv(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
            this.ebeanConfig = ebeanConfig;
            this.dynamicEvolutions = dynamicEvolutions;
        }

        @Override
        public Finder<Long, NotifTxnMdl> get() {
            return new Finder<>(NotifTxnMdl.class);
        }
    }

    @Id
    private UUID id;

    @NotNull
    @CreatedTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Index(unique = false)
    @NotNull
    private String createdBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotifMgr.ChannelCategory category; // eg: SMS, WEB, EMAIL

    @NotNull
    private String channelDesc; // eg: Change Password

    @NotNull
    private String recipientId; // eg: 0165000462/ rashidee@sc-avicenna.com

    @Index(unique = false)
    @NotNull
    private String recipientUsername; // eg: user1

    private String topicSubject; // SMS will be null

    @Lob
    private String notifMessage;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotifMgr.NotifDeliveryStatus deliveryStatus;

    private String redirectUrl;

    private boolean alreadyRead;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public NotifMgr.ChannelCategory getCategory() {
        return category;
    }

    public void setCategory(NotifMgr.ChannelCategory category) {
        this.category = category;
    }

    public String getChannelDesc() {
        return channelDesc;
    }

    public void setChannelDesc(String channelDesc) {
        this.channelDesc = channelDesc;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getTopicSubject() {
        return topicSubject;
    }

    public void setTopicSubject(String topicSubject) {
        this.topicSubject = topicSubject;
    }

    public String getNotifMessage() {
        return notifMessage;
    }

    public void setNotifMessage(String notifMessage) {
        this.notifMessage = notifMessage;
    }

    public NotifMgr.NotifDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(NotifMgr.NotifDeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isAlreadyRead() {
        return alreadyRead;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead = alreadyRead;
    }
}
