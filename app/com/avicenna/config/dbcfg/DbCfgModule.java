package com.avicenna.config.dbcfg;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.config.dbcfg.DbCfgMdl;
import com.avicenna.config.dbcfg.DbCfgMgr;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.ebean.Finder;

public class DbCfgModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(AppCfgMgr.class).to(DbCfgMgr.class);
        bind(new TypeLiteral<Finder<Long, DbCfgMdl>>(){}).toProvider(DbCfgMdl.DbCfgProvider.class);

    }
}
