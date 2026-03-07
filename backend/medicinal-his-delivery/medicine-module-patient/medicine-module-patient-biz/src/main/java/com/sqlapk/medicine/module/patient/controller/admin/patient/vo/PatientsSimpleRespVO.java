package com.sqlapk.medicine.module.patient.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author frankjlli
 * @date 2023/9/17
 **/
@Schema(description = "管理后台 - 用户精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientsSimpleRespVO {

    @Schema(description = "患者编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "患者姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "小明")
    private String nickname;

    private Long userId;

    private Integer age;

    private Integer sex;

    private Short insurance;

    private String phone;

    private String address;

    private String insuranceCardNumber;

    private LocalDateTime birthday;

}
