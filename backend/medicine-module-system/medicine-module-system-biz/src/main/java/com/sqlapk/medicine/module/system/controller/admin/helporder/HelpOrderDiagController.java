package com.sqlapk.medicine.module.system.controller.admin.helporder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单诊断控制器 - 用于验证模块是否被Spring Boot扫描
 *
 * @author sqlapk
 */
@RestController
@RequestMapping("/_diag/help")
public class HelpOrderDiagController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("message", "工单模块已成功注册");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("module", "help-order");
        result.put("package", "com.sqlapk.medicine.module.system.controller.admin.helporder");
        result.put("status", "registered");
        return result;
    }
}
