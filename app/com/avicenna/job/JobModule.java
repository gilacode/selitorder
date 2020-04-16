package com.avicenna.job;

import com.avicenna.nav.NavRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import play.libs.akka.AkkaGuiceSupport;

public class JobModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {

        Multibinder<NavRegistry> uriBinder = Multibinder.newSetBinder(binder(), NavRegistry.class);
        uriBinder.addBinding().to(JobNav.class);

        bind(JobMgr.class).asEagerSingleton();

    }
}
