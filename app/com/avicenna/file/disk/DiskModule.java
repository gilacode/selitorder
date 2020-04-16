package com.avicenna.file.disk;

import com.avicenna.encryption.EncMgr;
import com.avicenna.encryption.aes.AesMgr;
import com.avicenna.file.FileMgr;
import com.google.inject.AbstractModule;

public class DiskModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileMgr.class).to(DiskMgr.class);
    }
}
