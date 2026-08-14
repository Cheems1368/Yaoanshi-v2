package com.medicine.service.drug;

import com.medicine.api.dto.drug.DrugCatalogDTO;
import com.medicine.domain.drug.DrugCatalog;
import com.medicine.repository.drug.DrugCatalogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DrugCatalogService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final int MAX_LIMIT = 20;

    private final DrugCatalogMapper drugCatalogMapper;

    public DrugCatalogService(DrugCatalogMapper drugCatalogMapper) {
        this.drugCatalogMapper = drugCatalogMapper;
    }

    /**
     * 按关键词搜索药品目录。
     * 支持中文名称模糊匹配和拼音首字母前缀匹配（如输入 aspl 匹配 阿司匹林）。
     *
     * @param q     搜索关键词（中文名称或拼音首字母）
     * @param limit 最大返回条数，不超过 20
     */
    public List<DrugCatalogDTO> search(String q, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        String keyword = StringUtils.hasText(q) ? q.trim() : null;
        return drugCatalogMapper.search(DEFAULT_TENANT_ID, keyword, safeLimit)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private DrugCatalogDTO toDto(DrugCatalog d) {
        DrugCatalogDTO dto = new DrugCatalogDTO();
        dto.setId(d.getId());
        dto.setNameCn(d.getNameCn());
        dto.setNameEn(d.getNameEn());
        dto.setPinyinInitials(d.getPinyinInitials());
        dto.setCategory(d.getCategory());
        return dto;
    }
}
