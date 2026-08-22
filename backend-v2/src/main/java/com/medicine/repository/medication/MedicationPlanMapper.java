package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationPlan;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface MedicationPlanMapper {

    @Insert("""
            INSERT INTO medication_plan (
                patient_id, plan_name, version_no, status, source_type, source_prescription_id, effective_from, effective_to,
                notes, created_by_pharmacist_id, confirmed_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{planName}, #{versionNo}, #{status}, #{sourceType}, #{sourcePrescriptionId}, #{effectiveFrom}, #{effectiveTo},
                #{notes}, #{createdByPharmacistId}, #{confirmedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationPlan plan);

    @Select("""
            SELECT mp.id, mp.patient_id, mp.plan_name, mp.version_no, mp.status, mp.source_type, mp.source_prescription_id,
                   mp.effective_from, mp.effective_to, mp.notes, mp.created_by_pharmacist_id, mp.confirmed_at,
                   mp.creator, mp.create_time, mp.updater, mp.update_time, mp.deleted, mp.tenant_id,
                   p.name AS patient_name
            FROM medication_plan mp
            INNER JOIN patient p ON p.id = mp.patient_id AND p.deleted = 0
            WHERE mp.tenant_id = #{tenantId} AND mp.id = #{id} AND mp.deleted = 0
            LIMIT 1
            """)
    MedicationPlan selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT mp.id, mp.patient_id, mp.plan_name, mp.version_no, mp.status, mp.source_type, mp.source_prescription_id,
                   mp.effective_from, mp.effective_to, mp.notes, mp.created_by_pharmacist_id, mp.confirmed_at,
                   mp.creator, mp.create_time, mp.updater, mp.update_time, mp.deleted, mp.tenant_id,
                   p.name AS patient_name
            FROM medication_plan mp
            INNER JOIN patient p ON p.id = mp.patient_id AND p.deleted = 0
            WHERE mp.tenant_id = #{tenantId}
              AND mp.patient_id = #{patientId}
              AND mp.status = 'ACTIVE'
              AND mp.deleted = 0
            ORDER BY mp.confirmed_at DESC, mp.id DESC
            LIMIT 1
            """)
    MedicationPlan selectActiveByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Select("""
            SELECT mp.id, mp.patient_id, mp.plan_name, mp.version_no, mp.status, mp.source_type, mp.source_prescription_id,
                   mp.effective_from, mp.effective_to, mp.notes, mp.created_by_pharmacist_id, mp.confirmed_at,
                   mp.creator, mp.create_time, mp.updater, mp.update_time, mp.deleted, mp.tenant_id,
                   p.name AS patient_name
            FROM medication_plan mp
            INNER JOIN patient p ON p.id = mp.patient_id AND p.deleted = 0
            WHERE mp.tenant_id = #{tenantId}
              AND mp.patient_id = #{patientId}
              AND mp.deleted = 0
            ORDER BY mp.version_no DESC, mp.id DESC
            LIMIT 1
            """)
    MedicationPlan selectLatestByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Select("""
            SELECT MIN(effective_from)
            FROM medication_plan
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId}
              AND effective_from IS NOT NULL AND confirmed_at IS NOT NULL AND deleted = 0
            """)
    LocalDateTime selectFirstActivatedAt(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Update("""
            UPDATE medication_plan
            SET plan_name = #{planName},
                status = #{status},
                source_type = #{sourceType},
                source_prescription_id = #{sourcePrescriptionId},
                effective_from = #{effectiveFrom},
                effective_to = #{effectiveTo},
                notes = #{notes},
                confirmed_at = #{confirmedAt},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int update(MedicationPlan plan);

    @Update("""
            UPDATE medication_plan
            SET status = #{status},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId}
              AND patient_id = #{patientId}
              AND id <> #{id}
              AND status = 'ACTIVE'
              AND deleted = 0
            """)
    int replaceOtherActivePlans(MedicationPlan plan);
}
