package com.medicine.repository.identity;

import com.medicine.domain.identity.PatientHealthProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatientHealthProfileMapper {

    @Select("""
            SELECT id, patient_id, layer1_allergy, layer1_diagnosis, layer2_medical_history,
                   layer2_smoking_drinking, layer2_liver_kidney, layer2_current_medication,
                   layer3_detailed_symptoms, layer3_family_history, layer3_surgery_history,
                   layer3_social_history, submitted_at, creator, create_time, updater, update_time,
                   deleted, tenant_id
            FROM patient_health_profile
            WHERE tenant_id = #{tenantId} AND patient_id = #{patientId} AND deleted = 0
            LIMIT 1
            """)
    PatientHealthProfile selectByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Insert("""
            INSERT INTO patient_health_profile (
                patient_id, layer1_allergy, layer1_diagnosis, layer2_medical_history,
                layer2_smoking_drinking, layer2_liver_kidney, layer2_current_medication,
                layer3_detailed_symptoms, layer3_family_history, layer3_surgery_history,
                layer3_social_history, submitted_at, creator, create_time, updater, update_time,
                deleted, tenant_id
            ) VALUES (
                #{patientId}, #{layer1Allergy}, #{layer1Diagnosis}, #{layer2MedicalHistory},
                #{layer2SmokingDrinking}, #{layer2LiverKidney}, #{layer2CurrentMedication},
                #{layer3DetailedSymptoms}, #{layer3FamilyHistory}, #{layer3SurgeryHistory},
                #{layer3SocialHistory}, #{submittedAt}, #{creator}, #{createTime}, #{updater}, #{updateTime},
                #{deleted}, #{tenantId}
            )
            ON DUPLICATE KEY UPDATE
                layer1_allergy = VALUES(layer1_allergy),
                layer1_diagnosis = VALUES(layer1_diagnosis),
                layer2_medical_history = VALUES(layer2_medical_history),
                layer2_smoking_drinking = VALUES(layer2_smoking_drinking),
                layer2_liver_kidney = VALUES(layer2_liver_kidney),
                layer2_current_medication = VALUES(layer2_current_medication),
                layer3_detailed_symptoms = VALUES(layer3_detailed_symptoms),
                layer3_family_history = VALUES(layer3_family_history),
                layer3_surgery_history = VALUES(layer3_surgery_history),
                layer3_social_history = VALUES(layer3_social_history),
                submitted_at = VALUES(submitted_at),
                updater = VALUES(updater),
                update_time = VALUES(update_time),
                deleted = b'0'
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(PatientHealthProfile profile);
}
