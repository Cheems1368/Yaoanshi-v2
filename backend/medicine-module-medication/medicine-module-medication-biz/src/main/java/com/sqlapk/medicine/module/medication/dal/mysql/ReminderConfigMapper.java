package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提醒配置 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface ReminderConfigMapper extends BaseMapper<ReminderConfigDO> {

    /**
     * 根据患者ID查询提醒配置
     *
     * @param patientId 患者ID
     * @return 提醒配置列表
     */
    List<ReminderConfigDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID和类型查询提醒配置
     *
     * @param patientId 患者ID
     * @param type 提醒类型
     * @return 提醒配置
     */
    ReminderConfigDO selectByPatientIdAndType(@Param("patientId") Long patientId, 
                                               @Param("type") Integer type);

    /**
     * 根据类型查询启用的提醒配置
     *
     * @param type 提醒类型
     * @return 提醒配置列表
     */
    List<ReminderConfigDO> selectEnabledByType(@Param("type") Integer type);

}
