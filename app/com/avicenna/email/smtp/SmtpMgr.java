package com.avicenna.email.smtp;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.email.EmailMgr;
import com.avicenna.email.EmailTrxMdl;
import com.avicenna.logger.LogMgr;
import com.avicenna.util.DateTimeUtil;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.mail.EmailException;
import play.Logger;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import java.util.List;
import java.util.stream.Collectors;

public class SmtpMgr extends EmailMgr {


    private final MailerClient mailerClient;

    @Inject SmtpMgr(DateTimeUtil dateTimeUtil,
                    AppCfgMgr appCfgMgr,
                    LogMgr dbLog,
                    EmailTrxMdl.EmailTrxProvider emailTrxProv,
                    MailerClient mailerClient) {

        super(dateTimeUtil, appCfgMgr, dbLog, emailTrxProv);

        this.mailerClient = mailerClient;

        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.host", "example.com", "SMTP Server Hostname", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.port", "25", "SMTP Server Port No", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.ssl", "no", "Use SSL? yes/no", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.tls", "no", "Use TLS? yes/no", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.tlsRequired", "no", "TLS Required? yes/no", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.user", "user", "Username", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.password", "password", "Password", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.debug", "yes", "Debug? yes/no", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.timeout", "30000", "Timeout (ms)", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.connectionTimeout", "30000", "Connection Timeout (ms)", "Email"));
        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("play.mailer.mock", "no", "Allow Mock? yes/no", "Email"));
    }

    @Override
    protected boolean deliverEmail(EmailDto emailDto) {

        try {
            Logger.debug("Sending " + emailDto.getGroup() + " email. " + emailDto.toString());

            Email email = new Email();

            email = email.setFrom(emailDto.getFrom().toString());
            email = email.setTo(emailDto.getConcatenatedToAddress());
            email = email.setSubject(emailDto.getSubject());
            email = email.setBodyText(emailDto.getBodyText());

            String result = mailerClient.send(email);

            Logger.debug("SMTP Response : " + result);

            return true;
        } catch (Exception e) {
            Logger.error(this.getClass().getSimpleName(), e);
            return false;
        }
    }
}
