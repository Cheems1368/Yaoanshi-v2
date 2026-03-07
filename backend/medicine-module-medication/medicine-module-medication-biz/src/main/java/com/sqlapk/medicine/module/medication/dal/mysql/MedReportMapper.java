package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedReportDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药报告 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedReportMapper extends BaseMapper<MedReportDO> {

    /**
     * 根据患者ID和报告类型获取最新报告
     */
    @Select("SELECT * FROM med_reports WHERE patient_id = #{patientId} " +
            "AND report_type = #{reportType} AND deleted = 0 ORDER BY generation_time DESC LIMIT 1")
    MedReportDO selectLatestByPatientIdAndType(@Param("patientId") Long patientId,
                                              @Param("reportType") Integer reportType);

    /**
     * 根据患者ID和日期范围获取报告
     */
    @Select("SELECT * FROM med_reports WHERE patient_id = #{patientId} " +
            "AND start_date = #{startDate} AND end_date = #{endDate} AND deleted = 0")
    MedReportDO selectByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 根据患者ID获取所有报告
     */
    @Select("SELECT * FROM med_reports WHERE patient_id = #{patientId} " +
            "AND deleted = 0 ORDER BY generation_time DESC")
    List<MedReportDO> selectByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID和报告类型获取历史报告
     */
    @Select("SELECT * FROM med_reports WHERE patient_id = #{patientId} " +
            "AND report_type = #{reportType} AND deleted = 0 ORDER BY generation_time DESC")
    List<MedReportDO> selectHistoryByPatientIdAndType(@Param("patientId") Long patientId,
                                                      @Param("reportType") Integer reportType);

    /**
     * 根据状态获取报告列表
     */
    @Select("SELECT * FROM med_reports WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<MedReportDO> selectByStatus(@Param("status") Integer status);

    /**
     * 获取生成中的报告
     */
    @Select("SELECT * FROM med_reports WHERE status = 1 AND deleted = 0 " +
            "AND create_time >= #{startTime} ORDER BY create_time DESC")
    List<MedReportDO> selectGeneratingReports(@Param("startTime") LocalDateTime startTime);

    /**
     * 根据患者ID和报告类型获取指定数量的最新报告
     */
    @Select("SELECT * FROM med_reports WHERE patient_id = #{patientId} " +
            "AND report_type = #{reportType} AND deleted = 0 " +
            "ORDER BY generation_time DESC LIMIT #{limit}")
    List<MedReportDO> selectLatestByPatientIdAndTypeWithLimit(@Param("patientId") Long patientId,
                                                              @Param("reportType") Integer reportType,
                                                              @Param("limit") Integer limit);

    /**
     * 统计患者报告数量
     */
    @Select("SELECT COUNT(*) FROM med_reports WHERE patient_id = #{patientId} " +
            "AND report_type = #{reportType} AND deleted = 0")
    Integer countByPatientIdAndType(@Param("patientId") Long patientId,
                                    @Param("reportType") Integer reportType);

    /**
     * 根据条件查询报告
     */
    default List<MedReportDO> selectByCondition(Long patientId, Integer reportType, 
                                                LocalDate startDate, LocalDate endDate, Integer status) {
        LambdaQueryWrapper<MedReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, MedReportDO::getPatientId, patientId)
               .eq(reportType != null, MedReportDO::getReportType, reportType)
               .ge(startDate != null, MedReportDO::getStartDate, startDate)
               .le(endDate != null, MedReportDO::getEndDate, endDate)
               .eq(status != null, MedReportDO::getStatus, status)
               .orderByDesc(MedReportDO::getGenerationTime);
        return selectList(wrapper);
    }

}
