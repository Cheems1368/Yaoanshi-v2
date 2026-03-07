
package com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo;

import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 门诊药学服务 Response VO")
@Data
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutpatientPatientInfoRespVO {

    private List<EducationDO> education;
    private List<PharmacyOutpatientDO> medicine;
    private List<PrescriptionDetailRespVO> prescription;
    private List<PrescriptionExaminationsRespVO> check;
}
