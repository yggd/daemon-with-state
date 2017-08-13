package org.yggd.spring.daemonwithstate.repository;

import java.util.Date;

public class BatchJobRequest {

    private long jobSequenceId;
    private String jobName;
    private String jobParameter;
    private long jobExecutionId;
    private String pollingStatus;
    private Date createDate;
    private Date updateDate;

    public long getJobSequenceId() {
        return jobSequenceId;
    }

    public void setJobSequenceId(long jobSequenceId) {
        this.jobSequenceId = jobSequenceId;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public void setJobParameter(String jobParameter) {
        this.jobParameter = jobParameter;
    }

    public String getPollingStatus() {
        return pollingStatus;
    }

    public void setPollingStatus(String pollingStatus) {
        this.pollingStatus = pollingStatus;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }
}
