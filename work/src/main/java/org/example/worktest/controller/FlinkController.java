package org.example.worktest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.worktest.flink.FlinkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api("flink测试")
@RequestMapping("/api/executor")
@RestController
public class FlinkController {

    @Autowired
    private FlinkJobService service;

    @PostMapping("/start")
    @ApiOperation("启动jar作业")
    public String start(@RequestParam String jobName, @RequestParam String jarPath,
                        @RequestParam String entryClass) {
        String jobId = service.start(
                jobName, jarPath, entryClass
        );
        log.info("jobId = {}", jobId);
        return jobId;
    }

    @PostMapping("/stop")
    @ApiOperation("停止jar作业")
    public String stop(@RequestParam String jobName) {
        service.stop(jobName, "file:///var/data/flink/savepoints/mysql-cdc");
        return "success";
    }

}
