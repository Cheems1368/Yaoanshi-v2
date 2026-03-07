package com.sqlapk.medicine.module.patient.controller.admin.home.vo;

import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 居家药学服务创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HomeCreateReqVO extends HomeBaseVO {
    List<HomeConsultationCreateReqVO> consultation;
    List<HomeEducationCreateReqVO> education;
    List<HomeEvaluationCreateReqVO> evaluation;
    List<HomeMedicineCreateReqVO> medicine;
}
