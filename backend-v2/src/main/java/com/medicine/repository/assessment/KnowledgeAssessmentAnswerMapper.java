package com.medicine.repository.assessment;

import com.medicine.domain.assessment.KnowledgeAssessmentAnswer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeAssessmentAnswerMapper {

    @Insert("""
            INSERT INTO knowledge_assessment_answer (
                assessment_id, question_code, question_text_snapshot, selected_option, correct_option, is_correct,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{assessmentId}, #{questionCode}, #{questionTextSnapshot}, #{selectedOption}, #{correctOption}, #{isCorrect},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeAssessmentAnswer answer);

    @Select("""
            SELECT id, assessment_id, question_code, question_text_snapshot, selected_option, correct_option, is_correct,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM knowledge_assessment_answer
            WHERE tenant_id = #{tenantId} AND assessment_id = #{assessmentId} AND deleted = 0
            ORDER BY id ASC
            """)
    List<KnowledgeAssessmentAnswer> selectByAssessmentId(@Param("tenantId") Long tenantId, @Param("assessmentId") Long assessmentId);
}
