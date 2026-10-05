package com.medicine.repository.medication;

import com.medicine.domain.medication.SelfReportedMedication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SelfReportedMedicationMapper {

    @Insert("""
            INSERT INTO self_reported_medication (
                patient_id, drug_name, dosage, status, notes,
                operator_type, operator_account_id, work_order_id,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{drugName}, #{dosage}, #{status}, #{notes},
                #{operatorType}, #{operatorAccountId}, #{workOrderId},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SelfReportedMedication record);

    @Select("""
            SELECT id, patient_id, drug_name, dosage, status, notes,
                   operator_type, operator_account_id, work_order_id, transferred_plan_item_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM self_reported_medication
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    SelfReportedMedication selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, patient_id, drug_name, dosage, status, notes,
                   operator_type, operator_account_id, work_order_id, transferred_plan_item_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM self_reported_medication
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0
            ORDER BY create_time DESC
            """)
    List<SelfReportedMedication> selectByPatientId(@Param("tenantId") Long tenantId,
                                                    @Param("patientId") Long patientId);

    @Select("""
            SELECT id, patient_id, drug_name, dosage, status, notes,
                   operator_type, operator_account_id, work_order_id, transferred_plan_item_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM self_reported_medication
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId}
              AND status = 'ACTIVE' AND deleted = 0
            ORDER BY create_time DESC
            """)
    List<SelfReportedMedication> selectActiveByPatientId(@Param("tenantId") Long tenantId,
                                                          @Param("patientId") Long patientId);

    @Select("""
            SELECT id, patient_id, drug_name, dosage, status, notes,
                   operator_type, operator_account_id, work_order_id, transferred_plan_item_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM self_reported_medication
            WHERE tenant_id = #{tenantId} AND work_order_id = #{workOrderId} AND deleted = 0
            ORDER BY create_time ASC
            """)
    List<SelfReportedMedication> selectByWorkOrderId(@Param("tenantId") Long tenantId,
                                                      @Param("workOrderId") Long workOrderId);

    @Update("""
            UPDATE self_reported_medication
            SET status = #{status}, work_order_id = #{workOrderId},
                transferred_plan_item_id = #{transferredPlanItemId},
                updater = #{updater}, update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int update(SelfReportedMedication record);

    /** Count PENDING records for a given work order (used to detect if all withdrawn) */
    @Select("""
            SELECT COUNT(*) FROM self_reported_medication
            WHERE tenant_id = #{tenantId} AND work_order_id = #{workOrderId}
              AND status != 'STOPPED' AND deleted = 0
            """)
    int countActiveByWorkOrderId(@Param("tenantId") Long tenantId, @Param("workOrderId") Long workOrderId);
}
