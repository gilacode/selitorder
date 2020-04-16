package com.avicenna.config;

import com.avicenna.nav.NavRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class AppCfgModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<NavRegistry> uriBinder = Multibinder.newSetBinder(binder(), NavRegistry.class);
        uriBinder.addBinding().to(AppCfgNav.class);

    }
}
