package com.medicine.api.controller;

import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionUploadRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.app.AppPrescriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/prescriptions")
public class AppPrescriptionController {

    private final AppPrescriptionService appPrescriptionService;

    public AppPrescriptionController(AppPrescriptionService appPrescriptionService) {
        this.appPrescriptionService = appPrescriptionService;
    }

    @GetMapping
    public CommonResult<PageResult<PrescriptionDTO>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            Authentication authentication
    ) {
        return CommonResult.ok(appPrescriptionService.getPrescriptions(authentication.getName(), status, pageNo, pageSize));
    }

    @PostMapping("/uploads")
    public CommonResult<PrescriptionDTO> upload(@RequestBody PrescriptionUploadRequest request, Authentication authentication) {
        return CommonResult.ok(appPrescriptionService.registerUpload(authentication.getName(), request));
    }
}
