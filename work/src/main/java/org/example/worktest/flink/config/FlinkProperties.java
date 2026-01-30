package org.example.worktest.flink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "flink")
public class FlinkProperties {

    private Rest rest = new Rest();
    private Storage storage = new Storage();
    private Job job = new Job();
    private Client client = new Client();

    public static class Rest {
        private String address = "localhost";
        private int port = 8081;
        private String clusterId = "standalone";

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getClusterId() { return clusterId; }
        public void setClusterId(String clusterId) { this.clusterId = clusterId; }
    }

    public static class Storage {
        private String jobIdDir = "/var/data/flink/job-id-latest/";
        private String savepointDir = "/var/data/flink/savepoint-latest/";

        public String getJobIdDir() { return jobIdDir; }
        public void setJobIdDir(String jobIdDir) { this.jobIdDir = jobIdDir; }

        public String getSavepointDir() { return savepointDir; }
        public void setSavepointDir(String savepointDir) { this.savepointDir = savepointDir; }
    }

    public static class Job {
        private int defaultParallelism = 1;
        private boolean allowNonRestoredState = true;
        private int maxRestoreAttempts = 3;

        public int getDefaultParallelism() { return defaultParallelism; }
        public void setDefaultParallelism(int defaultParallelism) { this.defaultParallelism = defaultParallelism; }

        public boolean isAllowNonRestoredState() { return allowNonRestoredState; }
        public void setAllowNonRestoredState(boolean allowNonRestoredState) { this.allowNonRestoredState = allowNonRestoredState; }

        public int getMaxRestoreAttempts() { return maxRestoreAttempts; }
        public void setMaxRestoreAttempts(int maxRestoreAttempts) { this.maxRestoreAttempts = maxRestoreAttempts; }
    }

    public static class Client {
        private int connectionTimeout = 30000;
        private int requestTimeout = 60000;
        private int retryCount = 3;
        private int retryDelay = 1000;

        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public int getRequestTimeout() { return requestTimeout; }
        public void setRequestTimeout(int requestTimeout) { this.requestTimeout = requestTimeout; }

        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

        public int getRetryDelay() { return retryDelay; }
        public void setRetryDelay(int retryDelay) { this.retryDelay = retryDelay; }
    }

    public Rest getRest() { return rest; }
    public void setRest(Rest rest) { this.rest = rest; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}