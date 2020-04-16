package com.avicenna.util;

import com.google.inject.AbstractModule;

public class UtilModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(DateTimeUtil.class).asEagerSingleton();
        requestStaticInjection(DateTimeStatic.class);

        bind(IdUtil.class).asEagerSingleton();

        bind(LangUtil.class).asEagerSingleton();

        bind(NotifPrv.Builder.class).toProvider(NotifPrv.class);

        bind(MimeTypeUtil.class).asEagerSingleton();
    }
}
