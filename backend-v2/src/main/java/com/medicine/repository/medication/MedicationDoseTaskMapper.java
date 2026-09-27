package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationDoseTask;
import com.medicine.domain.medication.AdherenceDoseRow;
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
                drug_name, dosage, frequency_code, route, meal_relation, instructions, delayed_generated,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{planId}, #{planItemId}, #{taskDate}, #{scheduledAt}, #{status}, #{notificationStatus}, #{lastEffectiveCheckinId}, #{lockedAfter},
                #{drugName}, #{dosage}, #{frequencyCode}, #{route}, #{mealRelation}, #{instructions}, #{delayedGenerated},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            ON DUPLICATE KEY UPDATE id = id
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationDoseTask task);

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions, delayed_generated,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    MedicationDoseTask selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, patient_id, scheduled_at, last_effective_checkin_id, locked_after, status, notification_status, tenant_id
            FROM medication_dose_task
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            FOR UPDATE
            """)
    MedicationDoseTask selectByIdForUpdate(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, patient_id, plan_id, plan_item_id, task_date, scheduled_at, status, notification_status, last_effective_checkin_id, locked_after,
                   drug_name, dosage, frequency_code, route, meal_relation, instructions, delayed_generated,
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
                   drug_name, dosage, frequency_code, route, meal_relation, instructions, delayed_generated,
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
                   drug_name, dosage, frequency_code, route, meal_relation, instructions, delayed_generated,
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

    @Update("""
            UPDATE medication_dose_task
            SET status = 'CANCELLED', notification_status = 'CANCELLED', updater = #{updater}, update_time = #{now}
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND plan_id <> #{newPlanId}
              AND scheduled_at > #{now} AND last_effective_checkin_id IS NULL
              AND status = 'PENDING' AND deleted = 0
            """)
    int cancelFutureTasksForOtherPlans(@Param("tenantId") Long tenantId,
                                       @Param("patientId") Long patientId,
                                       @Param("newPlanId") Long newPlanId,
                                       @Param("now") LocalDateTime now,
                                       @Param("updater") String updater);

    @Update("""
            UPDATE medication_dose_task
            SET status = 'CANCELLED', notification_status = 'CANCELLED', updater = #{updater}, update_time = #{now}
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND plan_id = #{planId}
              AND scheduled_at > #{now} AND last_effective_checkin_id IS NULL
              AND status = 'PENDING' AND deleted = 0
            """)
    int cancelFutureTasksForPlan(@Param("tenantId") Long tenantId,
                                 @Param("patientId") Long patientId,
                                 @Param("planId") Long planId,
                                 @Param("now") LocalDateTime now,
                                 @Param("updater") String updater);

    @Select("""
            SELECT dt.id, dt.patient_id, dt.plan_id, dt.plan_item_id, dt.task_date, dt.scheduled_at,
                   dt.status, dt.notification_status, dt.last_effective_checkin_id, dt.locked_after,
                   dt.drug_name, dt.dosage, dt.frequency_code, dt.route, dt.meal_relation, dt.instructions,
                   dt.delayed_generated, dt.creator, dt.create_time, dt.updater, dt.update_time, dt.deleted, dt.tenant_id
            FROM medication_dose_task dt
            JOIN medication_checkin_record cr ON cr.id = dt.last_effective_checkin_id
              AND cr.tenant_id = dt.tenant_id AND cr.is_effective = b'1' AND cr.deleted = b'0'
            WHERE dt.tenant_id = #{tenantId} AND dt.patient_id = #{patientId}
              AND dt.plan_id = #{planId} AND dt.scheduled_at > #{now}
              AND dt.deleted = b'0' AND cr.result = 'TAKEN'
            ORDER BY dt.scheduled_at, dt.id
            """)
    List<MedicationDoseTask> selectFutureTakenTasks(@Param("tenantId") Long tenantId,
                                                     @Param("patientId") Long patientId,
                                                     @Param("planId") Long planId,
                                                     @Param("now") LocalDateTime now);

    @Select("""
            SELECT dt.scheduled_at, dt.status, cr.result, cr.actual_taken_at
            FROM medication_dose_task dt
            LEFT JOIN medication_checkin_record cr ON cr.id = dt.last_effective_checkin_id
              AND cr.tenant_id = dt.tenant_id AND cr.is_effective = b'1' AND cr.deleted = b'0'
            WHERE dt.tenant_id = #{tenantId} AND dt.patient_id = #{patientId}
              AND (#{planId} IS NULL OR dt.plan_id = #{planId})
              AND dt.task_date BETWEEN #{fromDate} AND #{toDate}
              AND dt.scheduled_at <= #{asOf} AND dt.status <> 'CANCELLED' AND dt.deleted = b'0'
            ORDER BY dt.scheduled_at, dt.id
            """)
    List<AdherenceDoseRow> selectAdherenceRows(@Param("tenantId") Long tenantId,
                                               @Param("patientId") Long patientId,
                                               @Param("planId") Long planId,
                                               @Param("fromDate") LocalDate fromDate,
                                               @Param("toDate") LocalDate toDate,
                                               @Param("asOf") LocalDateTime asOf);
}
