package com.medicine.api.controller;

import com.medicine.common.model.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public CommonResult<Map<String, String>> health() {
        Map<String, String> payload = new HashMap<>();
        payload.put("service", "backend-v2");
        payload.put("status", "UP");
        return CommonResult.ok(payload);
    }
}
