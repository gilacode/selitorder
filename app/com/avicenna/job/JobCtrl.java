package com.avicenna.job;

import com.avicenna.BaseCtrl;
import com.avicenna.nav.NavMgr;
import com.avicenna.job.html.JobBatch;
import com.avicenna.job.html.JobList;
import com.avicenna.job.html.JobLog;
import com.avicenna.job.routes;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.NotifPrv;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

public class JobCtrl extends BaseCtrl {

    private final DateTimeUtil dateTimeUtil;
    private final NotifPrv notifPrv;

    private final JobMgr jobMgr;

    @Inject
    JobCtrl(TmplMgr tmplMgr, SecMgr secMgr, JobMgr jobMgr, NavMgr navMgr, DateTimeUtil dateTimeUtil, NotifPrv notifPrv) {
        super(tmplMgr, secMgr, navMgr);

        this.jobMgr = jobMgr;

        this.dateTimeUtil = dateTimeUtil;
        this.notifPrv = notifPrv;
    }

    public Result getJobs() throws SecException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_VIEW_PAGE.name(), currentUser())) {

            TmplDto tmpl = tmplMgr.getTmpl("Jobs", "js/shared/job/job.js", null);
            return ok(JobList.render(jobMgr.getJobs(), tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view batch jobs").flash(ctx());
            return redirect("/");

        }
    }

    public Result getBatches(String jobName) throws JobException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_VIEW_PAGE.name(), currentUser())) {

            JobMgr.JobDto job = this.jobMgr.findJob(jobName);

            Date dtStart = new DateTime().withDayOfMonth(1).withTime(0,0,0,0).toDate();
            Date dtEnd = new DateTime().withTime(23,59,59,999).dayOfMonth().withMaximumValue().toDate();

            job.setStartDate(dateTimeUtil.getDate(dtStart));
            job.setEndDate(dateTimeUtil.getDate(dtEnd));

            List<JobMgr.BatchDto> batches = this.jobMgr.getBatches( jobName, dtStart,  dtEnd);

            TmplDto tmpl = tmplMgr.getTmpl("Batch", "js/shared/job/job.js", null);
            return ok(JobBatch.render(job, batches, tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view batch jobs").flash(ctx());
            return redirect("/");

        }
    }

    public Result getLogs(String jobName, String batchId) throws JobException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_VIEW_PAGE.name(), currentUser())) {

            JobMgr.JobDto job = this.jobMgr.findJob(jobName);

            JobMgr.BatchDto batch = this.jobMgr.findBatch(batchId);

            TmplDto tmpl = tmplMgr.getTmpl("Logs", "js/shared/job/job.js", null);
            return ok(JobLog.render(job, batch, this.jobMgr.getLogs(batchId), tmpl, currentUser()));

        } else {

            notifPrv.get().addError("You do not have permission to view batch jobs").flash(ctx());
            return redirect("/");

        }
    }

    public Result execute(String jobName)  throws JobException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_TRIGGER_JOB.name(), currentUser())) {

            String batchId = jobMgr.executeJob(jobName);

            return redirect(routes.JobCtrl.getLogs(jobName, batchId));

        } else {

            notifPrv.get().addError("You do not have permission to trigger batch jobs").flash(ctx());
            return redirect("/");

        }
    }

    public Result stop(String jobName)  throws JobException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_PAUSE_JOB.name(), currentUser())) {

            JobMgr.Response response = jobMgr.stopJob(jobName);

            if (response.isSuccess()) {
                notifPrv.get().addSuccess(response.getMessage());
            } else {
                notifPrv.get().addError(response.getMessage());
            }

            return redirect(routes.JobCtrl.getBatches(jobName));

        } else {

            notifPrv.get().addError("You do not have permission to stop batch jobs").flash(ctx());
            return redirect("/");

        }
    }

    public Result start(String jobName)  throws JobException {

        if(navMgr.hasPermission(JobNav.Permission.SCH_TRX_TRIGGER_JOB.name(), currentUser())) {

            JobMgr.Response response = jobMgr.startJob(jobName);

            if (response.isSuccess()) {
                notifPrv.get().addSuccess(response.getMessage());
            } else {
                notifPrv.get().addError(response.getMessage());
            }

            return redirect(routes.JobCtrl.getBatches(jobName));

        } else {

            notifPrv.get().addError("You do not have permission to trigger batch jobs").flash(ctx());
            return redirect("/");

        }
    }
}
