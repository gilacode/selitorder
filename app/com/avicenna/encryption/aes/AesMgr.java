package com.avicenna.encryption.aes;

import com.avicenna.encryption.EncException;
import com.avicenna.encryption.EncMgr;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class AesMgr extends EncMgr {

    private final SecretKeySpec secretKey;
    private final byte[] key;

    @Inject AesMgr(Config config) throws EncException {
        super(config);

        try {

            byte[] saltkey = salt.getBytes("UTF-8");

            MessageDigest sha = MessageDigest.getInstance("SHA-1");

            byte[] shaKey = sha.digest(saltkey);

            this.key = Arrays.copyOf(shaKey, 16);
            this.secretKey = new SecretKeySpec(key, "AES");

        } catch (NoSuchAlgorithmException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new EncException("Error while instantiating AES manager", e);
        } catch (UnsupportedEncodingException e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new EncException("Error while instantiating AES manager", e);
        }
    }

    @Override
    public String encrypt(String strToEncrypt) throws EncException {

        try {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));

        } catch (Exception e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new EncException("Error while performing encryption", e);
        }
    }

    @Override
    public String decrypt(String strToDecrypt) throws EncException {

        try {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));

        } catch (Exception e) {
            Logger.error(this.getClass().getSimpleName(), e);
            throw new EncException("Error while performing decryption", e);
        }
    }
}
