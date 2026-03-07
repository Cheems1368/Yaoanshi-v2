package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 药物重整剂量更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineReconciliationDetailUpdateReqVO extends MedicineReconciliationDetailBaseVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16137")
    @NotNull(message = "明细ID不能为空")
    private Long id;

}
