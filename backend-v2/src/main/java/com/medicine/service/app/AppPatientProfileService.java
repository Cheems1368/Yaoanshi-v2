package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.identity.AppPatientHealthProfileDTO;
import com.medicine.api.dto.identity.AppPatientHealthProfileUpdateRequest;
import com.medicine.api.dto.identity.AppPatientProfileDTO;
import com.medicine.api.dto.identity.AppPatientProfileUpdateRequest;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.identity.PatientProfile;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.identity.PatientMapper;
import com.medicine.repository.identity.PatientProfileMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AppPatientProfileService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final AppPatientService appPatientService;
    private final PatientMapper patientMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final PatientHealthProfileMapper patientHealthProfileMapper;
    private final ObjectMapper objectMapper;

    public AppPatientProfileService(
            AppPatientService appPatientService,
            PatientMapper patientMapper,
            PatientProfileMapper patientProfileMapper,
            PatientHealthProfileMapper patientHealthProfileMapper,
            ObjectMapper objectMapper
    ) {
        this.appPatientService = appPatientService;
        this.patientMapper = patientMapper;
        this.patientProfileMapper = patientProfileMapper;
        this.patientHealthProfileMapper = patientHealthProfileMapper;
        this.objectMapper = objectMapper;
    }

    public AppPatientProfileDTO getProfile(String subject) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Long tenantId = tenantIdOf(context.patient());
        PatientProfile profile = patientProfileMapper.selectByPatientId(tenantId, context.patient().getId());
        AppPatientProfileDTO dto = toProfileDto(context.patient(), profile);
        dto.setOperatorType(context.operatorType());
        return dto;
    }

    public AppPatientProfileDTO updateProfile(String subject, AppPatientProfileUpdateRequest request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Patient patient = context.patient();
        Long tenantId = tenantIdOf(patient);
        PatientProfile profile = patientProfileMapper.selectByPatientId(tenantId, patient.getId());

        if (profile == null) {
            profile = new PatientProfile();
            profile.setPatientId(patient.getId());
            applyAuditFields(profile, "app-patient-profile", tenantId);
        } else {
            touchAuditFields(profile, "app-patient-profile");
        }

        boolean archiveIdentityMaterial = StringUtils.hasText(request.getIdCard())
                || Boolean.TRUE.equals(request.getRealNameVerified())
                || Boolean.TRUE.equals(profile.getRealNameVerified());

        patient.setName(textOrExisting(request.getName(), patient.getName()));
        patient.setGender(textOrExisting(request.getGender(), patient.getGender()));
        patient.setAge(numberOrExisting(request.getAge(), patient.getAge()));
        if (archiveIdentityMaterial) {
            patient.setIdCardMasked(null);
        }
        if (StringUtils.hasText(request.getAllergies())) {
            patient.setDrugAllergy(request.getAllergies().trim());
        }
        patient.setUpdater("app-patient-profile");
        patient.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        patientMapper.updateAppProfile(patient);

        profile.setContactMobile(textOrExisting(request.getPhone(), fallbackText(profile.getContactMobile(), patient.getMobile())));
        profile.setIdType(textOrExisting(request.getIdType(), profile.getIdType()));
        profile.setEthnicity(textOrExisting(request.getEthnicity(), profile.getEthnicity()));
        profile.setHeightCm(numberOrExisting(request.getHeight(), profile.getHeightCm()));
        profile.setWeightKg(numberOrExisting(request.getWeight(), profile.getWeightKg()));
        profile.setBloodType(textOrExisting(request.getBloodType(), profile.getBloodType()));
        profile.setAllergies(textOrExisting(request.getAllergies(), fallbackText(profile.getAllergies(), patient.getDrugAllergy())));
        profile.setAvatarUrl(textOrExisting(request.getAvatar(), profile.getAvatarUrl()));
        profile.setAddress(textOrExisting(request.getAddress(), profile.getAddress()));
        profile.setPatientType(numberOrExisting(request.getPatientType(), profile.getPatientType()));
        profile.setInsurance(numberOrExisting(request.getInsurance(), profile.getInsurance()));
        profile.setRealNameVerified(booleanOrExisting(request.getRealNameVerified(), profile.getRealNameVerified()));
        profile.setRealNameVerifiedTime(dateTimeOrExisting(request.getRealNameVerifiedTime(), profile.getRealNameVerifiedTime()));
        if (StringUtils.hasText(request.getIdCard())) {
            profile.setIdentityMaterialHash(sha256Hex(request.getIdCard().trim()));
        }

        AppPatientProfileUpdateRequest.EmergencyContactRequest emergencyContact = request.getEmergencyContact();
        if (emergencyContact != null) {
            profile.setEmergencyContactName(textOrExisting(emergencyContact.getName(), profile.getEmergencyContactName()));
            profile.setEmergencyContactPhone(textOrExisting(emergencyContact.getPhone(), profile.getEmergencyContactPhone()));
            profile.setEmergencyContactRelationship(textOrExisting(emergencyContact.getRelationship(), profile.getEmergencyContactRelationship()));
        }

        patientProfileMapper.upsert(profile);
        return toProfileDto(patient, profile);
    }

    public AppPatientHealthProfileDTO getHealthProfile(String subject) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Long tenantId = tenantIdOf(context.patient());
        PatientHealthProfile profile = patientHealthProfileMapper.selectByPatientId(tenantId, context.patient().getId());
        return toHealthDto(context.patient().getId(), profile);
    }

    public AppPatientHealthProfileDTO updateHealthProfile(String subject, AppPatientHealthProfileUpdateRequest request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        Patient patient = context.patient();
        Long tenantId = tenantIdOf(patient);

        PatientHealthProfile profile = patientHealthProfileMapper.selectByPatientId(tenantId, patient.getId());
        if (profile == null) {
            profile = new PatientHealthProfile();
            profile.setPatientId(patient.getId());
            applyAuditFields(profile, "app-patient-health", tenantId);
        } else {
            touchAuditFields(profile, "app-patient-health");
        }

        AppPatientHealthProfileDTO.Layer1DTO layer1 = request.getLayer1() == null ? new AppPatientHealthProfileDTO.Layer1DTO() : request.getLayer1();
        AppPatientHealthProfileDTO.Layer2DTO layer2 = request.getLayer2() == null ? new AppPatientHealthProfileDTO.Layer2DTO() : request.getLayer2();
        AppPatientHealthProfileDTO.Layer3DTO layer3 = request.getLayer3() == null ? new AppPatientHealthProfileDTO.Layer3DTO() : request.getLayer3();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        profile.setLayer1Allergy(normalize(layer1.getAllergy()));
        profile.setLayer1Diagnosis(normalize(layer1.getDiagnosis()));
        profile.setLayer2MedicalHistory(normalize(layer2.getMedicalHistory()));
        profile.setLayer2SmokingDrinking(normalize(layer2.getSmokingDrinking()));
        profile.setLayer2LiverKidney(normalize(layer2.getLiverKidney()));
        profile.setLayer2CurrentMedication(normalize(layer2.getCurrentMedication()));
        profile.setLayer3DetailedSymptoms(normalize(layer3.getDetailedSymptoms()));
        profile.setLayer3FamilyHistory(normalize(layer3.getFamilyHistory()));
        profile.setLayer3SurgeryHistory(normalize(layer3.getSurgeryHistory()));
        profile.setLayer3SocialHistory(normalize(layer3.getSocialHistory()));
        profile.setSubmittedAt(now);

        patientHealthProfileMapper.upsert(profile);

        patient.setPrimaryDiseaseName(profile.getLayer1Diagnosis());
        patient.setDrugAllergy(profile.getLayer1Allergy());
        patient.setMedicalHistoryJson(writeHealthSummaryJson(profile));
        patient.setUpdater("app-patient-health");
        patient.setUpdateTime(now);
        patientMapper.updateAppHealthSummary(patient);

        PatientProfile patientProfile = patientProfileMapper.selectByPatientId(tenantId, patient.getId());
        if (patientProfile == null) {
            patientProfile = new PatientProfile();
            patientProfile.setPatientId(patient.getId());
            patientProfile.setContactMobile(patient.getMobile());
            applyAuditFields(patientProfile, "app-patient-health", tenantId);
        } else {
            touchAuditFields(patientProfile, "app-patient-health");
        }
        if (StringUtils.hasText(profile.getLayer1Allergy())) {
            patientProfile.setAllergies(profile.getLayer1Allergy());
            patientProfileMapper.upsert(patientProfile);
        }

        return toHealthDto(patient.getId(), profile);
    }

    private AppPatientProfileDTO toProfileDto(Patient patient, PatientProfile profile) {
        AppPatientProfileDTO dto = new AppPatientProfileDTO();
        dto.setPatientId(patient.getId());
        dto.setName(patient.getName());
        dto.setPhone(profile == null ? patient.getMobile() : fallbackText(profile.getContactMobile(), patient.getMobile()));
        dto.setAge(patient.getAge());
        dto.setGender(patient.getGender());
        dto.setIdType(profile == null ? null : profile.getIdType());
        dto.setEthnicity(profile == null ? null : profile.getEthnicity());
        dto.setHeight(profile == null ? null : profile.getHeightCm());
        dto.setWeight(profile == null ? null : profile.getWeightKg());
        dto.setBloodType(profile == null ? null : profile.getBloodType());
        dto.setAllergies(profile == null ? patient.getDrugAllergy() : fallbackText(profile.getAllergies(), patient.getDrugAllergy()));
        dto.setAvatar(profile == null ? null : profile.getAvatarUrl());
        dto.setAddress(profile == null ? null : profile.getAddress());
        dto.setPatientType(profile == null ? null : profile.getPatientType());
        dto.setInsurance(profile == null ? null : profile.getInsurance());
        dto.setIdentityMaterialArchived(profile != null && StringUtils.hasText(profile.getIdentityMaterialHash()));
        dto.setRealNameVerified(profile != null && Boolean.TRUE.equals(profile.getRealNameVerified()));
        dto.setRealNameVerifiedTime(profile == null ? null : formatDateTime(profile.getRealNameVerifiedTime()));
        dto.setCreatedAt(formatDateTime(profile == null ? patient.getCreateTime() : profile.getCreateTime()));
        dto.setUpdatedAt(formatDateTime(profile == null ? patient.getUpdateTime() : profile.getUpdateTime()));

        AppPatientProfileDTO.EmergencyContactDTO emergencyContact = new AppPatientProfileDTO.EmergencyContactDTO();
        if (profile != null) {
            emergencyContact.setName(profile.getEmergencyContactName());
            emergencyContact.setPhone(profile.getEmergencyContactPhone());
            emergencyContact.setRelationship(profile.getEmergencyContactRelationship());
        }
        dto.setEmergencyContact(emergencyContact);
        return dto;
    }

    private AppPatientHealthProfileDTO toHealthDto(Long patientId, PatientHealthProfile profile) {
        AppPatientHealthProfileDTO dto = new AppPatientHealthProfileDTO();
        dto.setPatientId(patientId);
        if (profile == null) {
            return dto;
        }

        AppPatientHealthProfileDTO.Layer1DTO layer1 = new AppPatientHealthProfileDTO.Layer1DTO();
        layer1.setAllergy(profile.getLayer1Allergy());
        layer1.setDiagnosis(profile.getLayer1Diagnosis());
        dto.setLayer1(layer1);

        AppPatientHealthProfileDTO.Layer2DTO layer2 = new AppPatientHealthProfileDTO.Layer2DTO();
        layer2.setMedicalHistory(profile.getLayer2MedicalHistory());
        layer2.setSmokingDrinking(profile.getLayer2SmokingDrinking());
        layer2.setLiverKidney(profile.getLayer2LiverKidney());
        layer2.setCurrentMedication(profile.getLayer2CurrentMedication());
        dto.setLayer2(layer2);

        AppPatientHealthProfileDTO.Layer3DTO layer3 = new AppPatientHealthProfileDTO.Layer3DTO();
        layer3.setDetailedSymptoms(profile.getLayer3DetailedSymptoms());
        layer3.setFamilyHistory(profile.getLayer3FamilyHistory());
        layer3.setSurgeryHistory(profile.getLayer3SurgeryHistory());
        layer3.setSocialHistory(profile.getLayer3SocialHistory());
        dto.setLayer3(layer3);
        dto.setSubmitTime(formatDateTime(profile.getSubmittedAt()));
        return dto;
    }

    private String writeHealthSummaryJson(PatientHealthProfile profile) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> layer1 = new LinkedHashMap<>();
        layer1.put("allergy", profile.getLayer1Allergy());
        layer1.put("diagnosis", profile.getLayer1Diagnosis());
        Map<String, Object> layer2 = new LinkedHashMap<>();
        layer2.put("medicalHistory", profile.getLayer2MedicalHistory());
        layer2.put("smokingDrinking", profile.getLayer2SmokingDrinking());
        layer2.put("liverKidney", profile.getLayer2LiverKidney());
        layer2.put("currentMedication", profile.getLayer2CurrentMedication());
        Map<String, Object> layer3 = new LinkedHashMap<>();
        layer3.put("detailedSymptoms", profile.getLayer3DetailedSymptoms());
        layer3.put("familyHistory", profile.getLayer3FamilyHistory());
        layer3.put("surgeryHistory", profile.getLayer3SurgeryHistory());
        layer3.put("socialHistory", profile.getLayer3SocialHistory());
        payload.put("layer1", layer1);
        payload.put("layer2", layer2);
        payload.put("layer3", layer3);
        payload.put("submitTime", formatDateTime(profile.getSubmittedAt()));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize patient health profile", ex);
        }
    }

    private void applyAuditFields(PatientProfile profile, String operator, Long tenantId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        profile.setCreator(operator);
        profile.setCreateTime(now);
        profile.setUpdater(operator);
        profile.setUpdateTime(now);
        profile.setDeleted(false);
        profile.setTenantId(tenantId);
    }

    private void applyAuditFields(PatientHealthProfile profile, String operator, Long tenantId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        profile.setCreator(operator);
        profile.setCreateTime(now);
        profile.setUpdater(operator);
        profile.setUpdateTime(now);
        profile.setDeleted(false);
        profile.setTenantId(tenantId);
    }

    private void touchAuditFields(PatientProfile profile, String operator) {
        profile.setUpdater(operator);
        profile.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
    }

    private void touchAuditFields(PatientHealthProfile profile, String operator) {
        profile.setUpdater(operator);
        profile.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
    }

    private Long tenantIdOf(Patient patient) {
        return patient.getTenantId() == null ? DEFAULT_TENANT_ID : patient.getTenantId();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String textOrExisting(String incoming, String existing) {
        return StringUtils.hasText(incoming) ? incoming.trim() : existing;
    }

    private String fallbackText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private Integer numberOrExisting(Integer incoming, Integer existing) {
        return incoming == null ? existing : incoming;
    }

    private Boolean booleanOrExisting(Boolean incoming, Boolean existing) {
        return incoming == null ? existing : incoming;
    }

    private LocalDateTime dateTimeOrExisting(String incoming, LocalDateTime existing) {
        if (!StringUtils.hasText(incoming)) {
            return existing;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(incoming.trim()), ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            return existing;
        }
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte current : hashed) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }
}
