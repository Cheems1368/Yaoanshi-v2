package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationDoseTask;
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
public interface MedicationDoseTaskMapper {

    @Insert("""
            INSERT INTO medication_dose_task (
                patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                drug_name, dosage, frequency_code, route, meal_relation, instructions,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{planId}, #{planItemId}, #{taskDate}, #{scheduledAt}, #{status}, #{notificationStatus}, #{lastEffectiveCheckinId}, #{lockedAfter},
                #{drugName}, #{dosage}, #{frequencyCode}, #{route}, #{mealRelation}, #{instructions},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationDoseTask task);

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    MedicationDoseTask selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId}
              AND patient_id = #{patientId}
              AND task_date = #{taskDate}
              AND deleted = 0
            ORDER BY scheduled_at ASC, id ASC
            """)
    List<MedicationDoseTask> selectTodayByPatientId(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("taskDate") LocalDate taskDate
    );

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId}
              AND plan_id = #{planId}
              AND task_date BETWEEN #{dateFrom} AND #{dateTo}
              AND deleted = 0
            ORDER BY scheduled_at ASC, id ASC
            """)
    List<MedicationDoseTask> selectByPlanIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("planId") Long planId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId}
              AND patient_id = #{patientId}
              AND task_date BETWEEN #{dateFrom} AND #{dateTo}
              AND deleted = 0
            ORDER BY scheduled_at DESC, id DESC
            """)
    List<MedicationDoseTask> selectByPatientIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Update("""
            UPDATE medication_dose_task
            SET status = #{status},
                notification_status = #{notificationStatus},
                last_effective_checkin_id = #{lastEffectiveCheckinId},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateStatus(MedicationDoseTask task);

    @Update("""
            UPDATE medication_dose_task
            SET deleted = b'1',
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId}
              AND plan_id = #{planId}
              AND task_date >= #{fromDate}
              AND deleted = 0
            """)
    int softDeleteFutureTasks(
            @Param("tenantId") Long tenantId,
            @Param("planId") Long planId,
            @Param("fromDate") LocalDate fromDate,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}
