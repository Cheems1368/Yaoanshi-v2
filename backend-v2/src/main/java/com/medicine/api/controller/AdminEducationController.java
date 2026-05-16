package com.medicine.api.controller;

import com.medicine.api.dto.common.CsvImportResultDTO;
import com.medicine.api.dto.education.EducationContentCardDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.education.EducationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin-api/education")
public class AdminEducationController {

    private final EducationService educationService;

    public AdminEducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @GetMapping("/cards")
    public CommonResult<PageResult<EducationContentCardDTO>> listCards(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "reviewStatus", required = false) String reviewStatus,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(educationService.adminList(claims, keyword, category, reviewStatus, pageNo, pageSize));
    }

    @PostMapping("/cards/imports")
    public CommonResult<CsvImportResultDTO> importCards(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dryRun", defaultValue = "true") boolean dryRun,
            Authentication authentication
    ) {
        JwtClaims claims = (JwtClaims) authentication.getDetails();
        return CommonResult.ok(educationService.importCards(claims, file, dryRun));
    }
}
