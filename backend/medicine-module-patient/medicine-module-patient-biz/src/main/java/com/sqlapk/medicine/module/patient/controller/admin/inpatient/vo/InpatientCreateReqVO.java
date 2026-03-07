package com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo;

import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 住院药学服务创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientCreateReqVO extends InpatientBaseVO {
    List<InpatientAdviceCreateReqVO> advice;
    List<InpatientEducationCreateReqVO> education;
    List<InpatientConsultationCreateReqVO> consultation;
    List<InpatientEvaluationCreateReqVO> evaluation;
    List<InpatientRoundCreateReqVO> round;
}
