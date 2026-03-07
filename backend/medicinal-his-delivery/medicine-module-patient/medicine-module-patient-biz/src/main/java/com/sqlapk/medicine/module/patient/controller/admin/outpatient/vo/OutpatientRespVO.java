package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 门诊药学服务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientRespVO extends OutpatientBaseVO {

    @Schema(description = "门诊服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31106")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    PatientsSimpleRespVO patient;

    List<OutpatientCheckDO> audit; // 处方审核
    List<PharmacyOutpatientDO> medicine; //药学门诊
    List<EducationDO> education; //用药教育
    List<PrescriptionExaminationsRespVO> check; //检查
}
