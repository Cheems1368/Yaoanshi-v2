package com.sqlapk.medicine.module.patient.dal.dataobject.prescription;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 电子处方 DO
 *
 * @author sqlapk
 */
@TableName("electronic_prescriptions")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectronicPrescriptionDO extends BaseDO {

    /**
     * 处方ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 处方编号（如：RX-6F29-****）
     */
    private String prescriptionCode;
    
    /**
     * 工单ID
     */
    private Long orderId;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 药师ID
     */
    private Long pharmacistId;
    
    /**
     * 处方类型（renewal/new/adjustment）
     */
    private String prescriptionType;
    
    /**
     * 诊断信息
     */
    private String diagnosis;
    
    /**
     * 处方药品信息（JSON格式）
     */
    private String medications;
    
    /**
     * 用法用量说明
     */
    private String instructions;
    
    /**
     * 注意事项
     */
    private String precautions;
    
    /**
     * 处方状态（draft/confirmed/signed/dispensed/completed/cancelled）
     */
    private String status;
    
    /**
     * 电子签名
     */
    private String digitalSignature;
    
    /**
     * 签名时间
     */
    private LocalDateTime signatureTime;
    
    /**
     * 签名证书
     */
    private String signatureCertificate;
    
    /**
     * PDF文件URL
     */
    private String pdfUrl;
    
    /**
     * 有效期（天数）
     */
    private Integer validityDays;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;
    
    /**
     * 审核意见
     */
    private String reviewComments;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
    
    /**
     * 配药状态（pending/dispensed/picked_up）
     */
    private String dispensingStatus;
    
    /**
     * 配药时间
     */
    private LocalDateTime dispensingTime;
    
    /**
     * 取药时间
     */
    private LocalDateTime pickupTime;
}
