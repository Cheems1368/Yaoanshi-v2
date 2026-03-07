package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 处方审核创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutpatientCheckCreateReqVO extends OutpatientCheckBaseVO {

}
