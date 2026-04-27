package com.medicine.repository.ddi;

import com.medicine.domain.ddi.DdiRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DdiRuleMapper {

    @Select("""
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND deleted = 0 AND status = 'ACTIVE'
            ORDER BY id ASC
            """)
    List<DdiRule> selectActiveRules(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    DdiRule selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND deleted = 0
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
              </foreach>
            ORDER BY id ASC
            </script>
            """)
    List<DdiRule> selectByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

    @Select("""
            <script>
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (
                   rule_code LIKE CONCAT('%', #{keyword}, '%')
                OR drug_a_name LIKE CONCAT('%', #{keyword}, '%')
                OR drug_b_name LIKE CONCAT('%', #{keyword}, '%')
                OR message LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="severity != null and severity != ''">
              AND severity = #{severity}
            </if>
            ORDER BY update_time DESC, id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<DdiRule> search(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("severity") String severity,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (
                   rule_code LIKE CONCAT('%', #{keyword}, '%')
                OR drug_a_name LIKE CONCAT('%', #{keyword}, '%')
                OR drug_b_name LIKE CONCAT('%', #{keyword}, '%')
                OR message LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="severity != null and severity != ''">
              AND severity = #{severity}
            </if>
            </script>
            """)
    long countSearch(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("severity") String severity
    );

    @Insert("""
            INSERT INTO ddi_rule (
                rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{ruleCode}, #{drugAName}, #{drugBName}, #{severity}, #{message}, #{recommendedAction}, #{status}, #{maintainedByPharmacistId},
                #{mechanismDesc}, #{alternativeRecommendation}, #{referenceSource}, #{patientSummary},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DdiRule rule);

    @Update("""
            UPDATE ddi_rule
            SET drug_a_name = #{drugAName},
                drug_b_name = #{drugBName},
                severity = #{severity},
                message = #{message},
                recommended_action = #{recommendedAction},
                status = #{status},
                maintained_by_pharmacist_id = #{maintainedByPharmacistId},
                mechanism_desc = #{mechanismDesc},
                alternative_recommendation = #{alternativeRecommendation},
                reference_source = #{referenceSource},
                patient_summary = #{patientSummary},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int update(DdiRule rule);
}
