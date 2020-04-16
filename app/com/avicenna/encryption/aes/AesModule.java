package com.avicenna.encryption.aes;

import com.avicenna.encryption.EncMgr;
import com.google.inject.AbstractModule;

public class AesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(EncMgr.class).to(AesMgr.class);

    }
}
