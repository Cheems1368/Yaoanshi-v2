package com.sqlapk.medicine.module.patient.api.dto.prescription;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子处方响应 DTO
 *
 * @author sqlapk
 */
@Data
public class ElectronicPrescriptionRespDTO {

    /**
     * 处方ID
     */
    private Long id;

    /**
     * 处方编号
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
     * 患者姓名
     */
    private String patientName;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 药师ID
     */
    private Long pharmacistId;

    /**
     * 药师姓名
     */
    private String pharmacistName;

    /**
     * 处方类型
     */
    private String prescriptionType;

    /**
     * 诊断信息
     */
    private String diagnosis;

    /**
     * 处方药品列表
     */
    private List<MedicationDTO> medications;

    /**
     * 用法用量说明
     */
    private String instructions;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 处方状态
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
     * 配药状态
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 药品信息DTO
     */
    @Data
    public static class MedicationDTO {
        /**
         * 药品名称
         */
        private String drugName;

        /**
         * 药品规格
         */
        private String specification;

        /**
         * 单次剂量
         */
        private String dosage;

        /**
         * 用法
         */
        private String usage;

        /**
         * 频次
         */
        private String frequency;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 单位
         */
        private String unit;

        /**
         * 备注
         */
        private String notes;
    }
}
