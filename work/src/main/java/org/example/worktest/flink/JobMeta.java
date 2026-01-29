package org.example.worktest.flink;

public class JobMeta {
    private final String jobName;
    private final String jobId;
    private final String savepoint;

    public JobMeta(String jobName, String jobId, String savepoint) {
        this.jobName = jobName;
        this.jobId = jobId;
        this.savepoint = savepoint;
    }

    public String getJobName() { return jobName; }
    public String getJobId() { return jobId; }
    public String getSavepoint() { return savepoint; }
}

