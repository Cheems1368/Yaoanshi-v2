package com.sqlapk.medicine.module.system.controller.admin.helporder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

/**
 * 简单测试控制器 - 验证包扫描是否工作
 */
@RestController
@RequestMapping("/simple")
public class SimpleController {

    @GetMapping("/ping")
    @PermitAll
    public String ping() {
        return "工单模块已成功注册！";
    }

    @GetMapping("/status")
    public String status() {
        return "工单控制器正常工作";
    }

    @GetMapping("/test")
    @PermitAll
    public String test() {
        return "测试接口 - 无需认证";
    }
} 