package com.medicine.api.controller;

import com.medicine.api.dto.ddi.AdminDdiRiskCardDTO;
import com.medicine.api.dto.ddi.AdminDdiRuleDTO;
import com.medicine.api.dto.ddi.AdminDdiRuleUpsertRequest;
import com.medicine.api.dto.ddi.DrugContraindicationDTO;
import com.medicine.api.dto.ddi.DrugContraindicationUpsertRequest;
import com.medicine.api.dto.prescription.DdiScreeningConfirmRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminDdiService;
import com.medicine.service.admin.AdminPrescriptionService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/ddi")
public class AdminDdiController {

    private final AdminPrescriptionService adminPrescriptionService;
    private final AdminDdiService adminDdiService;

    public AdminDdiController(AdminPrescriptionService adminPrescriptionService, AdminDdiService adminDdiService) {
        this.adminPrescriptionService = adminPrescriptionService;
        this.adminDdiService = adminDdiService;
    }

    @GetMapping("/rules")
    public CommonResult<PageResult<AdminDdiRuleDTO>> listRules(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.listRules(claims, keyword, severity, pageNo, pageSize));
    }

    @PostMapping("/rules")
    public CommonResult<AdminDdiRuleDTO> createRule(
            @RequestBody AdminDdiRuleUpsertRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.createRule(claims, request));
    }

    @PutMapping("/rules/{id}")
    public CommonResult<AdminDdiRuleDTO> updateRule(
            @PathVariable("id") Long id,
            @RequestBody AdminDdiRuleUpsertRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.updateRule(claims, id, request));
    }

    @GetMapping("/contraindications")
    public CommonResult<PageResult<DrugContraindicationDTO>> listContraindications(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "conditionType", required = false) String conditionType,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.listContraindications(claims, keyword, severity, conditionType, pageNo, pageSize));
    }

    @PostMapping("/contraindications")
    public CommonResult<DrugContraindicationDTO> createContraindication(
            @RequestBody DrugContraindicationUpsertRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.createContraindication(claims, request));
    }

    @PutMapping("/contraindications/{id}")
    public CommonResult<DrugContraindicationDTO> updateContraindication(
            @PathVariable("id") Long id,
            @RequestBody DrugContraindicationUpsertRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminDdiService.updateContraindication(claims, id, request));
    }

    @PostMapping("/screenings/{id}/confirm")
    public CommonResult<AdminDdiRiskCardDTO> confirm(
            @PathVariable("id") Long id,
            @RequestBody(required = false) DdiScreeningConfirmRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.confirmScreening(claims, id, request));
    }
}
