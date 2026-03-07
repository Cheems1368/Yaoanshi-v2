package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 药物重整剂量更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineReconciliationUpdateReqVO extends MedicineReconciliationBaseVO {

    @Schema(description = "重整ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29142")
    @NotNull(message = "重整ID不能为空")
    private Long id;

    List<MedicineReconciliationDetailCreateReqVO> details;
}
