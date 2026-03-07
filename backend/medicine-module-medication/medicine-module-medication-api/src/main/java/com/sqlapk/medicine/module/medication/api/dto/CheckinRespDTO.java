package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡记录响应 DTO
 *
 * @author sqlapk
 */
@Data
public class CheckinRespDTO {

    /**
     * 打卡记录ID
     */
    private Long id;

    /**
     * 待办任务ID
     */
    private Long todoId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 打卡时间
     */
    private LocalDateTime checkinTime;

    /**
     * 打卡状态(1-已完成,2-已跳过,3-已撤销)
     */
    private Integer status;

    /**
     * 打卡状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String notes;

    /**
     * 证据信息
     */
    private EvidenceDTO evidence;

    /**
     * 客户端唯一标识
     */
    private String clientNonce;

    /**
     * 撤销时间
     */
    private LocalDateTime revokeTime;

    /**
     * 撤销原因
     */
    private String revokeReason;

    /**
     * 核实状态(0-未核实,1-已核实,2-核实失败)
     */
    private Integer verifyStatus;

    /**
     * 核实状态名称
     */
    private String verifyStatusName;

    /**
     * 核实时间
     */
    private LocalDateTime verifyTime;

    /**
     * 核实备注
     */
    private String verifyNotes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 证据信息 DTO
     */
    @Data
    public static class EvidenceDTO {
        /**
         * 图片证据列表
         */
        private java.util.List<String> images;

        /**
         * 音频证据
         */
        private String audio;

        /**
         * 其他证据
         */
        private String other;
    }

}
