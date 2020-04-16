package com.avicenna.email;

import com.avicenna.logger.LogMgr;
import com.avicenna.job.JobBase;
import com.avicenna.job.JobMstMdl;
import com.avicenna.util.FilterUtil;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.util.List;

public class EmailJob extends JobBase {

    private EmailTrxMdl.EmailTrxProvider emailTrxProv;

    private final EmailMgr emailMgr;

    EmailJob(LogMgr logger, EmailTrxMdl.EmailTrxProvider emailTrxProv, JobMstMdl.JobMstProvider schJobProvider, EmailMgr emailMgr) {

        super(logger, schJobProvider);

        this.emailTrxProv = emailTrxProv;

        this.emailMgr = emailMgr;
    }

    @Override
    protected JobResult process(final String batchId) {

        JobResult jobResult = new JobResult("Email", batchId);

        List<EmailMgr.EmailDto> newEmails = ListUtils.emptyIfNull(emailMgr.getEmails(
                new EmailMgr.Filter(100).filterByStatus(FilterUtil.SortType.asc, EmailMgr.EmailStatus.NEW)));

        for (EmailMgr.EmailDto emailDto : newEmails) {

            jobResult.processed();

            jobResult.log("Sending email to "+StringUtils.join(emailDto.getTo()) + " / reference key : " + emailDto.getReferenceKey());

            try {

                boolean result = emailMgr.deliverEmail(emailDto);

                if (result) {

                    EmailTrxMdl db = emailTrxProv.get().query().where()
                            .eq("emailGroup", emailDto.getGroup())
                            .eq("referenceKey", emailDto.getReferenceKey())
                            .findOne();
                    if (db != null) {
                        db.setStatus(EmailMgr.EmailStatus.DELIVERED);
                        db.update();
                    }

                    jobResult.success();

                    jobResult.log("Email sucessfully delivered to "+StringUtils.join(emailDto.getTo()) + " / reference key : " + emailDto.getReferenceKey());
                } else {
                    jobResult.error("Email failed to be delivered to "+StringUtils.join(emailDto.getTo()) + " / reference key : " + emailDto.getReferenceKey());
                }

            } catch (Throwable t) {
                Logger.error(this.getClass().getSimpleName(), t);
                jobResult.log(t);
            }
        }

        return jobResult;
    }
}
