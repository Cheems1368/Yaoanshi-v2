package com.medicine.repository.identity;

import com.medicine.domain.identity.PatientAdminListRow;
import com.medicine.domain.identity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PatientMapper {

    @Select("""
            SELECT id, app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                   primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                   binding_status, bound_by_pharmacist_id, bound_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient
            WHERE id = #{id} AND deleted = 0
            """)
    Patient selectById(Long id);

    @Select("""
            SELECT id, app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                   primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                   binding_status, bound_by_pharmacist_id, bound_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    Patient selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id
            FROM patient
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            FOR UPDATE
            """)
    Long lockByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                   primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                   binding_status, bound_by_pharmacist_id, bound_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient
            WHERE tenant_id = #{tenantId} AND app_account_id = #{appAccountId} AND deleted = 0
            LIMIT 1
            """)
    Patient selectByAppAccountId(@Param("tenantId") Long tenantId, @Param("appAccountId") Long appAccountId);

    @Select("""
            SELECT id, app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                   primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                   binding_status, bound_by_pharmacist_id, bound_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient
            WHERE tenant_id = #{tenantId} AND mobile = #{mobile} AND hospital_patient_id = #{hospitalPatientId} AND deleted = 0
            LIMIT 1
            """)
    Patient selectByMobileAndHospitalPatientId(
            @Param("tenantId") Long tenantId,
            @Param("mobile") String mobile,
            @Param("hospitalPatientId") String hospitalPatientId
    );

    @Select("""
            SELECT id, app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                   primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                   binding_status, bound_by_pharmacist_id, bound_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient
            WHERE tenant_id = #{tenantId} AND hospital_patient_id = #{hospitalPatientId} AND deleted = 0
            LIMIT 1
            """)
    Patient selectByHospitalPatientId(@Param("tenantId") Long tenantId, @Param("hospitalPatientId") String hospitalPatientId);

    @Select("""
            <script>
            SELECT p.id AS id,
                   p.name AS name,
                   p.mobile AS mobile,
                   p.hospital_patient_id AS hospitalPatientId,
                   p.binding_status AS bindingStatus,
                   p.primary_disease_name AS primaryDiseaseName,
                   p.gender AS gender,
                   p.age AS age,
                   p.drug_allergy AS drugAllergy,
                   p.food_allergy AS foodAllergy,
                   p.medical_history_json AS medicalHistoryJson,
                   p.bound_by_pharmacist_id AS boundByPharmacistId,
                   pp.real_name AS responsiblePharmacistName,
                   (
                       SELECT COUNT(1)
                       FROM patient_family_binding fb
                       WHERE fb.tenant_id = p.tenant_id
                         AND fb.patient_id = p.id
                         AND fb.status = 'ACTIVE'
                         AND fb.deleted = 0
                   ) AS familyBindingCount
            FROM patient p
            LEFT JOIN pharmacist_profile pp
              ON pp.id = p.bound_by_pharmacist_id
             AND pp.deleted = 0
            WHERE p.tenant_id = #{tenantId}
              AND p.deleted = 0
              AND (p.bound_by_pharmacist_id IS NULL OR p.bound_by_pharmacist_id = #{pharmacistProfileId})
            <if test="keyword != null and keyword != ''">
              AND (
                   p.name LIKE CONCAT('%', #{keyword}, '%')
                OR p.mobile LIKE CONCAT('%', #{keyword}, '%')
                OR p.hospital_patient_id LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY CASE WHEN p.bound_by_pharmacist_id IS NULL THEN 0 ELSE 1 END, p.update_time DESC, p.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<PatientAdminListRow> searchForPharmacist(
            @Param("tenantId") Long tenantId,
            @Param("pharmacistProfileId") Long pharmacistProfileId,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM patient p
            WHERE p.tenant_id = #{tenantId}
              AND p.deleted = 0
              AND (p.bound_by_pharmacist_id IS NULL OR p.bound_by_pharmacist_id = #{pharmacistProfileId})
            <if test="keyword != null and keyword != ''">
              AND (
                   p.name LIKE CONCAT('%', #{keyword}, '%')
                OR p.mobile LIKE CONCAT('%', #{keyword}, '%')
                OR p.hospital_patient_id LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    long countForPharmacist(
            @Param("tenantId") Long tenantId,
            @Param("pharmacistProfileId") Long pharmacistProfileId,
            @Param("keyword") String keyword
    );

    @Insert("""
            INSERT INTO patient (
                app_account_id, hospital_patient_id, mobile, name, gender, birth_date, age, id_card_masked,
                primary_disease_code, primary_disease_name, drug_allergy, food_allergy, medical_history_json,
                binding_status, bound_by_pharmacist_id, bound_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{appAccountId}, #{hospitalPatientId}, #{mobile}, #{name}, #{gender}, #{birthDate}, #{age}, #{idCardMasked},
                #{primaryDiseaseCode}, #{primaryDiseaseName}, #{drugAllergy}, #{foodAllergy}, #{medicalHistoryJson},
                #{bindingStatus}, #{boundByPharmacistId}, #{boundAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Patient patient);

    @Update("""
            UPDATE patient
            SET app_account_id = #{appAccountId},
                mobile = #{mobile},
                name = #{name},
                binding_status = #{bindingStatus},
                bound_by_pharmacist_id = #{boundByPharmacistId},
                bound_at = #{boundAt},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE id = #{id}
            """)
    int updateBinding(Patient patient);

    @Update("""
            UPDATE patient
            SET name = #{name},
                gender = #{gender},
                birth_date = #{birthDate},
                age = #{age},
                primary_disease_code = #{primaryDiseaseCode},
                primary_disease_name = #{primaryDiseaseName},
                drug_allergy = #{drugAllergy},
                food_allergy = #{foodAllergy},
                medical_history_json = #{medicalHistoryJson},
                bound_by_pharmacist_id = #{boundByPharmacistId},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateForPharmacist(Patient patient);

    @Update("""
            UPDATE patient SET bound_by_pharmacist_id = #{boundByPharmacistId}, updater = #{updater}, update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateResponsiblePharmacist(Patient patient);

    @Update("""
            UPDATE patient
            SET name = #{name},
                gender = #{gender},
                age = #{age},
                id_card_masked = #{idCardMasked},
                drug_allergy = #{drugAllergy},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateAppProfile(Patient patient);

    @Update("""
            UPDATE patient
            SET primary_disease_name = #{primaryDiseaseName},
                drug_allergy = #{drugAllergy},
                medical_history_json = #{medicalHistoryJson},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateAppHealthSummary(Patient patient);
}
