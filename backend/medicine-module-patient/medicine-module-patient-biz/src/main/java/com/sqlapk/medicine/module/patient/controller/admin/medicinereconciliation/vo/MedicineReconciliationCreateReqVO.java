package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo;

import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 药物重整剂量创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MedicineReconciliationCreateReqVO extends MedicineReconciliationBaseVO {
    List<MedicineReconciliationDetailCreateReqVO> details;
}
