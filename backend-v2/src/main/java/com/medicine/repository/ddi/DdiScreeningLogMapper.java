package com.medicine.repository.ddi;

import com.medicine.domain.ddi.DdiScreeningLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DdiScreeningLogMapper {

    @Insert("""
            INSERT INTO ddi_screening_log (
                patient_id, prescription_id, medication_plan_id, matched_rule_id,
                rule_code, rule_status_at_screening, drug_a_name, drug_b_name,
                severity, message, recommended_action, mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                confirm_status,
                confirmed_by_pharmacist_id, confirmed_at, confirmation_note,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{prescriptionId}, #{medicationPlanId}, #{matchedRuleId},
                #{ruleCode}, #{ruleStatusAtScreening}, #{drugAName}, #{drugBName},
                #{severity}, #{message}, #{recommendedAction}, #{mechanismDesc}, #{alternativeRecommendation}, #{referenceSource}, #{patientSummary},
                #{confirmStatus},
                #{confirmedByPharmacistId}, #{confirmedAt}, #{confirmationNote},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DdiScreeningLog log);

    @Select("""
            SELECT id, patient_id, prescription_id, medication_plan_id, matched_rule_id,
                   rule_code, rule_status_at_screening, drug_a_name, drug_b_name,
                   severity, message, recommended_action, mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   confirm_status,
                   confirmed_by_pharmacist_id, confirmed_at, confirmation_note,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_screening_log
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND deleted = 0
            ORDER BY id ASC
            """)
    List<DdiScreeningLog> selectByPrescriptionId(@Param("tenantId") Long tenantId, @Param("prescriptionId") Long prescriptionId);

    @Select("""
            SELECT id, patient_id, prescription_id, medication_plan_id, matched_rule_id,
                   rule_code, rule_status_at_screening, drug_a_name, drug_b_name,
                   severity, message, recommended_action, mechanism_desc, alternative_recommendation, reference_source, patient_summary,
                   confirm_status,
                   confirmed_by_pharmacist_id, confirmed_at, confirmation_note,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_screening_log
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    DdiScreeningLog selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Update("""
            UPDATE ddi_screening_log
            SET confirm_status = 'ABORTED',
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND confirm_status = 'PENDING_CONFIRM' AND deleted = 0
            """)
    int abortPendingByPrescriptionId(
            @Param("tenantId") Long tenantId,
            @Param("prescriptionId") Long prescriptionId,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Update("""
            UPDATE ddi_screening_log
            SET confirm_status = 'CONFIRMED_CONTINUE',
                confirmed_by_pharmacist_id = #{pharmacistId},
                confirmed_at = #{confirmedAt},
                confirmation_note = #{note},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int confirm(
            @Param("tenantId") Long tenantId,
            @Param("id") Long id,
            @Param("pharmacistId") Long pharmacistId,
            @Param("note") String note,
            @Param("confirmedAt") LocalDateTime confirmedAt,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}
