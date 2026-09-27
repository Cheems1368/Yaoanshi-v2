package com.medicine.repository.assessment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface AssessmentReminderMapper {
    @Select("SELECT id FROM assessment_reminder WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND phase = #{phase}")
    Long selectId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId, @Param("phase") String phase);

    @Insert("""
            INSERT INTO assessment_reminder (tenant_id, patient_id, phase, created_at)
            VALUES (#{tenantId}, #{patientId}, #{phase}, #{now})
            """)
    int insert(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId,
               @Param("phase") String phase, @Param("now") LocalDateTime now);

    @Update("UPDATE assessment_reminder SET notification_log_id = #{logId} WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND phase = #{phase}")
    int attachLog(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId,
                  @Param("phase") String phase, @Param("logId") Long logId);
}
