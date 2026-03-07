package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationCheckinRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MedicationCheckinRecordMapper {

    @Insert("""
            INSERT INTO medication_checkin_record (
                dose_task_id, patient_id, operator_type, operator_id, result, actual_taken_at, note, skip_reason,
                is_correction, supersedes_record_id, is_effective,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{doseTaskId}, #{patientId}, #{operatorType}, #{operatorId}, #{result}, #{actualTakenAt}, #{note}, #{skipReason},
                #{isCorrection}, #{supersedesRecordId}, #{isEffective},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationCheckinRecord record);

    @Select("""
            SELECT id, dose_task_id, patient_id, operator_type, operator_id, result, actual_taken_at, note, skip_reason,
                   is_correction, supersedes_record_id, is_effective,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_checkin_record
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    MedicationCheckinRecord selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, dose_task_id, patient_id, operator_type, operator_id, result, actual_taken_at, note, skip_reason,
                   is_correction, supersedes_record_id, is_effective,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_checkin_record
            WHERE tenant_id = #{tenantId}
              AND dose_task_id = #{doseTaskId}
              AND deleted = 0
            ORDER BY create_time DESC, id DESC
            """)
    List<MedicationCheckinRecord> selectByDoseTaskId(@Param("tenantId") Long tenantId, @Param("doseTaskId") Long doseTaskId);

    @Select("""
            SELECT r.id, r.dose_task_id, r.patient_id, r.operator_type, r.operator_id, r.result, r.actual_taken_at, r.note, r.skip_reason,
                   r.is_correction, r.supersedes_record_id, r.is_effective,
                   r.creator, r.create_time, r.updater, r.update_time, r.deleted, r.tenant_id
            FROM medication_checkin_record r
            INNER JOIN medication_dose_task t ON t.id = r.dose_task_id AND t.deleted = 0
            WHERE r.tenant_id = #{tenantId}
              AND r.patient_id = #{patientId}
              AND t.task_date BETWEEN #{dateFrom} AND #{dateTo}
              AND r.deleted = 0
            ORDER BY r.create_time DESC, r.id DESC
            """)
    List<MedicationCheckinRecord> selectByPatientIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Update("""
            UPDATE medication_checkin_record
            SET is_effective = b'0',
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND dose_task_id = #{doseTaskId} AND is_effective = b'1' AND deleted = 0
            """)
    int markIneffectiveByDoseTaskId(
            @Param("tenantId") Long tenantId,
            @Param("doseTaskId") Long doseTaskId,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}
