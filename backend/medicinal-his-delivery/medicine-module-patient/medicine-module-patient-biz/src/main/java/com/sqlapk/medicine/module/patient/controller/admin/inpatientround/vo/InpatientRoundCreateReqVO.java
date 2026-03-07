package com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 住院药学查房记录创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientRoundCreateReqVO extends InpatientRoundBaseVO {

}
