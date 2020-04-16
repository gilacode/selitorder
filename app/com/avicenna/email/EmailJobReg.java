package com.avicenna.email;

import com.avicenna.job.JobMgr;
import com.avicenna.job.JobBase;
import com.avicenna.job.JobReg;
import com.google.inject.Inject;

public class EmailJobReg extends JobReg {

    private final EmailJobProvider prov;

    @Inject EmailJobReg(EmailJobProvider prov) {

        super(new JobMgr.JobDto(
                "Email Delivery",
                "Batch job to deliver email",
                new JobMgr.JobCfg(JobMgr.JobTriggerDuration.EVERY_15_SEC),
                "Email",
                "Email communication module"));

        this.prov = prov;
    }

    @Override
    public JobBase getJob() {
        return prov.get();
    }
}
