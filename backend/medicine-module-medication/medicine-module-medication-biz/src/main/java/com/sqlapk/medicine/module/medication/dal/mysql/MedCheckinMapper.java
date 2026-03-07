package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.medication.api.dto.CheckinPageReqDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedCheckinDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药打卡记录 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedCheckinMapper extends BaseMapperX<MedCheckinDO> {

    /**
     * 根据待办任务ID获取打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE todo_id = #{todoId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    MedCheckinDO selectByTodoId(@Param("todoId") Long todoId);

    /**
     * 根据患者ID和日期范围获取打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE patient_id = #{patientId} " +
            "AND DATE(checkin_time) BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY checkin_time DESC")
    List<MedCheckinDO> selectByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 根据患者ID和日期获取打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE patient_id = #{patientId} " +
            "AND DATE(checkin_time) = #{taskDate} AND deleted = 0 ORDER BY checkin_time DESC")
    List<MedCheckinDO> selectByPatientIdAndTaskDate(@Param("patientId") Long patientId,
                                                     @Param("taskDate") LocalDate taskDate);

    /**
     * 根据客户端唯一标识获取打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE client_nonce = #{clientNonce} AND deleted = 0")
    MedCheckinDO selectByClientNonce(@Param("clientNonce") String clientNonce);

    /**
     * 统计患者指定日期范围内的打卡次数
     */
    @Select("SELECT COUNT(*) FROM med_checkins WHERE patient_id = #{patientId} " +
            "AND DATE(checkin_time) BETWEEN #{startDate} AND #{endDate} " +
            "AND status = 1 AND deleted = 0")
    Integer countCompletedCheckins(@Param("patientId") Long patientId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * 根据患者ID获取打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE patient_id = #{patientId} AND deleted = 0 ORDER BY checkin_time DESC")
    List<MedCheckinDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 获取最近打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE patient_id = #{patientId} " +
            "AND checkin_time >= #{startTime} AND deleted = 0 ORDER BY checkin_time DESC")
    List<MedCheckinDO> selectRecentCheckins(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计患者指定日期范围内的跳过次数
     */
    @Select("SELECT COUNT(*) FROM med_checkins WHERE patient_id = #{patientId} " +
            "AND DATE(checkin_time) BETWEEN #{startDate} AND #{endDate} " +
            "AND status = 2 AND deleted = 0")
    Integer countSkippedCheckins(@Param("patientId") Long patientId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /**
     * 根据药品名称统计打卡次数
     */
    @Select("SELECT COUNT(*) FROM med_checkins c " +
            "INNER JOIN med_today_tasks t ON c.todo_id = t.id " +
            "INNER JOIN med_plan_items i ON t.plan_item_id = i.id " +
            "WHERE c.patient_id = #{patientId} AND i.drug_name = #{drugName} " +
            "AND DATE(c.checkin_time) BETWEEN #{startDate} AND #{endDate} " +
            "AND c.status = 1 AND c.deleted = 0")
    Integer countCheckinsByDrugName(@Param("patientId") Long patientId,
                                    @Param("drugName") String drugName,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * 获取需要核实的打卡记录
     */
    @Select("SELECT * FROM med_checkins WHERE verify_status = 0 AND deleted = 0 " +
            "AND create_time >= #{startTime} ORDER BY create_time DESC")
    List<MedCheckinDO> selectPendingVerifyCheckins(@Param("startTime") LocalDateTime startTime);

    /**
     * 根据条件查询打卡记录
     */
    default List<MedCheckinDO> selectByCondition(Long patientId, LocalDate startDate, LocalDate endDate, Integer status) {
        LambdaQueryWrapper<MedCheckinDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedCheckinDO::getPatientId, patientId)
               .ge(startDate != null, MedCheckinDO::getCheckinTime, startDate.atStartOfDay())
               .le(endDate != null, MedCheckinDO::getCheckinTime, endDate.atTime(23, 59, 59))
               .eq(status != null, MedCheckinDO::getStatus, status)
               .orderByDesc(MedCheckinDO::getCheckinTime);
        return selectList(wrapper);
    }

    /**
     * 分页查询打卡记录
     */
    default PageResult<MedCheckinDO> selectPage(CheckinPageReqDTO reqDTO) {
        return selectPage(reqDTO, new LambdaQueryWrapperX<MedCheckinDO>()
                .eqIfPresent(MedCheckinDO::getPatientId, reqDTO.getPatientId())
                .eqIfPresent(MedCheckinDO::getTodoId, reqDTO.getPlanId())
                .eqIfPresent(MedCheckinDO::getStatus, reqDTO.getStatus())
                .betweenIfPresent(MedCheckinDO::getCheckinTime, reqDTO.getStartDate(), reqDTO.getEndDate())
                .orderByDesc(MedCheckinDO::getCheckinTime));
    }

}
