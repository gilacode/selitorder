package com.avicenna.job;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import com.avicenna.logger.LogMgr;
import com.avicenna.util.DateTimeUtil;
import com.avicenna.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Singleton;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class JobMgr {

    private final ActorSystem actorSystem;
    private final Map<String, Cancellable> schedulers = new HashMap<>();
    private final ExecutionContext executionContext;

    private final DateTimeUtil dateTimeUtil;
    private final IdUtil idUtil;

    private final LogMgr logMgr;

    private final Set<JobReg> jobRegs;
    private final JobTrxMdl.JobTrxProvider schTrxProvider;

    @Inject
    JobMgr(ActorSystem actorSystem,
           ExecutionContext executionContext,
           DateTimeUtil dateTimeUtil,
           IdUtil idUtil,
           LogMgr logMgr,
           Set<JobReg> jobRegs,
           JobMstMdl.JobMstProvider schJobProvider,
           JobTrxMdl.JobTrxProvider schTrxProvider) {

        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.dateTimeUtil = dateTimeUtil;
        this.idUtil = idUtil;

        this.logMgr = logMgr;

        this.jobRegs = jobRegs;
        this.schTrxProvider = schTrxProvider;

        for(JobReg jobReg : SetUtils.emptyIfNull(jobRegs)) {

            JobMstMdl db = schJobProvider.get().query().where().eq("jobName", jobReg.getJobDto().getJobName()).findOne();
            if(db==null) {
                db = new JobMstMdl();
                db.setJobName(jobReg.getJobDto().getJobName());
                db.setJobDesc(jobReg.getJobDto().getJobDesc());
                db.setGroupName(jobReg.getJobDto().getGroupName());
                db.setGroupDesc(jobReg.getJobDto().getGroupDesc());
                db.setJobInterval(jobReg.getJobDto().getJobCfg().getInterval());
                db.setTriggeredAt(jobReg.getJobDto().getJobCfg().getTriggeredTime());
                db.insert();
            }

            jobReg.setJobDto(new JobDto(db));

            this.schedulers.put(jobReg.getJobDto().getJobName(), this.actorSystem
                    .scheduler()
                    .schedule(
                            jobReg.getJobDto().getJobCfg().getStartTime(), // initialDelay
                            jobReg.getJobDto().getJobCfg().getInterval().getDuration(), // interval
                            () -> jobReg.getJob().start(idUtil.getShortUniqueId(), jobReg.getJobDto()),
                            this.executionContext));
        }

    }

    public String executeJob(String jobName) throws JobException {

        final String batchId = dateTimeUtil.getReverseDate(new Date()) + "_" + idUtil.getShortUniqueId();

        final JobReg jobReg = jobRegs.stream()
                .filter(j -> j.getJobDto().getJobName().equals(jobName)).findFirst().orElse(null);

        if(jobReg!=null) {
            this.actorSystem
                    .scheduler()
                    .scheduleOnce(
                            Duration.create(1, TimeUnit.SECONDS), // initialDe
                            () -> jobReg.getJob().start(idUtil.getShortUniqueId(), jobReg.getJobDto()),
                            this.executionContext);
        }

        return batchId;
    }

    public Response stopJob(String jobName) throws JobException {

        Cancellable jobToCancel = schedulers.get(jobName);

        if(jobToCancel==null && jobToCancel.isCancelled()) {
            return new Response(false, "Fail to stop job. Job is either not exist in registry or already stopped");
        }

        jobToCancel.cancel();

        return new Response(true, "Job "+jobName+ " stopped");
    }

    public Response startJob(String jobName) throws JobException {

        Cancellable jobToCancel = schedulers.get(jobName);

        if(jobToCancel!=null && !jobToCancel.isCancelled()) {
            return new Response(false, "Failed to start job. Another instance of the same job is still active");
        }

        JobReg jobReg = this.jobRegs.stream().filter(j -> j.getJobDto().getJobName().equals(jobName)).findFirst().orElse(null);

        if(jobReg==null) {
            return new Response(false, "Failed to start job. Job not found in registry");
        }

        this.schedulers.put(jobReg.getJobDto().getJobName(), this.actorSystem
                .scheduler()
                .schedule(
                        jobReg.getJobDto().getJobCfg().getStartTime(), // initialDelay
                        jobReg.getJobDto().getJobCfg().getInterval().getDuration(), // interval
                        () -> jobReg.getJob().start(idUtil.getShortUniqueId(), jobReg.getJobDto()),
                        this.executionContext));

        return new Response(true, "Job "+jobName+ " started");
    }

    public List<JobDto> getJobs() {
        List<JobDto> jobs = ListUtils.emptyIfNull(jobRegs.stream().map(j -> j.getJobDto()).collect(Collectors.toList()));

        for(JobDto job : jobs) {
            Cancellable status = schedulers.get(job.getJobName());
            if(status!=null || !status.isCancelled()) {
                job.setRunning(true);
            } else {
                job.setRunning(false);
            }
        }

        return jobs;
    }

    public JobDto findJob(String jobName) {
        JobDto job = SetUtils.emptyIfNull(jobRegs).stream()
                .map(j -> j.getJobDto())
                .filter(j -> j.getJobName().equals(jobName)).findFirst().orElse(null);

        if(job!=null) {
            Cancellable status = schedulers.get(jobName);
            if(status!=null || !status.isCancelled()) {
                job.setRunning(true);
            } else {
                job.setRunning(false);
            }
        }

        return job;
    }

    public List<BatchDto> getBatches(String jobName, Date startDate, Date endDate) {

        final List<BatchDto> batchLogs = new ArrayList<>();

        final JobDto jobReg = SetUtils.emptyIfNull(jobRegs).stream()
                .map(j -> j.getJobDto())
                .filter(j -> j.getJobName().equals(jobName)).findFirst().orElse(null);

        if(jobReg!=null) {

            final Date qStartDate = new DateTime(startDate).withTimeAtStartOfDay().toDate();
            final Date qEndDate = new DateTime(endDate).withTime(23, 59, 59, 999).toDate();

            final List<JobTrxMdl> dbs = ListUtils.emptyIfNull(schTrxProvider.get().query().fetch("job").order("createdAt desc")
                    .where().eq("job.jobName", jobName).between("createdAt", qStartDate, qEndDate)
                    .findList());

            for (JobTrxMdl db : dbs) {

                final BatchDto batch = findBatch(db.getBatchId());
                batchLogs.add(batch);

            }
        }

        return batchLogs;
    }

    public static class JobDto {

        private final String jobName;
        private final String jobDesc;
        private final JobCfg jobCfg;
        private final String groupName;
        private final String groupDesc;

        private String startDate;
        private String endDate;
        private final List<BatchDto> batches = new ArrayList<>();

        private boolean running = true;

        public JobDto(JobMstMdl db) {
            this.jobName = db.getJobName();
            this.jobDesc = db.getJobDesc();
            this.jobCfg = new JobCfg(db.getJobInterval(), db.getTriggeredAt());
            this.groupName = db.getGroupName();
            this.groupDesc = db.getGroupDesc();
        }

        public JobDto(String jobName, String jobDesc, JobCfg jobCfg, String groupName, String groupDesc) {
            this.jobName = jobName;
            this.jobDesc = jobDesc;
            this.jobCfg = jobCfg;
            this.groupName = groupName;
            this.groupDesc = groupDesc;
        }

        public String getJobName() {
            return jobName;
        }

        public String getJobDesc() {
            return jobDesc;
        }

        public JobCfg getJobCfg() {
            return jobCfg;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getGroupDesc() {
            return groupDesc;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public List<BatchDto> getBatches() {
            return batches;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }

    public BatchDto findBatch(String batchId) {

        final JobTrxMdl db = schTrxProvider.get().query().where().eq("batchId", batchId).findOne();

        if(db!=null) {
            String strElapsed = "-";
            long millis = 0;
            if (db.getElapsed() != null && db.getElapsed() > 0) {
                millis = db.getElapsed();
                strElapsed = String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(millis),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                );
            }

            final BatchDto batch = new BatchDto(
                    db.getBatchId(), dateTimeUtil.getDateTime(db.getCreatedAt()),
                    db.getSuccess(), db.getCancelled(), db.getProcessed(),
                    strElapsed);

            return batch;
        }

        return null;
    }

    public static class BatchDto {

        private final String batchId;
        private final String batchDate;
        private final Integer success;
        private final Integer cancelled;
        private final Integer processed;
        private final String elapsed;

        @JsonCreator
        public BatchDto(String batchId, String batchDate, Integer success, Integer cancelled, Integer processed, String elapsed) {
            this.batchId = batchId;
            this.batchDate = batchDate;
            this.success = success;
            this.cancelled = cancelled;
            this.processed = processed;
            this.elapsed = elapsed;
        }

        public String getBatchId() {
            return batchId;
        }

        public String getBatchDate() {
            return batchDate;
        }

        public Integer getSuccess() {
            return success;
        }

        public Integer getCancelled() {
            return cancelled;
        }

        public Integer getProcessed() {
            return processed;
        }

        public String getElapsed() {
            return elapsed;
        }
    }

    public List<LogDto> getLogs(String batchId) {
        return ListUtils.emptyIfNull(logMgr.get(new LogMgr.LogParam().addReferences(batchId)))
                .stream().map(l -> new JobMgr.LogDto(dateTimeUtil.getDate(l.getTimestamp()), l.getType(), l.getMessage()))
                .collect(Collectors.toList());
    }

    public static class LogDto {

        private final String timeStamp;
        private final boolean error;
        private final String log;

        @JsonCreator
        public LogDto(String timeStamp, boolean error, String log) {
            this.timeStamp = timeStamp;
            this.error= error;
            this.log = log;
        }

        public LogDto(String timeStamp, LogMgr.LogType logType, String log) {
            this.timeStamp = timeStamp;
            if(logType.equals(LogMgr.LogType.ERROR)) {
                this.error = true;
            } else {
                this.error = false;
            }
            this.log = log;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public boolean isError() {
            return error;
        }

        public String getLog() {
            return log;
        }
    }

    public static class JobCfg {

        private final JobTriggerDuration interval;
        private final String triggeredTime; // HH:mm:ss

        public JobCfg(JobTriggerDuration interval) {
            this.interval = interval;
            this.triggeredTime = null;
        }

        @JsonCreator
        public JobCfg(JobTriggerDuration interval, String triggeredTime) {
            this.interval = interval;
            this.triggeredTime = triggeredTime;
        }

        public FiniteDuration getStartTime() {

            if(this.triggeredTime!=null) {

                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
                DateTime dtNow = DateTime.now();
                DateTime dtTriggered = fmt.parseDateTime(triggeredTime);

                return Duration.create(Seconds.secondsBetween(dtNow, dtTriggered).getSeconds(), TimeUnit.SECONDS);

            } else  {

                return JobTriggerDuration.EVERY_15_SEC.getDuration();

            }
        }

        public String getDesc() {

            StringBuilder str = new StringBuilder();

            str.append(this.interval.getDesc());

            if(triggeredTime!=null) {
                str.append(" at "+triggeredTime);
            }

            return str.toString();
        }

        public JobTriggerDuration getInterval() {
            return interval;
        }

        public String getTriggeredTime() {
            return triggeredTime;
        }
    }

    public static class TimeRange {

        private final String startTime;
        private final String endTime;

        @JsonCreator
        public TimeRange(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        @Override
        public String toString() {
            return startTime + " - " + endTime;
        }
    }

    public enum JobTriggerDuration {

        EVERY_15_SEC("Every 15 seconds", Duration.create(15, TimeUnit.SECONDS)),
        EVERY_1_MIN("Every 1 Minute", Duration.create(60, TimeUnit.SECONDS)),
        EVERY_15_MIN("Every 15 Minutes", Duration.create(900, TimeUnit.SECONDS)),
        EVERY_1_HOUR("Every 1 Hour", Duration.create(3600, TimeUnit.SECONDS)),
        EVERY_6_HOUR("Every 1 Hours", Duration.create(21600, TimeUnit.SECONDS)),
        EVERY_12_HOUR("Every 12 Hours", Duration.create(43200, TimeUnit.SECONDS)),
        DAILY("Every Day at 00:01", Duration.create(86400, TimeUnit.SECONDS)),
        ;

        private final String desc;
        private final FiniteDuration duration;

        JobTriggerDuration(String desc, FiniteDuration duration) {
            this.desc = desc;
            this.duration = duration;
        }

        public String getDesc() {
            return desc;
        }

        public FiniteDuration getDuration() {
            return duration;
        }
    }

    public static class Response {

        private final boolean success;
        private final String message;

        @JsonCreator
        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
