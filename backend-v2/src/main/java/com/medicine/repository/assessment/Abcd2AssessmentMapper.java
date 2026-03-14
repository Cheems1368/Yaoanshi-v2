package com.medicine.repository.assessment;

import com.medicine.domain.assessment.Abcd2Assessment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Abcd2AssessmentMapper {

    @Select("""
            SELECT id, patient_id, source_type, source_ref_id, raw_input_json, score, risk_level, calculated_at, calculated_by_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM abcd2_assessment
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0
            ORDER BY calculated_at DESC, id DESC
            LIMIT 1
            """)
    Abcd2Assessment selectLatestByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Insert("""
            INSERT INTO abcd2_assessment (
                patient_id, source_type, source_ref_id, raw_input_json, score, risk_level, calculated_at, calculated_by_id,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{sourceType}, #{sourceRefId}, #{rawInputJson}, #{score}, #{riskLevel}, #{calculatedAt}, #{calculatedById},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Abcd2Assessment assessment);
}
