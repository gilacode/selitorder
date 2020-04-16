package com.avicenna.template;

import com.google.inject.AbstractModule;

public class TmplModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TmplMgr.class);
    }
}
