package com.medicine.repository.assessment;

import com.medicine.domain.assessment.KnowledgeAssessment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeAssessmentMapper {

    @Insert("""
            INSERT INTO knowledge_assessment (
                patient_id, phase, question_set_version, score, max_score, submitted_by_type, submitted_by_id, submitted_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{phase}, #{questionSetVersion}, #{score}, #{maxScore}, #{submittedByType}, #{submittedById}, #{submittedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeAssessment assessment);

    @Select("""
            SELECT id, patient_id, phase, question_set_version, score, max_score, submitted_by_type, submitted_by_id, submitted_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM knowledge_assessment
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    KnowledgeAssessment selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, patient_id, phase, question_set_version, score, max_score, submitted_by_type, submitted_by_id, submitted_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM knowledge_assessment
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0
            ORDER BY submitted_at DESC, id DESC
            """)
    List<KnowledgeAssessment> selectByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);
}
