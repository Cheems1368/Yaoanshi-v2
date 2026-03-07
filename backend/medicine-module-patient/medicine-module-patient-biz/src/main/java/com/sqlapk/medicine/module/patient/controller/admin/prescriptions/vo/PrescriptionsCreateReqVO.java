package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 处方创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionsCreateReqVO extends PrescriptionsBaseVO {
    private List<PrescriptionDetailCreateReqVO> inspection;
    private List<PrescriptionExaminationsCreateReqVO> exam;

}
