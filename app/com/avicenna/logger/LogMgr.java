package com.avicenna.logger;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class LogMgr {

    public LogMgr() {
    }

    public abstract void debug(String group, String reference, String message);
    public abstract void info(String group, String reference, String message);
    public abstract void warning(String group, String reference, String message);
    public abstract void error(String group, String reference, String message);
    public abstract void error(String group, String reference, Throwable throwable);
    public abstract void error(String group, String reference, String message, Throwable throwable);
    public abstract List<LogDto> get(LogParam param);

    public enum LogType {
        ERROR, WARNING, INFO, DEBUG
    }

    public static class LogDto {

        private final Date timestamp;
        private final LogType type;
        private final String group;
        private final String reference;
        private final String message;

        @JsonCreator
        public LogDto(Date timestamp, LogType type, String group, String reference, String message) {
            this.timestamp = timestamp;
            this.type = type;
            this.group = group;
            this.reference = reference;
            this.message = message;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public LogType getType() {
            return type;
        }

        public String getGroup() {
            return group;
        }

        public String getReference() {
            return reference;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class LogParam {

        private final int offset;
        private final int limit;

        private final List<LogType> logTypes = new ArrayList<>();
        private final List<String> groups = new ArrayList<>();
        private final List<String> references = new ArrayList<>();

        public LogParam(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public LogParam() {
            this.offset = 0;
            this.limit = 0;
        }

        public LogParam addReferences(String... references) {
            this.references.addAll(Arrays.stream(references).collect(Collectors.toList()));
            return this;
        }

        public LogParam addGroups(String... groups) {
            this.groups.addAll(Arrays.stream(groups).collect(Collectors.toList()));
            return this;
        }

        public LogParam addLogTypes(LogType... logTypes) {
            this.logTypes.addAll(Arrays.stream(logTypes).collect(Collectors.toList()));
            return this;
        }

        public int getLimit() {
            return limit;
        }

        public int getOffset() {
            return offset;
        }

        public List<LogType> getLogTypes() {
            return logTypes;
        }

        public List<String> getGroups() {
            return groups;
        }

        public List<String> getReferences() {
            return references;
        }
    }

}
