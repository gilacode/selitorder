package com.avicenna.security.apis;

import com.avicenna.security.SecMgr;
import com.google.inject.AbstractModule;

public class ApisModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SecMgr.class).to(ApisMgr.class);
    }
}
