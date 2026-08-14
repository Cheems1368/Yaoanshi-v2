package com.medicine.api.controller;

import com.medicine.api.dto.medication.SelfReportedMedicationCreateRequest;
import com.medicine.api.dto.medication.SelfReportedMedicationDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppSelfReportedMedicationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app-api/self-reported-medications")
public class AppSelfReportedMedicationController {

    private final AppSelfReportedMedicationService service;

    public AppSelfReportedMedicationController(AppSelfReportedMedicationService service) {
        this.service = service;
    }

    @PostMapping
    public CommonResult<List<SelfReportedMedicationDTO>> create(
            @RequestBody SelfReportedMedicationCreateRequest request,
            @RequestParam(value = "priority", required = false, defaultValue = "NORMAL") String priority,
            Authentication authentication
    ) {
        return CommonResult.ok(service.create(authentication.getName(), request, priority));
    }

    @GetMapping
    public CommonResult<List<SelfReportedMedicationDTO>> list(Authentication authentication) {
        return CommonResult.ok(service.list(authentication.getName()));
    }

    @PostMapping("/{id}/withdraw")
    public CommonResult<Void> withdraw(@PathVariable("id") Long id, Authentication authentication) {
        service.withdraw(authentication.getName(), id);
        return CommonResult.ok(null);
    }

    @PostMapping("/{id}/stop")
    public CommonResult<Void> stop(@PathVariable("id") Long id, Authentication authentication) {
        service.stop(authentication.getName(), id);
        return CommonResult.ok(null);
    }
}
