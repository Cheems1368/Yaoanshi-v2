package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 住院药学不合理用药创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InpatientAdviceCreateReqVO extends InpatientAdviceBaseVO {

}
