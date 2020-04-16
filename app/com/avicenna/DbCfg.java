package com.avicenna;

import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.event.ServerConfigStartup;

public class DbCfg implements ServerConfigStartup {

    @Override
    public void onStart(ServerConfig serverConfig) {
        DatabasePlatform platform = new MySqlPlatform();
        serverConfig.setDatabasePlatform(platform);
        serverConfig.setDdlGenerate(true);
    }
}
