package com.avicenna.security.noop;

import com.avicenna.security.SecMgr;
import com.google.inject.AbstractModule;

public class NoopModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SecMgr.class).to(NoopMgr.class);
    }
}
