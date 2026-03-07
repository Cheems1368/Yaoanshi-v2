package com.sqlapk.medicine.module.patient.dal.dataobject.patient;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 患者档案审核记录 DO
 *
 * @author 管理员
 */
@TableName("patient_audit_record")
@KeySequence("patient_audit_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAuditRecordDO extends BaseDO {

    /**
     * 审核记录ID
     */
    @TableId
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核状态（待审核(0)、已通过(1)、已拒绝(2)、需要修改(3)）
     */
    private Integer auditStatus;

    /**
     * 完整性得分
     */
    private Double completenessScore;

    /**
     * 完整性等级
     */
    private String completenessLevel;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

}
