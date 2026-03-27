package com.medicine.api.controller;

import com.medicine.api.dto.drug.DrugCatalogDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.drug.DrugCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者端药品目录搜索接口
 * 支持中文名称和拼音首字母检索（如输入 aspl 匹配 阿司匹林）
 */
@RestController
@RequestMapping("/app-api/drugs")
public class AppDrugCatalogController {

    private final DrugCatalogService drugCatalogService;

    public AppDrugCatalogController(DrugCatalogService drugCatalogService) {
        this.drugCatalogService = drugCatalogService;
    }

    /**
     * GET /app-api/drugs/search?q=aspl&limit=10
     */
    @GetMapping("/search")
    public CommonResult<List<DrugCatalogDTO>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        return CommonResult.ok(drugCatalogService.search(q, limit));
    }
}
