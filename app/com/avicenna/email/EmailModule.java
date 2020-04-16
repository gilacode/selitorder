package com.avicenna.email;

import com.avicenna.email.smtp.SmtpCfgProvider;
import com.avicenna.email.smtp.SmtpMgr;
import com.avicenna.job.JobReg;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import play.api.libs.mailer.SMTPConfiguration;

public class EmailModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<JobReg> uriJobRegBinder = Multibinder.newSetBinder(binder(), JobReg.class);
        uriJobRegBinder.addBinding().to(EmailJobReg.class);

        // SMTP
        bind(SMTPConfiguration.class).toProvider(SmtpCfgProvider.class);
        bind(EmailMgr.class).to(SmtpMgr.class);

        // Dummy
        //bind(EmailMgr.class).to(DummyMgr.class);

        bind(EmailJob.class).toProvider(EmailJobProvider.class);
    }
}
