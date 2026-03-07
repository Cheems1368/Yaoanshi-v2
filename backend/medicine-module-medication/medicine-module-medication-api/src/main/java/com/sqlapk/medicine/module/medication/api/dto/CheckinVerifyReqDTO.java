package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 * 打卡核实请求 DTO
 *
 * @author sqlapk
 */
@Data
public class CheckinVerifyReqDTO {

    /**
     * 打卡记录ID
     */
    @NotNull(message = "打卡记录ID不能为空")
    private Long checkinId;

    /**
     * 核实状态(1-已核实,2-核实失败)
     */
    @NotNull(message = "核实状态不能为空")
    private Integer verifyStatus;

    /**
     * 核实备注
     */
    private String verifyNotes;

}
