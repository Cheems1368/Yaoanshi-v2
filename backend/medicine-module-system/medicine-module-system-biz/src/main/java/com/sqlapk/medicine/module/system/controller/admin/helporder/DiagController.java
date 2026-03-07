package com.sqlapk.medicine.module.system.controller.admin.helporder;

import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 诊断控制器 - 用于验证模块是否被加载
 *
 * @author sqlapk
 */
@RestController
@RequestMapping("/_diag")
public class DiagController {

    @Resource
    private HelpOrderService helpOrderService;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
            "ok", true, 
            "module", "helporder", 
            "timestamp", System.currentTimeMillis(),
            "helpOrderService", helpOrderService != null ? "injected" : "not injected"
        );
    }
}
