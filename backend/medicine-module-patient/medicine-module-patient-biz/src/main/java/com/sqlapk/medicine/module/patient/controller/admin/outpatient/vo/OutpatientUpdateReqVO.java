package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 门诊药学服务更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientUpdateReqVO extends OutpatientBaseVO {

    @Schema(description = "门诊服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31106")
    @NotNull(message = "门诊服务ID不能为空")
    private Long id;

    List<OutpatientCheckCreateReqVO> audit; // 处方审核
    List<PharmacyOutpatientCreateReqVO> medicine; //药学门诊
    List<EducationCreateReqVO> education; //用药教育
    List<PrescriptionExaminationsRespVO> check; //检查

}
