package com.avicenna.audit;

import com.avicenna.nav.NavMgr;
import com.google.inject.AbstractModule;

public class AuditModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(AuditMgr.class).asEagerSingleton();

    }
}
