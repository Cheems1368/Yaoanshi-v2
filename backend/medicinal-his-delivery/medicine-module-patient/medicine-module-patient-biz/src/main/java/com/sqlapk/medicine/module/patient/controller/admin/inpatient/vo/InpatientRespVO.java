package com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 住院药学服务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientRespVO extends InpatientBaseVO {

    @Schema(description = "住院服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30456")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;

    List<InpatientAdviceRespVO> advice;
    List<InpatientEducationRespVO> education;
    List<InpatientConsultationRespVO> consultation;
    List<InpatientEvaluationRespVO> evaluation;
    List<InpatientRoundRespVO> round;

}
