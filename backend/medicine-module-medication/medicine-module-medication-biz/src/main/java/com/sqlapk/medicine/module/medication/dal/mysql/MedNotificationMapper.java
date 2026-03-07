package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedNotificationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药通知 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedNotificationMapper extends BaseMapper<MedNotificationDO> {

    /**
     * 根据患者ID获取未读通知数量
     */
    @Select("SELECT COUNT(*) FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND status = 0 AND deleted = 0")
    Integer countUnreadByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID获取通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND deleted = 0 ORDER BY send_time DESC")
    List<MedNotificationDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID和类型获取通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND type = #{type} AND deleted = 0 ORDER BY send_time DESC")
    List<MedNotificationDO> selectByPatientIdAndType(@Param("patientId") Long patientId,
                                                    @Param("type") Integer type);

    /**
     * 根据患者ID获取未读通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND status = 0 AND deleted = 0 ORDER BY send_time DESC")
    List<MedNotificationDO> selectUnreadByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据待办任务ID获取通知
     */
    @Select("SELECT * FROM med_notifications WHERE todo_id = #{todoId} " +
            "AND deleted = 0 ORDER BY send_time DESC LIMIT 1")
    MedNotificationDO selectByTodoId(@Param("todoId") Long todoId);

    /**
     * 根据计划ID获取通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE plan_id = #{planId} " +
            "AND deleted = 0 ORDER BY send_time DESC")
    List<MedNotificationDO> selectByPlanId(@Param("planId") Long planId);

    /**
     * 根据发送状态获取通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE send_status = #{sendStatus} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<MedNotificationDO> selectBySendStatus(@Param("sendStatus") Integer sendStatus);

    /**
     * 获取需要重试的通知
     */
    @Select("SELECT * FROM med_notifications WHERE send_status = 3 " +
            "AND retry_count < #{maxRetryCount} " +
            "AND next_retry_time <= #{currentTime} " +
            "AND deleted = 0 ORDER BY next_retry_time ASC")
    List<MedNotificationDO> selectRetryNotifications(@Param("maxRetryCount") Integer maxRetryCount,
                                                      @Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据患者ID和日期范围获取通知列表
     */
    @Select("SELECT * FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND send_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY send_time DESC")
    List<MedNotificationDO> selectByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计患者指定类型的通知数量
     */
    @Select("SELECT COUNT(*) FROM med_notifications WHERE patient_id = #{patientId} " +
            "AND type = #{type} AND deleted = 0")
    Integer countByPatientIdAndType(@Param("patientId") Long patientId,
                                    @Param("type") Integer type);

    /**
     * 根据条件查询通知
     */
    default List<MedNotificationDO> selectByCondition(Long patientId, Integer type, 
                                                     Integer status, Integer channel, Integer sendStatus) {
        LambdaQueryWrapper<MedNotificationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, MedNotificationDO::getPatientId, patientId)
               .eq(type != null, MedNotificationDO::getType, type)
               .eq(status != null, MedNotificationDO::getStatus, status)
               .eq(channel != null, MedNotificationDO::getChannel, channel)
               .eq(sendStatus != null, MedNotificationDO::getSendStatus, sendStatus)
               .orderByDesc(MedNotificationDO::getSendTime);
        return selectList(wrapper);
    }

}
