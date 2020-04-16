package com.avicenna.email.smtp;

import com.avicenna.config.AppCfgMgr;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.commons.lang3.builder.ToStringBuilder;
import play.Logger;
import play.api.libs.mailer.SMTPConfiguration;
import scala.Option;

import java.util.Properties;

public class SmtpCfgProvider implements Provider<SMTPConfiguration> {

    private final AppCfgMgr appCfgMgr;

    @Inject SmtpCfgProvider(AppCfgMgr appCfgMgr) {
        this.appCfgMgr = appCfgMgr;
    }

    @Override
    public SMTPConfiguration get() {

        SMTPConfiguration conf = new SMTPConfiguration(
                appCfgMgr.getString("play.mailer.host"),
                Integer.valueOf(appCfgMgr.getString("play.mailer.port")),
                parseString(appCfgMgr.getString("play.mailer.ssl")),
                parseString(appCfgMgr.getString("play.mailer.tls")),
                parseString(appCfgMgr.getString("play.mailer.tlsRequired")),
                Option.apply(appCfgMgr.getString("play.mailer.user")),
                Option.apply(appCfgMgr.getString("play.mailer.password")),
                parseString(appCfgMgr.getString("play.mailer.debug")),
                Option.apply(Integer.valueOf(appCfgMgr.getString("play.mailer.timeout"))),
                Option.apply(Integer.valueOf(appCfgMgr.getString("play.mailer.connectionTimeout"))),
                parseString(appCfgMgr.getString("play.mailer.mock"))
        );

        Logger.debug("SMTP Configuration : " + ToStringBuilder.reflectionToString(conf));

        return conf;
    }

    public boolean parseString(String yesOrNo) {
        if("yes".equals(yesOrNo)) {
            return true;
        }
        return false;
    }

}
