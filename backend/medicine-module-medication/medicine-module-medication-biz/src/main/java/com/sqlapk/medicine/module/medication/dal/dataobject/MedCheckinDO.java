package com.sqlapk.medicine.module.medication.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 用药打卡记录 DO
 *
 * @author sqlapk
 */
@TableName("med_checkins")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedCheckinDO extends BaseDO {

    /**
     * 打卡记录ID
     */
    @TableId(type = IdType.AUTO)
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
     * 备注
     */
    private String notes;
    
    /**
     * 证据信息(JSON格式)
     */
    private String evidence;
    
    /**
     * 客户端唯一标识（防重复提交）
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
     * 核实时间
     */
    private LocalDateTime verifyTime;
    
    /**
     * 核实备注
     */
    private String verifyNotes;

}
