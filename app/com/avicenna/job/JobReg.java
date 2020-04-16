package com.avicenna.job;

public abstract class JobReg {

    protected JobMgr.JobDto jobDto;

    public JobReg(JobMgr.JobDto jobDto) {
        this.jobDto = jobDto;
    }

    public JobMgr.JobDto getJobDto() {
        return jobDto;
    }

    public void setJobDto(JobMgr.JobDto jobDto) {
        this.jobDto = jobDto;
    }

    public abstract JobBase getJob();
}
