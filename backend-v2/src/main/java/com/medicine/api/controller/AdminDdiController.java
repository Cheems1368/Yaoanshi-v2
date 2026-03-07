package com.medicine.api.controller;

import com.medicine.api.dto.prescription.DdiScreeningConfirmRequest;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.admin.AdminPrescriptionService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/ddi")
public class AdminDdiController {

    private final AdminPrescriptionService adminPrescriptionService;

    public AdminDdiController(AdminPrescriptionService adminPrescriptionService) {
        this.adminPrescriptionService = adminPrescriptionService;
    }

    @PostMapping("/screenings/{id}/confirm")
    public CommonResult<PrescriptionDTO.DdiScreeningDTO> confirm(
            @PathVariable("id") Long id,
            @RequestBody(required = false) DdiScreeningConfirmRequest request,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(adminPrescriptionService.confirmScreening(claims, id, request));
    }
}
