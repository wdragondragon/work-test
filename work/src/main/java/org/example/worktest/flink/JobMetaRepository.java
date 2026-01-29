package org.example.worktest.flink;

import cn.hutool.core.io.FileUtil;

import java.io.File;

public class JobMetaRepository {

    private static final String JOB_ID_DIR = "/data/flink/jobId-latest/";
    private static final String SAVEPOINT_DIR = "/data/flink/savepoint-latest/";

    public String loadJobId(String jobName) {
        File file = new File(JOB_ID_DIR + jobName + ".txt");
        return file.exists() ? FileUtil.readUtf8String(file) : null;
    }

    public String loadSavepoint(String jobName) {
        File file = new File(SAVEPOINT_DIR + jobName + ".txt");
        return file.exists() ? FileUtil.readUtf8String(file) : null;
    }

    public void saveJobId(String jobName, String jobId) {
        FileUtil.writeUtf8String(jobId, JOB_ID_DIR + jobName + ".txt");
    }

    public void saveSavepoint(String jobName, String savepointPath) {
        FileUtil.writeUtf8String(savepointPath, SAVEPOINT_DIR + jobName + ".txt");
    }
}
