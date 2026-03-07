package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 居家药学服务更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeUpdateReqVO extends HomeBaseVO {

    @Schema(description = "居家药学服务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11186")
    @NotNull(message = "居家药学服务ID不能为空")
    private Long id;

    List<HomeConsultationCreateReqVO> consultation;
    List<HomeEducationCreateReqVO> education;
    List<HomeEvaluationCreateReqVO> evaluation;
    List<HomeMedicineCreateReqVO> medicine;

}
