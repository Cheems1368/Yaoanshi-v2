package com.sqlapk.medicine.module.patient.controller.admin.surveys.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问卷创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SurveysCreateReqVO extends SurveysBaseVO {

}
