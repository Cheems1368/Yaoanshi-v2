package com.medicine.repository.identity;

import com.medicine.domain.identity.PatientFamilyBinding;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PatientFamilyBindingMapper {

    @Select("""
            SELECT id, patient_id, family_account_id, relationship_type, status, authorized_at, unbound_at, unbound_reason,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient_family_binding
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND status = 'ACTIVE' AND deleted = 0
            ORDER BY authorized_at DESC
            """)
    List<PatientFamilyBinding> selectActiveByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Select("""
            SELECT id, patient_id, family_account_id, relationship_type, status, authorized_at, unbound_at, unbound_reason,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient_family_binding
            WHERE tenant_id = #{tenantId} AND family_account_id = #{familyAccountId} AND status = 'ACTIVE' AND deleted = 0
            ORDER BY authorized_at DESC
            """)
    List<PatientFamilyBinding> selectActiveByFamilyAccountId(@Param("tenantId") Long tenantId, @Param("familyAccountId") Long familyAccountId);

    @Select("""
            SELECT id, patient_id, family_account_id, relationship_type, status, authorized_at, unbound_at, unbound_reason,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient_family_binding
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    PatientFamilyBinding selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT COUNT(1)
            FROM patient_family_binding
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND status = 'ACTIVE' AND deleted = 0
            """)
    int countActiveByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Select("""
            SELECT id, patient_id, family_account_id, relationship_type, status, authorized_at, unbound_at, unbound_reason,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient_family_binding
            WHERE tenant_id = #{tenantId}
              AND patient_id = #{patientId}
              AND family_account_id = #{familyAccountId}
              AND status = 'ACTIVE'
              AND deleted = 0
            LIMIT 1
            """)
    PatientFamilyBinding selectActiveBinding(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("familyAccountId") Long familyAccountId
    );

    @Insert("""
            INSERT INTO patient_family_binding (
                patient_id, family_account_id, relationship_type, status, authorized_at, unbound_at, unbound_reason,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{familyAccountId}, #{relationshipType}, #{status}, #{authorizedAt}, #{unboundAt}, #{unboundReason},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PatientFamilyBinding binding);

    @Update("""
            UPDATE patient_family_binding
            SET status = #{status},
                unbound_at = #{unboundAt},
                unbound_reason = #{unboundReason},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateStatus(PatientFamilyBinding binding);
}
