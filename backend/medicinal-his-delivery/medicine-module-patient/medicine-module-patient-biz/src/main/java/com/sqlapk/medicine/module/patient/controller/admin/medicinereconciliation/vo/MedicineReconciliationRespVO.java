package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 药物重整剂量 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineReconciliationRespVO extends MedicineReconciliationBaseVO {

    @Schema(description = "重整ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29142")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private List<MedicineReconciliationDetailRespVO> details;

}
