package com.seance.screen.web;

import com.alibaba.fastjson.JSON;
import com.seance.screen.dao.FileDto;
import com.seance.screen.service.GetMailService;
import com.seance.screen.service.RecordDuplicationService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author master
 */
@Api(value = "controller测试")
@RestController
public class TestController {
    @Autowired
    private RecordDuplicationService recordDuplicationService;
    @Autowired
    private GetMailService getMailService;

    @GetMapping("/test")
    @ApiOperation(value = "测试", notes = "测试")
    public void test() {
//        recordDuplicationService.main("C:" + File.separator + "Users" + File.separator
//                + "master" + File.separator + "Desktop" + File.separator + "10.17高德", screenJd);
    }

    @PostMapping("/screen-file")
    @ApiOperation(value = "筛选文件", notes = "筛选工具用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "screenJson",value = "筛选JD",required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "screenPath",value = "路径",required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "openDelete",value = "开启删除无效简历",required = true, paramType = "query", dataType = "boolean"),
    })
    public void screen(String screenJson,String screenPath,Boolean openDelete) {
        Map<String, List<String>> screenJd = (Map<String, List<String>>) JSON.parse(screenJson);
        recordDuplicationService.main(screenPath,screenJd,openDelete);
    }


    @PostMapping("/getMail")
    @ApiOperation(value = "获取邮件列表", notes = "获取邮件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "emailName",value = "邮箱账号",required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "passWord",value = "密码",required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "typeName",value = "简历主题筛选关键字",required = true, paramType = "query", dataType = "string",allowableValues="快手,百度,小米"),
    })
    public void test2(String emailName, String passWord,String typeName) {
        getMailService.getMail(emailName,passWord,typeName);
    }


}
