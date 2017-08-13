package org.yggd.spring.daemonwithstate.receiver;

public class ReceiveMessage {

    private RequestState state;
    private String jobName;
    private String jobParameters;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }
}
