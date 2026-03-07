package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 门诊药学服务创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientCreateReqVO extends OutpatientBaseVO {
    List<OutpatientCheckCreateReqVO> audit; // 处方审核
    List<PharmacyOutpatientCreateReqVO> medicine; //药学门诊
    List<EducationCreateReqVO> education; //用药教育
    List<PrescriptionExaminationsRespVO> check; //检查
}
