package org.example.worktest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api("测试")
@RequestMapping("api")
@RestController
public class ApiController {

    @ApiOperation("测试1")
    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @ApiOperation("测试2")
    @GetMapping("/test/test2")
    public String test2() {
        return "test";
    }


}
