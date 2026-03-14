package com.medicine.repository.identity;

import com.medicine.domain.identity.PatientProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatientProfileMapper {

    @Select("""
            SELECT id, patient_id, contact_mobile, id_type, ethnicity, height_cm, weight_kg, blood_type,
                   allergies, avatar_url, address, patient_type, insurance, emergency_contact_name,
                   emergency_contact_phone, emergency_contact_relationship, identity_material_hash, real_name_verified,
                   real_name_verified_time, creator, create_time, updater, update_time, deleted, tenant_id
            FROM patient_profile
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0
            LIMIT 1
            """)
    PatientProfile selectByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Insert("""
            INSERT INTO patient_profile (
                patient_id, contact_mobile, id_type, ethnicity, height_cm, weight_kg, blood_type,
                allergies, avatar_url, address, patient_type, insurance, emergency_contact_name,
                emergency_contact_phone, emergency_contact_relationship, identity_material_hash, real_name_verified,
                real_name_verified_time, creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{contactMobile}, #{idType}, #{ethnicity}, #{heightCm}, #{weightKg}, #{bloodType},
                #{allergies}, #{avatarUrl}, #{address}, #{patientType}, #{insurance}, #{emergencyContactName},
                #{emergencyContactPhone}, #{emergencyContactRelationship}, #{identityMaterialHash}, #{realNameVerified},
                #{realNameVerifiedTime}, #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            ON DUPLICATE KEY UPDATE
                contact_mobile = VALUES(contact_mobile),
                id_type = VALUES(id_type),
                ethnicity = VALUES(ethnicity),
                height_cm = VALUES(height_cm),
                weight_kg = VALUES(weight_kg),
                blood_type = VALUES(blood_type),
                allergies = VALUES(allergies),
                avatar_url = VALUES(avatar_url),
                address = VALUES(address),
                patient_type = VALUES(patient_type),
                insurance = VALUES(insurance),
                emergency_contact_name = VALUES(emergency_contact_name),
                emergency_contact_phone = VALUES(emergency_contact_phone),
                emergency_contact_relationship = VALUES(emergency_contact_relationship),
                identity_material_hash = VALUES(identity_material_hash),
                real_name_verified = VALUES(real_name_verified),
                real_name_verified_time = VALUES(real_name_verified_time),
                updater = VALUES(updater),
                update_time = VALUES(update_time),
                deleted = b'0'
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(PatientProfile profile);
}
