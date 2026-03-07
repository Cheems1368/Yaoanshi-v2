package com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine;

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
 * 用药清单 DO
 *
 * @author 管理员
 */
@TableName("patient_home_medicine")
@KeySequence("patient_home_medicine_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeMedicineDO extends BaseDO {

    /**
     * 住院医嘱ID
     */
    @TableId
    private Long id;
    /**
     * 居家药学ID
     */
    private Long homeId;
    /**
     * 药品名称
     */
    private String medicineName;
    /**
     * 医师ID
     */
    private Long doctorId;
    /**
     * 处方医师
     */
    private String doctorName;
    /**
     * 剂量
     */
    private String dosage;
    /**
     * 适应症
     */
    private String indications;
    /**
     * 用法
     */
    private String medicineUsage;
    /**
     * 开始日期
     */
    private LocalDateTime startTime;
    /**
     * 访视时间
     */
    private LocalDateTime endTime;
    /**
     * 特殊说明
     */
    private String comment;

}
