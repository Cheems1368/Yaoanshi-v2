package com.medicine.repository.assessment;

import com.medicine.domain.assessment.SatisfactionSurvey;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SatisfactionSurveyMapper {

    @Insert("""
            INSERT INTO satisfaction_survey (
                help_order_id, patient_id, rating, comment, submitted_by_type, submitted_by_id, submitted_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{helpOrderId}, #{patientId}, #{rating}, #{comment}, #{submittedByType}, #{submittedById}, #{submittedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SatisfactionSurvey survey);

    @Select("""
            SELECT id, help_order_id, patient_id, rating, comment, submitted_by_type, submitted_by_id, submitted_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM satisfaction_survey
            WHERE tenant_id = #{tenantId} AND help_order_id = #{helpOrderId} AND deleted = 0
            LIMIT 1
            """)
    SatisfactionSurvey selectByHelpOrderId(@Param("tenantId") Long tenantId, @Param("helpOrderId") Long helpOrderId);

    @Select("SELECT COUNT(1) FROM satisfaction_survey WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0")
    long countByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Select("SELECT AVG(rating) FROM satisfaction_survey WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0")
    BigDecimal averageByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);
}
