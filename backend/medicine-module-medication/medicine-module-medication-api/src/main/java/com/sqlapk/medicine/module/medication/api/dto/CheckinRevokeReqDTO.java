package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 * 打卡撤销请求 DTO
 *
 * @author sqlapk
 */
@Data
public class CheckinRevokeReqDTO {

    /**
     * 打卡记录ID
     */
    @NotNull(message = "打卡记录ID不能为空")
    private Long checkinId;

    /**
     * 撤销原因
     */
    @NotBlank(message = "撤销原因不能为空")
    private String reason;

    /**
     * 撤销备注
     */
    private String notes;

}
