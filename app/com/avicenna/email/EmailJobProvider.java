package com.avicenna.email;

import com.avicenna.logger.LogMgr;
import com.avicenna.job.JobMstMdl;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class EmailJobProvider implements Provider<EmailJob> {

    private final LogMgr logger;

    private final EmailTrxMdl.EmailTrxProvider emailTrxProv;
    private final JobMstMdl.JobMstProvider schJobProvider;

    private final EmailMgr emailMgr;

    @Inject EmailJobProvider(LogMgr logger, EmailTrxMdl.EmailTrxProvider emailTrxProv, JobMstMdl.JobMstProvider schJobProvider, EmailMgr emailMgr) {

        this.logger = logger;

        this.emailTrxProv = emailTrxProv;
        this.schJobProvider = schJobProvider;

        this.emailMgr = emailMgr;
    }

    @Override
    public EmailJob get() {

        return new EmailJob(logger, emailTrxProv, schJobProvider, emailMgr);

    }
}
