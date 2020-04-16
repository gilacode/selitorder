package com.avicenna.encryption;

import com.typesafe.config.Config;

public abstract class EncMgr {

    private final Config config;

    protected final String salt;

    public EncMgr(Config config) {

        this.config = config;

        this.salt = this.config.getString("play.http.secret.key");
    }

    public abstract String encrypt(String strToEncrypt) throws EncException;
    public abstract String decrypt(String strToDecrypt) throws EncException;

}
