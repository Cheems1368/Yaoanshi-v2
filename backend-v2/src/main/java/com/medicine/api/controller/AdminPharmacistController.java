package com.medicine.api.controller;

import com.medicine.api.dto.identity.PharmacistOptionDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.admin.AdminPatientService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin-api/pharmacists")
public class AdminPharmacistController {
    private final AdminPatientService adminPatientService;

    public AdminPharmacistController(AdminPatientService adminPatientService) {
        this.adminPatientService = adminPatientService;
    }

    @GetMapping("/options")
    public CommonResult<List<PharmacistOptionDTO>> options(Authentication authentication) {
        return CommonResult.ok(adminPatientService.getActivePharmacists((JwtClaims) authentication.getDetails()));
    }
}
