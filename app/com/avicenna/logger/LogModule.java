package com.avicenna.logger;

import com.avicenna.logger.dblog.DbLogMgr;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class LogModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {

        // dblog
        bind(LogMgr.class).to(DbLogMgr.class);
        bindActor(DbLogMgr.LogActor.class, "dblog");
    }
}
