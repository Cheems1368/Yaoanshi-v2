package com.sqlapk.medicine.module.patient.controller.admin.questions.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 问卷问题创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionsCreateReqVO extends QuestionsBaseVO {

}
