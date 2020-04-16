package com.avicenna.uqcode;

import com.avicenna.util.LangUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Logger;
import play.i18n.Lang;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

@Singleton
public class UQCodeMgr {

    private final LangUtil langUtil;

    private final UQCodeMdl.UqCodeProvider uqCodePrv;

    @Inject UQCodeMgr(LangUtil langUtil, UQCodeMdl.UqCodeProvider uqCodePrv) {

        this.langUtil = langUtil;

        this.uqCodePrv = uqCodePrv;
    }

    private char[] ch = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public synchronized String getUniqueCode(int length) throws UQException {
        return getUniqueCode(length, null);
    }

    public String findReferenceKey(String uqCode) {
        UQCodeMdl db = uqCodePrv.get().query().where().eq("uqCode", uqCode).findOne();
        if(db!=null) {
            return db.getReferenceKey();
        }
        return null;
    }

    public void removeUniqueCode(String uqCode) {
        UQCodeMdl db = uqCodePrv.get().query().where().eq("uqCode", uqCode).findOne();
        if(db!=null) {
            db.deletePermanent();
        }
    }

    public synchronized String getUniqueCode(int length, String uniqueRefKey) throws UQException {

        long startTime = System.currentTimeMillis();
        char[] c = new char[length];
        Random random = new Random();

        do {
            for(int i = 0; i < length; ++i) {
                c[i] = ch[random.nextInt(ch.length)];
            }

            String generatedCode = new String(c);
            int countCode = uqCodePrv.get().query().where().eq("uqCode", generatedCode).findCount();

            if (countCode == 0) {

                long endTime = System.currentTimeMillis();
                Logger.debug("Get code duration is " + (endTime - startTime) + " millis");

                try {
                    UQCodeMdl db = new UQCodeMdl();
                    db.setUqCode(generatedCode);
                    if(uniqueRefKey!=null) {
                        db.setReferenceKey(uniqueRefKey);
                    }
                    db.insert();
                } catch (Exception e) {
                    throw new UQException(langUtil.at("uqcode.error.generate.insertrow"), e);
                }

                return generatedCode;
            }
        } while(System.currentTimeMillis() - startTime < 5000L);

        throw new UQException(langUtil.at("uqcode.error.generate.toolong"));
    }
}
