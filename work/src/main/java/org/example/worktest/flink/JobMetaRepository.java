package org.example.worktest.flink;

import cn.hutool.core.io.FileUtil;
import org.example.worktest.flink.config.FlinkProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class JobMetaRepository {

    private final FlinkProperties flinkProperties;

    private String jobIdDir;
    private String savepointDir;
    private boolean initialized = false;

    public JobMetaRepository(FlinkProperties flinkProperties) {
        this.flinkProperties = flinkProperties;
        init();
    }

    public JobMetaRepository() {
        this.flinkProperties = new FlinkProperties();
        init();
    }

    @PostConstruct
    public void postConstruct() {
        if (!initialized) {
            init();
        }
    }

    private void init() {
        if (initialized) {
            return;
        }
        
        if (flinkProperties != null && flinkProperties.getStorage() != null) {
            jobIdDir = normalizePath(flinkProperties.getStorage().getJobIdDir());
            savepointDir = normalizePath(flinkProperties.getStorage().getSavepointDir());
        } else {
            jobIdDir = normalizePath("/var/data/flink/job-id-latest/");
            savepointDir = normalizePath("/var/data/flink/savepoint-latest/");
        }

        ensureDirectoryExists(jobIdDir);
        ensureDirectoryExists(savepointDir);
        initialized = true;
    }

    public String loadJobId(String jobName) {
        File file = new File(jobIdDir + jobName + ".txt");
        return file.exists() ? FileUtil.readUtf8String(file) : null;
    }

    public String loadSavepoint(String jobName) {
        File file = new File(savepointDir + jobName + ".txt");
        return file.exists() ? FileUtil.readUtf8String(file) : null;
    }

    public void saveJobId(String jobName, String jobId) {
        FileUtil.writeUtf8String(jobId, jobIdDir + jobName + ".txt");
    }

    public void saveSavepoint(String jobName, String savepointPath) {
        FileUtil.writeUtf8String(savepointPath, savepointDir + jobName + ".txt");
    }

    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return System.getProperty("user.dir") + File.separator + "data" + File.separator + "flink" + File.separator;
        }

        String normalized = path.trim();
        if (!normalized.endsWith(File.separator)) {
            normalized += File.separator;
        }

        return normalized;
    }

    private void ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + dirPath);
            }
        }
    }
}
