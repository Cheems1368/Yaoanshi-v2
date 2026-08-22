package com.medicine.api.controller;

import com.medicine.api.dto.identity.AdminPatientDetailDTO;
import com.medicine.api.dto.identity.AdminPatientListItemDTO;
import com.medicine.api.dto.identity.AdminPatientUpdateRequest;
import com.medicine.api.dto.identity.PatientReassignRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminPatientService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/patients")
public class AdminPatientController {

    private final AdminPatientService adminPatientService;

    public AdminPatientController(AdminPatientService adminPatientService) {
        this.adminPatientService = adminPatientService;
    }

    @GetMapping
    public CommonResult<PageResult<AdminPatientListItemDTO>> listPatients(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPatientService.getPatients(claims, keyword, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public CommonResult<AdminPatientDetailDTO> getPatientDetail(@PathVariable("id") Long id, Authentication authentication) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPatientService.getPatientDetail(claims, id));
    }

    @PutMapping("/{id}")
    public CommonResult<AdminPatientDetailDTO> updatePatient(
            @PathVariable("id") Long id,
            @RequestBody AdminPatientUpdateRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPatientService.updatePatient(claims, id, request));
    }

    @PutMapping("/{id}/responsible-pharmacist")
    public CommonResult<AdminPatientDetailDTO> reassign(
            @PathVariable("id") Long id, @RequestBody PatientReassignRequest request, Authentication authentication
    ) {
        return CommonResult.ok(adminPatientService.reassignPatient((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/knowledge-assessments/remind")
    public CommonResult<Boolean> remindKnowledgeAssessment(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminPatientService.remindKnowledgeAssessment((JwtClaims) authentication.getDetails(), id));
    }
}
