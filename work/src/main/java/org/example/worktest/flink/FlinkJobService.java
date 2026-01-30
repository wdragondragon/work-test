package org.example.worktest.flink;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.runtime.messages.FlinkJobNotFoundException;
import org.example.worktest.flink.config.FlinkProperties;
import org.example.worktest.flink.exception.FlinkJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutionException;

@Service
public class FlinkJobService {

    private final Logger log = LoggerFactory.getLogger(FlinkJobService.class);

    private final FlinkProperties flinkProperties;
    private final JobMetaRepository repository;
    private Configuration defaultConfig;
    private final String clusterId;

    @Autowired
    public FlinkJobService(FlinkProperties flinkProperties, JobMetaRepository repository) {
        this.flinkProperties = flinkProperties;
        this.repository = repository;
        this.defaultConfig = createDefaultConfig();
        this.clusterId = flinkProperties != null ? flinkProperties.getRest().getClusterId() : "standalone";
    }

    public FlinkJobService(Configuration config) {
        this.flinkProperties = null;
        this.repository = new JobMetaRepository();
        this.defaultConfig = config;
        this.clusterId = "standalone";
    }

    private Configuration createDefaultConfig() {
        if (flinkProperties == null) {
            return new Configuration();
        }

        Configuration config = new Configuration();
        config.set(RestOptions.ADDRESS, flinkProperties.getRest().getAddress());
        config.set(RestOptions.PORT, flinkProperties.getRest().getPort());
        config.set(RestOptions.CONNECTION_TIMEOUT, (long) flinkProperties.getClient().getConnectionTimeout());
        config.set(RestOptions.RETRY_MAX_ATTEMPTS, flinkProperties.getClient().getRetryCount());
        config.set(RestOptions.RETRY_DELAY, (long) flinkProperties.getClient().getRetryDelay());

        return config;
    }

    private FlinkRestClientProvider createRestClientProvider() throws Exception {
        return new FlinkRestClientProvider(defaultConfig, clusterId);
    }

    private FlinkRestClientProvider createRestClientProvider(Configuration config) throws Exception {
        return new FlinkRestClientProvider(config, clusterId);
    }

    /* ================= 启动方法重载 ================= */

    public String start(String jobName, String jarPath, String entryClass) {
        return start(jobName, jarPath, entryClass, getDefaultParallelism());
    }

    public String start(String jobName, String jarPath, String entryClass, int parallelism) {
        return start(jobName, jarPath, entryClass, parallelism, null, defaultConfig);
    }

    public String start(String jobName, String jarPath, String entryClass, int parallelism,
                        String savepointPath, Configuration customConfig) {

        validateJobParameters(jobName, jarPath, entryClass);

        String jobId = repository.loadJobId(jobName);
        if (StringUtils.isNotBlank(jobId) && isJobRunning(jobId)) {
            log.info("Job already running: {}", jobId);
            return jobId;
        }

        String savepoint = StringUtils.isNotBlank(savepointPath) ?
                savepointPath : repository.loadSavepoint(jobName);

        Configuration configToUse = customConfig != null ? customConfig : defaultConfig;

        try (FlinkRestClientProvider provider = createRestClientProvider(configToUse)) {

            PackagedProgram.Builder builder = PackagedProgram.newBuilder()
                    .setJarFile(new File(jarPath))
                    .setEntryPointClassName(entryClass);

            if (StringUtils.isNotBlank(savepoint)) {
                boolean allowNonRestoredState = flinkProperties != null ?
                        flinkProperties.getJob().isAllowNonRestoredState() : true;
                builder.setSavepointRestoreSettings(
                        SavepointRestoreSettings.forPath(savepoint, allowNonRestoredState)
                );
            }

            JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
                    builder.build(),
                    configToUse,
                    parallelism,
                    false
            );

            JobID newJobId = provider.getClient().submitJob(jobGraph).get();
            repository.saveJobId(jobName, newJobId.toHexString());
            log.info("Job started: {} with parallelism: {}", newJobId, parallelism);
            return newJobId.toHexString();
        } catch (Exception e) {
            throw FlinkJobException.submissionError(jobName, e);
        }
    }

    public String startWithArgs(String jobName, String jarPath, String entryClass,
                                String[] programArgs, int parallelism) {
        return startWithArgs(jobName, jarPath, entryClass, programArgs, parallelism, null);
    }

    public String startWithArgs(String jobName, String jarPath, String entryClass,
                                String[] programArgs, int parallelism, String savepointPath) {
        validateJobParameters(jobName, jarPath, entryClass);

        String jobId = repository.loadJobId(jobName);
        if (StringUtils.isNotBlank(jobId) && isJobRunning(jobId)) {
            log.info("Job already running: {}", jobId);
            return jobId;
        }

        String savepoint = StringUtils.isNotBlank(savepointPath) ?
                savepointPath : repository.loadSavepoint(jobName);

        Configuration configToUse = defaultConfig;

        try (FlinkRestClientProvider provider = createRestClientProvider(configToUse)) {

            PackagedProgram.Builder builder = PackagedProgram.newBuilder()
                    .setJarFile(new File(jarPath))
                    .setEntryPointClassName(entryClass)
                    .setArguments(programArgs);

            if (StringUtils.isNotBlank(savepoint)) {
                boolean allowNonRestoredState = flinkProperties != null ?
                        flinkProperties.getJob().isAllowNonRestoredState() : true;
                builder.setSavepointRestoreSettings(
                        SavepointRestoreSettings.forPath(savepoint, allowNonRestoredState)
                );
            }

            JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
                    builder.build(),
                    configToUse,
                    parallelism,
                    false
            );

            JobID newJobId = provider.getClient().submitJob(jobGraph).get();
            repository.saveJobId(jobName, newJobId.toHexString());
            log.info("Job started with args: {} with parallelism: {}", newJobId, parallelism);
            return newJobId.toHexString();
        } catch (Exception e) {
            throw FlinkJobException.submissionError(jobName, e);
        }
    }

    /* ================= 停止方法 ================= */

    public void stop(String jobName, String savepointDir) {
        stop(jobName, savepointDir, true);
    }

    public void stop(String jobName, String savepointDir, boolean cancelJob) {
        String jobIdStr = repository.loadJobId(jobName);
        if (StringUtils.isBlank(jobIdStr)) {
            log.warn("Job not found: {}", jobName);
            return;
        }

        JobID jobId = JobID.fromHexString(jobIdStr);

        try (FlinkRestClientProvider provider = createRestClientProvider()) {
            String savepoint = provider.getClient()
                    .stopWithSavepoint(jobId, cancelJob, savepointDir, SavepointFormatType.DEFAULT)
                    .get();

            repository.saveSavepoint(jobName, savepoint);
            log.info("Job stopped: {}, savepoint: {}", jobId, savepoint);
        } catch (Exception e) {
            throw FlinkJobException.stopError(jobIdStr, e);
        }
    }

    public void cancel(String jobName) {
        String jobIdStr = repository.loadJobId(jobName);
        if (StringUtils.isBlank(jobIdStr)) {
            log.warn("Job not found: {}", jobName);
            return;
        }

        JobID jobId = JobID.fromHexString(jobIdStr);

        try (FlinkRestClientProvider provider = createRestClientProvider()) {
            provider.getClient().cancel(jobId).get();
            log.info("Job cancelled: {}", jobId);
        } catch (Exception e) {
            throw FlinkJobException.stopError(jobIdStr, e);
        }
    }

    /* ================= 状态方法 ================= */

    public boolean isJobRunning(String jobIdStr) {
        try (FlinkRestClientProvider provider = createRestClientProvider()) {
            JobStatus status = provider.getClient()
                    .getJobStatus(JobID.fromHexString(jobIdStr))
                    .get();

            return status == JobStatus.RUNNING
                    || status == JobStatus.CREATED
                    || status == JobStatus.RESTARTING
                    || status == JobStatus.INITIALIZING;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FlinkJobNotFoundException ||
                    (e.getMessage() != null && e.getMessage().contains("FlinkJobNotFoundException"))) {
                return false;
            }
            throw FlinkJobException.networkError("checking job status", e);
        } catch (Exception e) {
            throw FlinkJobException.networkError("checking job status", e);
        }
    }

    public JobStatus getJobStatus(String jobIdStr) {
        try (FlinkRestClientProvider provider = createRestClientProvider()) {
            return provider.getClient()
                    .getJobStatus(JobID.fromHexString(jobIdStr))
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FlinkJobNotFoundException ||
                    (e.getMessage() != null && e.getMessage().contains("FlinkJobNotFoundException"))) {
                throw FlinkJobException.jobNotFound(jobIdStr);
            }
            throw FlinkJobException.networkError("getting job status", e);
        } catch (Exception e) {
            throw FlinkJobException.networkError("getting job status", e);
        }
    }

    public String getJobIdByName(String jobName) {
        return repository.loadJobId(jobName);
    }

    public String getSavepointByJobName(String jobName) {
        return repository.loadSavepoint(jobName);
    }

    /* ================= 工具方法 ================= */

    private void validateJobParameters(String jobName, String jarPath, String entryClass) {
        if (StringUtils.isBlank(jobName)) {
            throw FlinkJobException.configurationError("Job name cannot be empty", null);
        }
        if (StringUtils.isBlank(jarPath)) {
            throw FlinkJobException.configurationError("JAR path cannot be empty", null);
        }
        if (StringUtils.isBlank(entryClass)) {
            throw FlinkJobException.configurationError("Entry class cannot be empty", null);
        }

        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw FlinkJobException.configurationError("JAR file not found: " + jarPath, null);
        }
        if (!jarFile.isFile()) {
            throw FlinkJobException.configurationError("JAR path is not a file: " + jarPath, null);
        }
    }

    private int getDefaultParallelism() {
        return flinkProperties != null ? flinkProperties.getJob().getDefaultParallelism() : 1;
    }

    public Configuration getDefaultConfig() {
        return defaultConfig;
    }

    public void updateDefaultConfig(Configuration newConfig) {
        this.defaultConfig = newConfig;
    }
}