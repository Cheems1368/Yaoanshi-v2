package com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo;

import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 处方更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrescriptionsUpdateReqVO extends PrescriptionsBaseVO {

    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22100")
    @NotNull(message = "处方ID不能为空")
    private Long id;

    private List<PrescriptionDetailCreateReqVO> inspection;
    private List<PrescriptionExaminationsCreateReqVO> exam;

}
