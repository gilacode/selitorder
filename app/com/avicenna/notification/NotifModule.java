package com.avicenna.notification;

import com.google.inject.AbstractModule;

public class NotifModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(NotifMgr.class).asEagerSingleton();

    }
}
