package com.sqlapk.medicine.module.system.dal.dataobject.helporder;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 工单 DO
 *
 * @author sqlapk
 */
@TableName("help_orders")
@KeySequence("help_orders_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpOrderDO extends BaseDO {

    /**
     * 工单编号
     */
    @TableId
    private Long id;

    /**
     * 患者编号
     */
    private Long patientId;

    /**
     * 分配药师编号
     */
    private Long assignedPharmacistId;

    /**
     * 分配医生编号
     */
    private Long assignedDoctorId;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单描述
     */
    private String description;

    /**
     * 意向药品
     */
    private String intendedDrugs;

    /**
     * 附件列表
     */
    private String attachments;

    /**
     * 是否需要医生
     */
    private Boolean toDoctor;

    /**
     * 工单状态
     */
    private HelpOrderStatus status;

    /**
     * 分配时间
     */
    private LocalDateTime assignedAt;

    /**
     * 升级时间
     */
    private LocalDateTime escalatedAt;

    /**
     * 处方就绪时间
     */
    private LocalDateTime prescriptionReadyAt;

    /**
     * 完成时间
     */
    private LocalDateTime doneAt;

    /**
     * 驳回时间
     */
    private LocalDateTime rejectedAt;

    /**
     * 驳回人
     */
    private Long rejectedBy;

    /**
     * 驳回原因
     */
    private String rejectReason;

}

