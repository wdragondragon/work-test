package org.example.worktest.flink;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.runtime.messages.FlinkJobNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class FlinkJobService {

    private final Logger log = LoggerFactory.getLogger(FlinkJobService.class);

    private final Configuration config;
    private final JobMetaRepository repository = new JobMetaRepository();

    public FlinkJobService(Configuration config) {
        this.config = config;
    }

    /* ================= 启动 ================= */

    public String start(String jobName, String jarPath, String entryClass) {
        String jobId = repository.loadJobId(jobName);

        if (StringUtils.isNotBlank(jobId) && isJobRunning(jobId)) {
            log.info("Job already running: {}", jobId);
            return jobId;
        }

        String savepoint = repository.loadSavepoint(jobName);

        try (FlinkRestClientProvider provider = new FlinkRestClientProvider(config)) {

            PackagedProgram.Builder builder = PackagedProgram.newBuilder()
                    .setJarFile(new File(jarPath))
                    .setEntryPointClassName(entryClass);

            if (StringUtils.isNotBlank(savepoint)) {
                builder.setSavepointRestoreSettings(
                        SavepointRestoreSettings.forPath(savepoint, true)
                );
            }

            JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
                    builder.build(),
                    config,
                    1,
                    false
            );

            JobID newJobId = provider.getClient().submitJob(jobGraph).get();
            repository.saveJobId(jobName, newJobId.toHexString());
            log.info("Job started: {}", newJobId);
            return newJobId.toHexString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start job", e);
        }
    }

    /* ================= 停止 ================= */

    public void stop(String jobName, String savepointDir) {
        String jobIdStr = repository.loadJobId(jobName);
        if (StringUtils.isBlank(jobIdStr)) {
            return;
        }

        JobID jobId = JobID.fromHexString(jobIdStr);

        try (FlinkRestClientProvider provider = new FlinkRestClientProvider(config)) {

            String savepoint = provider.getClient()
                    .stopWithSavepoint(jobId, true, savepointDir, SavepointFormatType.DEFAULT)
                    .get();

            repository.saveSavepoint(jobName, savepoint);
            log.info("Job stopped: {}, save point : {}", jobId, savepoint);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop job", e);
        }
    }

    /* ================= 状态 ================= */

    public boolean isJobRunning(String jobIdStr) {
        try (FlinkRestClientProvider provider = new FlinkRestClientProvider(config)) {
            JobStatus status = provider.getClient()
                    .getJobStatus(JobID.fromHexString(jobIdStr))
                    .get();

            return status == JobStatus.RUNNING
                    || status == JobStatus.CREATED
                    || status == JobStatus.RESTARTING;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FlinkJobNotFoundException || e.getMessage().contains("FlinkJobNotFoundException")) {
                return false;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
