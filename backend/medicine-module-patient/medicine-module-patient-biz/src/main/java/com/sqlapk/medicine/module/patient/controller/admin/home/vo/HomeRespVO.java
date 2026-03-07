package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 居家药学服务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeRespVO extends HomeBaseVO {

    @Schema(description = "居家药学服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11186")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    List<HomeConsultationRespVO> consultation;
    List<HomeEducationRespVO> education;
    List<HomeEvaluationRespVO> evaluation;
    List<HomeMedicineRespVO> medicine;

}
