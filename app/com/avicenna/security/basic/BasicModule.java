package com.avicenna.security.basic;

import com.avicenna.apiclient.ApiClientNav;
import com.avicenna.nav.NavRegistry;
import com.avicenna.security.SecMgr;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class BasicModule extends AbstractModule {
    @Override
    protected void configure() {

        Multibinder<NavRegistry> uriBinder = Multibinder.newSetBinder(binder(), NavRegistry.class);
        uriBinder.addBinding().to(BasicNav.class);

        bind(SecMgr.class).to(BasicMgr.class);

    }
}
