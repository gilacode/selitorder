package com.avicenna.owner;

import com.google.inject.AbstractModule;

public class OwnerModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(OwnerMgr.class).asEagerSingleton();

    }
}
