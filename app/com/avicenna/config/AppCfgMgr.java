package com.avicenna.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AppCfgMgr {

    public AppCfgMgr() {

    }

    public abstract void registerConfig(AppCfgDto appCfgDto);

    public abstract String getString(String key);
    public abstract AppCfgDto findProperty(String key);
    public abstract List<AppCfgDto> getProperties(String groupName);
    public abstract void updateProperty(String key, AppCfgDto prop);

    public abstract List<String> getGroups();
    public abstract AppCfgGrpDto getGroup(String groupName);
    public abstract void updateGroup(AppCfgGrpDto group);

    public abstract void clearCache(String groupName);

    public static class AppCfgAddProp {

        private List<String> arrayVal;
        private  Map<String, String> propMap;

        public AppCfgAddProp() {
            arrayVal = new ArrayList<>();
            propMap = new HashMap<>();
        }

        @JsonCreator
        public AppCfgAddProp(
                @JsonProperty("arrayVal") List<String> arrayVal,
                @JsonProperty("propMap") Map<String, String> propMap) {
            this.arrayVal = arrayVal;
            this.propMap = propMap;
        }

        public List<String> getArrayVal() {
            return arrayVal;
        }

        public void setArrayVal(List<String> arrayVal) {
            this.arrayVal = arrayVal;
        }

        public Map<String, String> getPropMap() {
            return propMap;
        }

        public void setPropMap(Map<String, String> propMap) {
            this.propMap = propMap;
        }
    }

    public static class AppCfgDto {

        private final String key;
        private final String value;
        private final AppCfgAddProp additionalProperties;
        private final String desc;
        private final String parentKey;
        private final boolean deletable;

        @JsonCreator
        public AppCfgDto(String key, String value, AppCfgAddProp additionalProperties, String desc, String parentKey, boolean deletable) {
            this.key = key;
            this.value = value;
            this.additionalProperties = additionalProperties;
            this.desc = desc;
            this.parentKey = parentKey;
            this.deletable = deletable;
        }

        public AppCfgDto(String key, String value, String parentKey, String desc, boolean deletable) {
            this.key = key;
            this.value = value;
            this.additionalProperties = null;
            this.desc = desc;
            this.parentKey = parentKey;
            this.deletable = deletable;
        }

        public AppCfgDto(String key, String value, String desc, String parentKey) {
            this.key = key;
            this.value = value;
            this.additionalProperties = null;
            this.desc = desc;
            this.parentKey = parentKey;
            this.deletable = false;
        }

        public String getCleanKey() {
            return this.key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public AppCfgAddProp getAdditionalProperties() {
            return additionalProperties;
        }

        public String getParentKey() {
            return parentKey;
        }

        public boolean isDeletable() {
            return deletable;
        }

        public String getDesc() {
            return desc;
        }
    }

    public static class AppCfgGrpDto {

        private final String name;
        private final List<String> groupNames;
        private List<AppCfgDto> properties = new ArrayList<>();

        @JsonCreator
        public AppCfgGrpDto(String name, List<String> groupNames) {
            this.name = name;
            this.groupNames = groupNames;
        }

        public List<String> getGroupNames() {
            return groupNames;
        }

        public String getName() {
            return name;
        }

        public List<AppCfgDto> getProperties() {
            return properties;
        }
    }
}
