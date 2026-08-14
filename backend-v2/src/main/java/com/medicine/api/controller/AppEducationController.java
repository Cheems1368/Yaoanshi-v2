package com.medicine.api.controller;

import com.medicine.api.dto.education.EducationContentCardDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.education.EducationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/app-api/education")
public class AppEducationController {

    private final EducationService educationService;

    public AppEducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @GetMapping("/recommendations")
    public CommonResult<List<EducationContentCardDTO>> recommendations(
            @RequestParam(value = "limit", defaultValue = "5") int limit,
            Principal principal
    ) {
        return CommonResult.ok(educationService.recommendations(principal.getName(), limit));
    }

    @GetMapping("/cards")
    public CommonResult<PageResult<EducationContentCardDTO>> cards(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        return CommonResult.ok(educationService.appList(keyword, category, pageNo, pageSize));
    }

    @GetMapping("/cards/{id}")
    public CommonResult<EducationContentCardDTO> cardDetail(@PathVariable("id") Long id) {
        return CommonResult.ok(educationService.appDetail(id));
    }
}
