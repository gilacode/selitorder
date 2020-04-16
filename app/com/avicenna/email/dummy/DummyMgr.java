package com.avicenna.email.dummy;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.email.EmailMgr;
import com.avicenna.email.EmailTrxMdl;
import com.avicenna.logger.LogMgr;
import com.avicenna.util.DateTimeUtil;
import com.google.inject.Inject;
import play.Logger;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

public class DummyMgr extends EmailMgr {

    @Inject
    public DummyMgr(DateTimeUtil dateTimeUtil, AppCfgMgr appCfg, LogMgr dbLog, EmailTrxMdl.EmailTrxProvider emailTrxProv) {
        super(dateTimeUtil, appCfg, dbLog, emailTrxProv);
    }

    @Override
    protected boolean deliverEmail(EmailDto emailDto) {
        return true;
    }
}
