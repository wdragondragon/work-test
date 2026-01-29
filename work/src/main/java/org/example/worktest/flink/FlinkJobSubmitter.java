package org.example.worktest.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkJobSubmitter {

    private static final Logger log = LoggerFactory.getLogger(FlinkJobSubmitter.class);

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.set(RestOptions.ADDRESS, "192.168.188.128");
        config.set(RestOptions.PORT, 8081);

        FlinkJobService service = new FlinkJobService(config);

        String jobId = service.start(
                "test-job",
                "C:\\dev\\ideaProject\\flink-cdc\\target\\flink-cdc-1.0-SNAPSHOT.jar",
                "com.jdragon.flinkcdc.MySqlCdcToMySqlJob"
        );

        log.info("jobId = {}", jobId);

//         service.stop("test-job", "file:///var/data/flink/savepoints/mysql-cdc");
    }
}


