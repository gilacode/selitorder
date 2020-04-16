package com.avicenna.util;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilterUtil {

    public enum SortType {

        asc, desc

    }

    public static class SingleStringValue {

        private final String field;
        private final String value;
        private final SortType sortType;

        public SingleStringValue(String field, String value) {
            this.field = field;
            this.value = value;
            this.sortType = SortType.asc;
        }

        @JsonCreator
        public SingleStringValue(String field, String value, SortType sortType) {
            this.field = field;
            this.value = value;
            this.sortType = sortType;
        }

        public String getField() {
            return field;
        }

        public String getValue() {
            return value;
        }

        public SortType getSortType() {
            return sortType;
        }
    }

    public static class MultipleStringValue {

        private final String field;
        private final List<String> values = new ArrayList<>();
        private final SortType sortType;

        public MultipleStringValue(String field, List<String> values) {
            this.field = field;
            this.sortType = SortType.asc;
            this.values.addAll(values);
        }

        @JsonCreator
        public MultipleStringValue(String field, SortType sortType, List<String> values) {
            this.field = field;
            this.sortType = sortType;
            this.values.addAll(values);
        }

        public MultipleStringValue(String field, SortType sortType, String... aryValues) {
            this.field = field;
            this.sortType = sortType;
            if(aryValues!=null && aryValues.length > 0) {
                for(String v : aryValues) {
                    values.add(v);
                }
            }
        }

        public String getField() {
            return field;
        }

        public List<String> getValues() {
            return values;
        }

        public SortType getSortType() {
            return sortType;
        }
    }

    public static class SingleDateValue {

        private final String field;
        private final Date value;
        private final SortType sortType;

        public SingleDateValue(String field, Date value) {
            this.field = field;
            this.value = value;
            this.sortType = SortType.asc;
        }

        @JsonCreator
        public SingleDateValue(String field, Date value, SortType sortType) {
            this.field = field;
            this.value = value;
            this.sortType = sortType;
        }

        public String getField() {
            return field;
        }

        public Date getValue() {
            return value;
        }

        public SortType getSortType() {
            return sortType;
        }
    }

    public static class DateRangeValue {

        private final String field;
        private final Date start;
        private final Date end;
        private final SortType sortType;

        public DateRangeValue(String field, Date start, Date end) {
            this.field = field;
            this.start = start;
            this.end = end;
            this.sortType = SortType.asc;
        }

        @JsonCreator
        public DateRangeValue(String field, Date start, Date end, SortType sortType) {
            this.field = field;
            this.start = start;
            this.end = end;
            this.sortType = sortType;
        }

        public String getField() {
            return field;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public SortType getSortType() {
            return sortType;
        }
    }
}
