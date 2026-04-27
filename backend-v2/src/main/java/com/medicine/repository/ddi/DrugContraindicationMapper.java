package com.medicine.repository.ddi;

import com.medicine.domain.ddi.DrugContraindication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DrugContraindicationMapper {

    @Select("""
            SELECT id, rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary, status,
                   maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
            FROM drug_contraindication
            WHERE tenant_id = #{tenantId} AND deleted = 0 AND status = 'ACTIVE'
            ORDER BY id ASC
            """)
    List<DrugContraindication> selectActiveRules(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary, status,
                   maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
            FROM drug_contraindication
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    DrugContraindication selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT id, rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action,
                   mechanism_desc, alternative_recommendation, reference_source, patient_summary, status,
                   maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
            FROM drug_contraindication
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (
                   rule_code LIKE CONCAT('%', #{keyword}, '%')
                OR drug_name LIKE CONCAT('%', #{keyword}, '%')
                OR condition_value LIKE CONCAT('%', #{keyword}, '%')
                OR message LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="severity != null and severity != ''">
              AND severity = #{severity}
            </if>
            <if test="conditionType != null and conditionType != ''">
              AND condition_type = #{conditionType}
            </if>
            ORDER BY update_time DESC, id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<DrugContraindication> search(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("severity") String severity,
            @Param("conditionType") String conditionType,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM drug_contraindication
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (
                   rule_code LIKE CONCAT('%', #{keyword}, '%')
                OR drug_name LIKE CONCAT('%', #{keyword}, '%')
                OR condition_value LIKE CONCAT('%', #{keyword}, '%')
                OR message LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="severity != null and severity != ''">
              AND severity = #{severity}
            </if>
            <if test="conditionType != null and conditionType != ''">
              AND condition_type = #{conditionType}
            </if>
            </script>
            """)
    long countSearch(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("severity") String severity,
            @Param("conditionType") String conditionType
    );

    @Insert("""
            INSERT INTO drug_contraindication (
                rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action,
                mechanism_desc, alternative_recommendation, reference_source, patient_summary, status,
                maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{ruleCode}, #{drugName}, #{conditionType}, #{conditionValue}, #{severity}, #{message}, #{recommendedAction},
                #{mechanismDesc}, #{alternativeRecommendation}, #{referenceSource}, #{patientSummary}, #{status},
                #{maintainedByPharmacistId}, #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DrugContraindication rule);

    @Update("""
            UPDATE drug_contraindication
            SET drug_name = #{drugName},
                condition_type = #{conditionType},
                condition_value = #{conditionValue},
                severity = #{severity},
                message = #{message},
                recommended_action = #{recommendedAction},
                mechanism_desc = #{mechanismDesc},
                alternative_recommendation = #{alternativeRecommendation},
                reference_source = #{referenceSource},
                patient_summary = #{patientSummary},
                status = #{status},
                maintained_by_pharmacist_id = #{maintainedByPharmacistId},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int update(DrugContraindication rule);
}
