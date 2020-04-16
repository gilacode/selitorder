package com.avicenna.email;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.logger.LogMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.FilterUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class EmailMgr {

    private final DateTimeUtil dateTimeUtil;

    protected final AppCfgMgr appCfg;
    protected final LogMgr dbLog;

    private final EmailTrxMdl.EmailTrxProvider emailTrxProv;

    public EmailMgr(DateTimeUtil dateTimeUtil, AppCfgMgr appCfg, LogMgr dbLog,
                    EmailTrxMdl.EmailTrxProvider emailTrxProv) {

        this.dateTimeUtil = dateTimeUtil;

        this.appCfg = appCfg;
        this.dbLog = dbLog;

        this.emailTrxProv = emailTrxProv;

        this.appCfg.registerConfig(new AppCfgMgr.AppCfgDto("email.from.name", "No Reply", "Email From Name","Email"));
        this.appCfg.registerConfig(new AppCfgMgr.AppCfgDto("email.from.address", "noreply@example.com", "Email From Address", "Email"));
    }

    public void scheduleEmailToBeSent(EmailDto emailDto) {

        EmailTrxMdl db = new EmailTrxMdl();

        db.setFromAddr(emailDto.getFrom().toString());
        db.setToAddrs(StringUtils.join(emailDto.getTo(), ", "));
        db.setEmailSubject(emailDto.getSubject());
        db.setBodyText(emailDto.getBodyText());
        db.setReferenceKey(emailDto.getReferenceKey());
        db.setStatus(EmailStatus.NEW);
        db.setEmailGroup(emailDto.getGroup());

        db.insert();
    }

    protected abstract boolean deliverEmail(EmailDto emailDto);

    public EmailDto findEmail(String referenceKey) {

        EmailTrxMdl db = emailTrxProv.get().query().where().eq("referenceKey", referenceKey).findOne();
        if(db!=null) {
            return new EmailDto(db);
        }

        return null;
    }

    public List<EmailDto> getEmails(Filter filter) {

        List<EmailDto> result = new ArrayList<>();

        Query<EmailTrxMdl> query = emailTrxProv.get().query();

        /*
         * ORDER BY
         */

        // reference key
        if(filter.getReferenceKey()!=null && StringUtils.isNotBlank(filter.getReferenceKey().getValue())) {
            query = query.orderBy("referenceKey "+filter.getReferenceKey().getSortType().name());
        }

        // created at
        if(filter.getDateCreated()!=null && filter.getDateCreated().getStart()!=null && filter.getDateCreated().getEnd()!=null) {
            query = query.orderBy("createdAt "+filter.getDateCreated().getSortType().name());
        }

        // email group
        if(filter.getEmailGroup()!=null && !filter.getEmailGroup().getValues().isEmpty()) {
            query = query.orderBy("emailGroup "+filter.getEmailGroup().getSortType().name());
        }

        // email group
        if(filter.getStatus()!=null && !filter.getStatus().getValues().isEmpty()) {
            query = query.orderBy("status "+filter.getStatus().getSortType().name());
        }

        /*
         * WHERE CLAUSE
         */

        ExpressionList<EmailTrxMdl> where = query.where();

        // reference key
        if(filter.getReferenceKey()!=null && StringUtils.isNotBlank(filter.getReferenceKey().getValue())) {
            where = where.eq("referenceKey", filter.getReferenceKey().getValue());
        }

        // created at
        if(filter.getDateCreated()!=null && filter.getDateCreated().getStart()!=null && filter.getDateCreated().getEnd()!=null) {
            where = where.between("createdAt",
                    dateTimeUtil.setSOD(filter.getDateCreated().getStart()),
                    dateTimeUtil.setEOD(filter.getDateCreated().getEnd()));
        }

        // email group
        if(filter.getEmailGroup()!=null && !filter.getEmailGroup().getValues().isEmpty()) {
            where = where.in("emailGroup", filter.getEmailGroup().getValues());
        }

        // email group
        if(filter.getStatus()!=null && !filter.getStatus().getValues().isEmpty()) {
            where = where.in("status", filter.getStatus().getValues());
        }

        List<EmailTrxMdl> dbs = ListUtils.emptyIfNull(where.findList());

        for(EmailTrxMdl db : dbs) {
            result.add(new EmailDto(db));
        }

        return result;
    }

    public static class Filter {

        private int maxRow = 100;

        private FilterUtil.SingleStringValue referenceKey;
        private FilterUtil.DateRangeValue dateCreated;
        private FilterUtil.MultipleStringValue emailGroup;
        private FilterUtil.MultipleStringValue status;

        public Filter(int maxRow) {
            this.maxRow = maxRow;
        }

        // reference key
        public Filter setReferenceKey(String referenceKey) {
            this.referenceKey = new FilterUtil.SingleStringValue(
                    "referenceKey", referenceKey, FilterUtil.SortType.asc);
            return this;
        }

        // created at
        public Filter filterByCreatedDate(FilterUtil.SortType sortType, Date start, Date end) {
            this.dateCreated = new FilterUtil.DateRangeValue("createdAt", start, end);
            return this;
        }

        // email group
        public Filter filterByGroup(FilterUtil.SortType sortType, String... groups) {
            this.emailGroup = new FilterUtil.MultipleStringValue("emailGroup", sortType, groups);
            return this;
        }

        // email status
        public Filter filterByStatus(FilterUtil.SortType sortType, EmailStatus... statuses) {
            String[] strStatuses = Arrays.stream(statuses).map(s -> s.name())
                    .collect(Collectors.toList()).toArray(new String[statuses.length]);
            this.status = new FilterUtil.MultipleStringValue("status", sortType, strStatuses);
            return this;
        }

        public FilterUtil.SingleStringValue getReferenceKey() {
            return referenceKey;
        }

        public FilterUtil.DateRangeValue getDateCreated() {
            return dateCreated;
        }

        public FilterUtil.MultipleStringValue getEmailGroup() {
            return emailGroup;
        }

        public FilterUtil.MultipleStringValue getStatus() {
            return status;
        }
    }


    public static class Response {

        private final EmailStatus status;
        private final String message;

        @JsonCreator
        public Response(EmailStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public EmailStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum EmailStatus {
        NEW, SCHEDULED, QUEUE, DELIVERED
    }

    public static class EmailDto {

        private final String group;
        private final String referenceKey;

        private final Address from;
        private final List<Address> to;

        private List<Address> cc;

        private final String subject;

        private final String bodyText;
        private String bodyHtml;

        private List<Attachment> attachments;

        private EmailStatus status = EmailStatus.NEW;

        @JsonCreator
        public EmailDto(String group, String referenceKey,
                        Address from, List<Address> to, String subject, String bodyText) {
            this.group = group;
            this.referenceKey = referenceKey;
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.bodyText = bodyText;
        }

        public EmailDto(EmailTrxMdl db) {

            this.group = db.getEmailGroup();
            this.referenceKey = db.getReferenceKey();

            this.from = Address.fromString(db.getFromAddr());

            String[] addrWithNames = StringUtils.split(db.getToAddrs(), ",");
            List<Address> addresses = new ArrayList<>();
            for(String a: addrWithNames) {
                addresses.add(Address.fromString(a));
            }
            this.to = addresses;

            this.subject = db.getEmailSubject();

            this.bodyText = db.getBodyText();

            this.status = db.getStatus();
        }

        public List<String> getConcatenatedToAddress() {
           return to.stream().map(e -> e.toString()).collect(Collectors.toList());
        }

        public Address getFrom() {
            return from;
        }

        public List<Address> getTo() {
            return to;
        }

        public List<Address> getCc() {
            return cc;
        }

        public void setCc(List<Address> cc) {
            this.cc = cc;
        }

        public String getSubject() {
            return subject;
        }

        public String getBodyText() {
            return bodyText;
        }

        public String getBodyHtml() {
            return bodyHtml;
        }

        public void setBodyHtml(String bodyHtml) {
            this.bodyHtml = bodyHtml;
        }

        public EmailStatus getStatus() {
            return status;
        }

        public void setStatus(EmailStatus status) {
            this.status = status;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public String getGroup() {
            return group;
        }

        public String getReferenceKey() {
            return referenceKey;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("from", from)
                    .append("to", StringUtils.join(to, ","))
                    .append("subject", subject)
                    .toString();
        }
    }

    public static class Attachment {

        private final String name;
        private final String cid;
        private final File file;

        @JsonCreator
        public Attachment(String name, String cid, File file) {
            this.name = name;
            this.cid = cid;
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public String getCid() {
            return cid;
        }

        public File getFile() {
            return file;
        }
    }

    public static class Address {

        private final String address;
        private String name;

        @JsonCreator
        public Address(String address) {
            this.address = address;
        }

        @JsonCreator
        public Address(String address, String name) {
            this.address = address;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {

            if(StringUtils.isNotBlank(name)) {

                return name + " <" + address + ">";
            } else {

                return address;
            }
        }

        public static Address fromString(String addressWithname) {

            if(StringUtils.isBlank(addressWithname)) {
                return null;
            }

            int lt = StringUtils.indexOf(addressWithname, "<");
            if(lt > -1 && StringUtils.endsWith(addressWithname, ">")) {
                return new Address(StringUtils.substringBetween(addressWithname, "<", ">"),
                        StringUtils.trim(StringUtils.left(addressWithname, lt)));
            }

            return new Address(addressWithname);
        }
    }
}
