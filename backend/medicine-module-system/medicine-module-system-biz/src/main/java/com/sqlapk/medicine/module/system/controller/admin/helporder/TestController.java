package com.sqlapk.medicine.module.system.controller.admin.helporder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器 - 用于验证包扫描是否正常工作
 *
 * @author sqlapk
 */
@RestController
@RequestMapping("/system/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TestController!";
    }
}
