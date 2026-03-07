package com.medicine.api.controller;

import com.medicine.api.dto.prescription.PrescriptionConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.api.dto.prescription.PrescriptionRejectRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminPrescriptionService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/prescriptions")
public class AdminPrescriptionController {

    private final AdminPrescriptionService adminPrescriptionService;

    public AdminPrescriptionController(AdminPrescriptionService adminPrescriptionService) {
        this.adminPrescriptionService = adminPrescriptionService;
    }

    @GetMapping
    public CommonResult<PageResult<PrescriptionDTO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.getPrescriptions(claims, keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public CommonResult<PrescriptionDTO> detail(@PathVariable("id") Long id, Authentication authentication) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.getPrescriptionDetail(claims, id));
    }

    @PostMapping("/{id}/confirm")
    public CommonResult<PrescriptionDTO> confirm(
            @PathVariable("id") Long id,
            @RequestBody PrescriptionConfirmRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.confirmPrescription(claims, id, request));
    }

    @PostMapping("/{id}/reject")
    public CommonResult<PrescriptionDTO> reject(
            @PathVariable("id") Long id,
            @RequestBody PrescriptionRejectRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.rejectPrescription(claims, id, request));
    }
}
