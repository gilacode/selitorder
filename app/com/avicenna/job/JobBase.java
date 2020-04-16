package com.avicenna.job;

import com.avicenna.logger.LogMgr;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import play.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JobBase {

    private final LogMgr logger;
    private final JobMstMdl.JobMstProvider schJobProvider;

    public JobBase(LogMgr logger, JobMstMdl.JobMstProvider schJobProvider) {
        this.logger = logger;
        this.schJobProvider = schJobProvider;
    }

    public void start(final String batchId, JobMgr.JobDto job) {

        final long startTime = System.currentTimeMillis();

        JobResult result = null;
        try {

            result = process(batchId);

        } catch (Throwable t) {

            Logger.error(this.getClass().getSimpleName(), t);

        } finally {

            long elapsed = 0;
            if (startTime > 0) {
                elapsed = System.currentTimeMillis() - startTime;
            }


            if(result!=null && result.getProcessed() > 0) {

                JobTrxMdl schTrx = new JobTrxMdl();

                schTrx.setBatchId(batchId);

                JobMstMdl db = schJobProvider.get().query().where().eq("jobName", job.getJobName()).findOne();
                schTrx.setJob(db);

                schTrx.setElapsed(elapsed);

                schTrx.setProcessed(result.getProcessed());
                schTrx.setSuccess(result.getSuccessful());

                schTrx.insert();

                for(Message msg : result.getMessages()) {

                    if(msg.isError()) {
                        logger.error(job.getGroupName(), batchId, msg.getMessage());
                    } else {
                        logger.info(job.getGroupName(), batchId, msg.getMessage());
                    }

                }
            }

        }
    }

    protected abstract JobResult process(final String batchId);

    public static class JobResult {

        private final String group;
        private final String referenceKey;

        private int processed = 0;
        private int successful = 0;

        private final List<Message> messages = new ArrayList<>();

        @JsonCreator
        public JobResult(String group, String referenceKey) {
            this.group = group;
            this.referenceKey = referenceKey;
        }

        public void processed() {
            this.processed++;
        }

        public void success() {
            this.successful++;
        }

        public void log(String message) {
            this.messages.add(new Message(message));
        }

        public void error(String message) {
            this.messages.add(new Message(message, true));
        }

        public void log(Throwable t) {
            String errMsg = ExceptionUtils.getStackTrace(t);
            this.messages.add(new Message(errMsg, true));
        }

        public String getGroup() {
            return group;
        }

        public String getReferenceKey() {
            return referenceKey;
        }

        public int getProcessed() {
            return processed;
        }

        public int getSuccessful() {
            return successful;
        }

        public List<Message> getMessages() {
            return Collections.unmodifiableList(messages);
        }
    }

    public static class Message {

        private final String message;
        private final boolean error;

        public Message(String message) {
            this.message = message;
            this.error = false;
        }

        public Message(String message, boolean error) {
            this.message = message;
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public boolean isError() {
            return error;
        }
    }
}
