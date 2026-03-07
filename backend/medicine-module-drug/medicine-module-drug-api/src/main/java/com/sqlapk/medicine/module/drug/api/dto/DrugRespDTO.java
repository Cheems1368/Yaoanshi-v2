package com.sqlapk.medicine.module.drug.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品信息 Response DTO
 *
 * @author sqlapk
 */
@Data
public class DrugRespDTO {

    /**
     * 药品ID
     */
    private Long id;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 药品通用名
     */
    private String genericName;

    /**
     * 药品规格
     */
    private String specification;

    /**
     * 药品单位
     */
    private String unit;

    /**
     * 药品分类
     */
    private String category;

    /**
     * 药品类型（处方药(0)、非处方药(1)）
     */
    private Integer drugType;

    /**
     * 药品价格
     */
    private BigDecimal price;

    /**
     * 药品库存
     */
    private Integer stock;

    /**
     * 药品描述
     */
    private String description;

    /**
     * 药品用法用量
     */
    private String usage;

    /**
     * 药品注意事项
     */
    private String precautions;

    /**
     * 药品禁忌
     */
    private String contraindications;

    /**
     * 药品不良反应
     */
    private String adverseReactions;

    /**
     * 药品状态（启用(0)、禁用(1)）
     */
    private Integer status;

}
