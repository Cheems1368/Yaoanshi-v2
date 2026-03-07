package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用药报告 DO
 *
 * @author sqlapk
 */
@TableName("med_reports")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedReportDO extends BaseDO {

    /**
     * 报告ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 报告类型(1-周报告,2-月报告,3-季度报告,4-年度报告)
     */
    private Integer reportType;
    
    /**
     * 报告标题
     */
    private String title;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 报告内容(JSON格式)
     */
    private String content;
    
    /**
     * 依从性摘要(JSON格式)
     */
    private String adherenceSummary;
    
    /**
     * 用药摘要(JSON格式)
     */
    private String medicationSummary;
    
    /**
     * 健康指标(JSON格式)
     */
    private String healthIndicators;
    
    /**
     * 生成时间
     */
    private LocalDateTime generationTime;
    
    /**
     * 报告状态(1-生成中,2-已完成,3-生成失败)
     */
    private Integer status;
    
    /**
     * 口径版本号
     */
    private Integer version;
    
    /**
     * 报告文件路径
     */
    private String filePath;
    
    /**
     * 报告文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 报告文件格式(PDF,HTML,JSON)
     */
    private String fileFormat;

}
