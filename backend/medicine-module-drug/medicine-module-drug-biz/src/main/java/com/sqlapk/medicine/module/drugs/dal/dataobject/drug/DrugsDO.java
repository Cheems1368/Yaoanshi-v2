package com.sqlapk.medicine.module.drugs.dal.dataobject.drug;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 药品 DO
 *
 * @author 管理员
 */
@TableName("drugs")
@KeySequence("drugs_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugsDO extends BaseDO {

    /**
     * 药品ID
     */
    @TableId
    private Long drugId;
    /**
     * 药品名称
     */
    private String drugName;
    /**
     * 药品规格
     */
    private String specification;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 批准文号
     */
    private String approvalNumber;
    /**
     * 有效期
     */
    private LocalDate expiryDate;
    /**
     * 药品分类
     */
    private String category;
    /**
     * 药品剂型
     */
    private String dosageForm;
    /**
     * 药品成分
     */
    private String ingredients;
    /**
     * 适应症
     */
    private String indications;
    /**
     * 用法用量
     */
    private String dosage;
    /**
     * 不良反应
     */
    private String sideEffects;
    /**
     * 禁忌
     */
    private String contraindications;
    /**
     * 注意事项
     */
    private String precautions;
    /**
     * 药物相互作用
     */
    private String interactions;
    /**
     * 药品图片URL
     */
    private String imageUrl;

}
