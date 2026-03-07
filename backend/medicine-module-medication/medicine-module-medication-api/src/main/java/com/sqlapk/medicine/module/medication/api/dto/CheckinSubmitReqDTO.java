package com.sqlapk.medicine.module.medication.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡提交请求 DTO
 *
 * @author sqlapk
 */
@Data
public class CheckinSubmitReqDTO {

    /**
     * 待办任务ID
     */
    @NotNull(message = "待办任务ID不能为空")
    private Long todoId;

    /**
     * 打卡时间
     */
    private LocalDateTime checkinTime;

    /**
     * 打卡状态(1-已完成,2-已跳过)
     */
    @NotNull(message = "打卡状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    private String notes;

    /**
     * 证据信息
     */
    private EvidenceDTO evidence;

    /**
     * 客户端唯一标识（防重复提交）
     */
    @NotBlank(message = "客户端唯一标识不能为空")
    private String clientNonce;

    /**
     * 证据信息 DTO
     */
    @Data
    public static class EvidenceDTO {
        /**
         * 图片证据列表
         */
        private List<String> images;

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
