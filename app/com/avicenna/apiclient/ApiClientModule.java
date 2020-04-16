package com.avicenna.apiclient;

import com.avicenna.nav.NavRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import play.libs.akka.AkkaGuiceSupport;

public class ApiClientModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {

        Multibinder<NavRegistry> uriBinder = Multibinder.newSetBinder(binder(), NavRegistry.class);
        uriBinder.addBinding().to(ApiClientNav.class);

        bind(ApiClientMgr.class).asEagerSingleton();

    }
}
