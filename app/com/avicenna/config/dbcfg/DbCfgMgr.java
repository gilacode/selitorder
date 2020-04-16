package com.avicenna.config.dbcfg;

import com.avicenna.config.AppCfgMgr;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.cache.SyncCacheApi;

import java.util.List;
import java.util.stream.Collectors;

public class DbCfgMgr extends AppCfgMgr {

    private final DbCfgMdl.DbCfgProvider dbCfgProvider;
    private final SyncCacheApi cache;

    @Inject DbCfgMgr(DbCfgMdl.DbCfgProvider dbCfgProvider, SyncCacheApi cache) {

        this.dbCfgProvider = dbCfgProvider;
        this.cache = cache;
    }

    @Override
    public void registerConfig(AppCfgDto appCfgDto) {
        DbCfgMdl db = dbCfgProvider.get().query().where().eq("propertyKey", appCfgDto.getKey()).findOne();
        if(db==null) {
            db = new DbCfgMdl();
        }

        db.setPropertyKey(appCfgDto.getKey());
        if(StringUtils.isBlank(db.getStringValue())) {
            db.setStringValue(appCfgDto.getValue());
        }
        db.setPropertyDesc(appCfgDto.getDesc());
        db.setParentKey(appCfgDto.getParentKey());

        if(db.getId()==null) {
            db.insert();
        } else {
            db.update();
        }
    }

    @Override
    public String getString(String key) {
        if(StringUtils.isBlank(key)) {
            return "";
        }

        final String cacheKey = "APPCONFIG_"+key;
        String cacheValue = this.cache.get(cacheKey);

        if(StringUtils.isNotBlank(cacheValue)) {
            return cacheValue;
        }

        // try to load from database
        DbCfgMdl db = dbCfgProvider.get().query().where().eq("propertyKey", key).findOne();
        if(db!=null && StringUtils.isNotBlank(db.getStringValue())) {

            this.cache.set(cacheKey, db.getStringValue());
            return db.getStringValue();
        }

        return null;
    }

    @Override
    public AppCfgDto findProperty(String key) {

        if(StringUtils.isBlank(key)) {
            return null;
        }

        DbCfgMdl db = dbCfgProvider.get().query().where().eq("propertyKey", key).findOne();
        if(db!=null) {
            return new AppCfgDto(db.getPropertyKey(), db.getStringValue(), db.getAdditionalProperties(), db.getPropertyDesc(), db.getParentKey(), db.isDeletable());
        }

        return null;
    }

    @Override
    public List<AppCfgDto> getProperties(String groupName) {

        List<DbCfgMdl> cfgs = ListUtils.emptyIfNull(dbCfgProvider.get().query().where().eq("parentKey", groupName).findList());

        return cfgs.stream().map(c -> {
            return new AppCfgDto(c.getPropertyKey(), c.getStringValue(), c.getAdditionalProperties(), c.getPropertyDesc(), c.getParentKey(), c.isDeletable());
        }).collect(Collectors.toList());
    }

    @Override
    public void updateProperty(String key, AppCfgDto prop) {

        if(StringUtils.isBlank(key)) {
            return;
        }

        DbCfgMdl db = dbCfgProvider.get().query().where().eq("propertyKey", key).findOne();
        if(db==null) {
            db = new DbCfgMdl();
        }

        db.setPropertyKey(key);
        db.setStringValue(prop.getValue());
        db.setParentKey(prop.getParentKey());
        db.setAdditionalProperties(prop.getAdditionalProperties());

        if(db.getId()!=null) {
            db.update();
        } else {
            db.insert();
        }
    }

    @Override
    public void clearCache(String groupName) {
        List<DbCfgMdl> dbs = ListUtils.emptyIfNull(dbCfgProvider.get().all());

        for(DbCfgMdl db: dbs) {
            final String cacheKey = "APPCONFIG_"+db.getPropertyKey();
            this.cache.remove(cacheKey);
        }
    }

    @Override
    public List<String> getGroups() {
        List<DbCfgMdl> cfgs = ListUtils.emptyIfNull(dbCfgProvider.get().all());

        return cfgs.stream().map(c -> c.getParentKey()).distinct().sorted().collect(Collectors.toList());
    }

    @Override
    public AppCfgGrpDto getGroup(String groupName) {

        List<DbCfgMdl> cfgs = ListUtils.emptyIfNull(dbCfgProvider.get().query().where().eq("parentKey", groupName).findList());

        AppCfgGrpDto grpDto = new AppCfgGrpDto(groupName, getGroups());

        List<AppCfgDto> dtos = cfgs.stream().map(c -> {
            return new AppCfgDto(c.getPropertyKey(), c.getStringValue(), c.getAdditionalProperties(), c.getPropertyDesc(), c.getParentKey(), c.isDeletable());
        }).collect(Collectors.toList());

        grpDto.getProperties().addAll(dtos);

        return grpDto;
    }

    @Override
    public void updateGroup(AppCfgGrpDto group) {

        if(group!=null && StringUtils.isNotBlank(group.getName())) {

            List<AppCfgDto> dtos = ListUtils.emptyIfNull(group.getProperties());

            dtos.forEach(d -> {

                DbCfgMdl db = dbCfgProvider.get().query().where().eq("propertyKey", d.getKey()).findOne();
                if(db!=null && StringUtils.isNotBlank(d.getValue())) {

                    db.setStringValue(d.getValue());
                    db.update();

                    final String cacheKey = "APPCONFIG_"+d.getKey();
                    cache.remove(cacheKey);
                }
            });
        }
    }
}
