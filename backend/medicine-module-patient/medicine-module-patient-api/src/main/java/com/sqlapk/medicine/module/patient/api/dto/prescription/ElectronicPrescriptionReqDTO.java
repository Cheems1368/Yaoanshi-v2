package com.sqlapk.medicine.module.patient.api.dto.prescription;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 电子处方请求 DTO
 *
 * @author sqlapk
 */
@Data
public class ElectronicPrescriptionReqDTO {

    /**
     * 工单ID
     */
    @NotNull(message = "工单ID不能为空")
    private Long orderId;

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 医生ID
     */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    /**
     * 药师ID
     */
    private Long pharmacistId;

    /**
     * 处方类型
     */
    @NotBlank(message = "处方类型不能为空")
    private String prescriptionType;

    /**
     * 诊断信息
     */
    @NotBlank(message = "诊断信息不能为空")
    private String diagnosis;

    /**
     * 处方药品列表
     */
    @NotNull(message = "处方药品不能为空")
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
     * 有效期（天数）
     */
    private Integer validityDays;

    /**
     * 药品信息DTO
     */
    @Data
    public static class MedicationDTO {
        /**
         * 药品名称
         */
        @NotBlank(message = "药品名称不能为空")
        private String drugName;

        /**
         * 药品规格
         */
        private String specification;

        /**
         * 单次剂量
         */
        @NotBlank(message = "单次剂量不能为空")
        private String dosage;

        /**
         * 用法
         */
        @NotBlank(message = "用法不能为空")
        private String usage;

        /**
         * 频次
         */
        @NotBlank(message = "频次不能为空")
        private String frequency;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
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
