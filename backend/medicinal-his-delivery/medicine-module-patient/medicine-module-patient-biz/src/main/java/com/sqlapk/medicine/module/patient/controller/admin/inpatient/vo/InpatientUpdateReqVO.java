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

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 住院药学服务更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientUpdateReqVO extends InpatientBaseVO {

    @Schema(description = "住院服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30456")
    @NotNull(message = "住院服务ID不能为空")
    private Long id;

    List<InpatientAdviceCreateReqVO> advice;
    List<InpatientEducationCreateReqVO> education;
    List<InpatientConsultationCreateReqVO> consultation;
    List<InpatientEvaluationCreateReqVO> evaluation;
    List<InpatientRoundCreateReqVO> round;
}
