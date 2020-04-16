package com.avicenna.nav;

import com.google.inject.AbstractModule;

public class NavModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(NavMgr.class).asEagerSingleton();

    }
}
