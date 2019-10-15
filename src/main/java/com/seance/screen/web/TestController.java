package com.seance.screen.web;

import com.seance.screen.service.RecordDuplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author master
 */
@Api(value = "controller测试")
@RestController
public class TestController {
    @Autowired
    private RecordDuplicationService recordDuplicationService;

    @GetMapping("/test")
    @ApiOperation(value = "测试", notes = "测试")
    public void test() {
        recordDuplicationService.main("C:" + File.separator + "Users" + File.separator
                + "master" + File.separator + "Desktop" + File.separator + "10.14高德");
    }
}
