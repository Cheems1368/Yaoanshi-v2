package com.medicine.service.auth;

import com.medicine.api.dto.auth.BindPatientRequest;
import com.medicine.api.dto.identity.AppFamilyBindingDTO;
import com.medicine.api.dto.identity.AppPatientSummaryDTO;
import com.medicine.api.dto.identity.CreateFamilyBindingRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientFamilyBinding;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.PatientFamilyBindingMapper;
import com.medicine.repository.identity.PatientMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AppPatientService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final PatientMapper patientMapper;
    private final AppAccountMapper appAccountMapper;
    private final PatientFamilyBindingMapper patientFamilyBindingMapper;

    public AppPatientService(
            PatientMapper patientMapper,
            AppAccountMapper appAccountMapper,
            PatientFamilyBindingMapper patientFamilyBindingMapper
    ) {
        this.patientMapper = patientMapper;
        this.appAccountMapper = appAccountMapper;
        this.patientFamilyBindingMapper = patientFamilyBindingMapper;
    }

    public AppPatientSummaryDTO bindPatient(String subject, BindPatientRequest request) {
        String mobile = normalize(request.getMobile());
        String hospitalPatientId = normalize(request.getHospitalPatientId());
        String name = normalize(request.getName());

        if (!StringUtils.hasText(subject) || !StringUtils.hasText(mobile) || !StringUtils.hasText(hospitalPatientId) || !StringUtils.hasText(name)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        if (!subject.equals(mobile)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (appAccount == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        Patient currentPatient = patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, appAccount.getId());
        if (currentPatient != null && !hospitalPatientId.equals(currentPatient.getHospitalPatientId())) {
            throw new BusinessException(ErrorCode.PATIENT_BIND_CONFLICT.getCode(), ErrorCode.PATIENT_BIND_CONFLICT.getMessage());
        }

        Patient duplicateHospitalPatient = patientMapper.selectByHospitalPatientId(DEFAULT_TENANT_ID, hospitalPatientId);
        if (duplicateHospitalPatient != null
                && duplicateHospitalPatient.getAppAccountId() != null
                && !appAccount.getId().equals(duplicateHospitalPatient.getAppAccountId())
                && !mobile.equals(duplicateHospitalPatient.getMobile())) {
            throw new BusinessException(
                    ErrorCode.PATIENT_BIND_REQUIRES_PHARMACIST_CONFIRM.getCode(),
                    ErrorCode.PATIENT_BIND_REQUIRES_PHARMACIST_CONFIRM.getMessage()
            );
        }

        Patient patient = patientMapper.selectByMobileAndHospitalPatientId(DEFAULT_TENANT_ID, mobile, hospitalPatientId);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (patient == null) {
            patient = new Patient();
            patient.setAppAccountId(appAccount.getId());
            patient.setHospitalPatientId(hospitalPatientId);
            patient.setMobile(mobile);
            patient.setName(name);
            patient.setBindingStatus("BOUND");
            patient.setBoundAt(now);
            applyAuditFields(patient, "app-patient");
            patientMapper.insert(patient);
        } else {
            patient.setAppAccountId(appAccount.getId());
            patient.setMobile(mobile);
            patient.setName(name);
            patient.setBindingStatus("BOUND");
            patient.setBoundAt(now);
            patient.setUpdater("app-patient");
            patient.setUpdateTime(now);
            patientMapper.updateBinding(patient);
        }

        return toPatientSummary(patient);
    }

    public AppPatientSummaryDTO getCurrentPatient(String subject) {
        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (appAccount == null) {
            return null;
        }

        Patient patient = resolvePatientForAccount(appAccount);
        if (patient == null) {
            return null;
        }

        return toPatientSummary(patient);
    }

    public List<AppFamilyBindingDTO> getFamilyBindings(String subject) {
        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (appAccount == null) {
            return List.of();
        }

        if ("FAMILY".equalsIgnoreCase(appAccount.getAccountType())) {
            return patientFamilyBindingMapper.selectActiveByFamilyAccountId(DEFAULT_TENANT_ID, appAccount.getId())
                    .stream()
                    .map(this::toFamilyBindingSummary)
                    .toList();
        }

        Patient patient = patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, appAccount.getId());
        if (patient == null) {
            return List.of();
        }
        return patientFamilyBindingMapper.selectActiveByPatientId(DEFAULT_TENANT_ID, patient.getId())
                .stream()
                .map(this::toFamilyBindingSummary)
                .toList();
    }

    @Transactional
    public AppFamilyBindingDTO createFamilyBinding(String subject, CreateFamilyBindingRequest request) {
        AppAccount ownerAccount = requireAppAccount(subject);
        if (!"PATIENT".equalsIgnoreCase(ownerAccount.getAccountType())) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        Patient patient = requireOwnedPatient(ownerAccount);
        if (patientFamilyBindingMapper.countActiveByPatientId(DEFAULT_TENANT_ID, patient.getId()) >= 2) {
            throw new BusinessException(400, "FAMILY_BINDING_LIMIT_REACHED");
        }

        String familyMobile = normalize(request.getFamilyMobile());
        if (!isValidMobile(familyMobile)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        AppAccount familyAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, familyMobile);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (familyAccount == null) {
            familyAccount = new AppAccount();
            familyAccount.setMobile(familyMobile);
            familyAccount.setAccountType("FAMILY");
            familyAccount.setStatus("ACTIVE");
            familyAccount.setDisplayName(normalize(request.getFamilyDisplayName()));
            familyAccount.setLastLoginIp("127.0.0.1");
            familyAccount.setLastLoginTime(now);
            applyAuditFields(familyAccount, "app-family");
            appAccountMapper.insert(familyAccount);
        } else {
            familyAccount.setAccountType("FAMILY");
            if (StringUtils.hasText(request.getFamilyDisplayName())) {
                familyAccount.setDisplayName(request.getFamilyDisplayName().trim());
            }
            familyAccount.setLastLoginIp("127.0.0.1");
            familyAccount.setLastLoginTime(now);
            familyAccount.setUpdater("app-family");
            familyAccount.setUpdateTime(now);
            appAccountMapper.updateLoginInfo(familyAccount);
        }

        List<PatientFamilyBinding> familyBindings = patientFamilyBindingMapper.selectActiveByFamilyAccountId(
                DEFAULT_TENANT_ID, familyAccount.getId()
        );
        if (familyBindings.stream().anyMatch(binding -> !patient.getId().equals(binding.getPatientId()))) {
            throw new BusinessException(400, "FAMILY_ACCOUNT_ALREADY_BOUND");
        }

        PatientFamilyBinding existing = patientFamilyBindingMapper.selectActiveBinding(DEFAULT_TENANT_ID, patient.getId(), familyAccount.getId());
        if (existing == null) {
            existing = new PatientFamilyBinding();
            existing.setPatientId(patient.getId());
            existing.setFamilyAccountId(familyAccount.getId());
            existing.setRelationshipType(normalize(request.getRelationshipType()));
            existing.setStatus("ACTIVE");
            existing.setAuthorizedAt(now);
            applyAuditFields(existing, "app-family");
            patientFamilyBindingMapper.insert(existing);
        }

        return toFamilyBindingSummary(existing);
    }

    public boolean unbindFamilyBinding(String subject, Long bindingId) {
        AppAccount ownerAccount = requireAppAccount(subject);
        if (!"PATIENT".equalsIgnoreCase(ownerAccount.getAccountType())) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        Patient patient = requireOwnedPatient(ownerAccount);
        PatientFamilyBinding binding = patientFamilyBindingMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, bindingId);
        if (binding == null || !patient.getId().equals(binding.getPatientId()) || !"ACTIVE".equals(binding.getStatus())) {
            throw new BusinessException(ErrorCode.FAMILY_BINDING_NOT_FOUND.getCode(), ErrorCode.FAMILY_BINDING_NOT_FOUND.getMessage());
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        binding.setStatus("UNBOUND");
        binding.setUnboundAt(now);
        binding.setUnboundReason("patient_unbind");
        binding.setUpdater("app-family");
        binding.setUpdateTime(now);
        patientFamilyBindingMapper.updateStatus(binding);
        return true;
    }

    public AccessContext requireAccessContext(String subject) {
        AppAccount appAccount = requireAppAccount(subject);
        Patient patient = resolvePatientForAccount(appAccount);
        if (patient == null) {
            throw new BusinessException(ErrorCode.PATIENT_BIND_CONFLICT.getCode(), ErrorCode.PATIENT_BIND_CONFLICT.getMessage());
        }
        String operatorType = "FAMILY".equalsIgnoreCase(appAccount.getAccountType()) ? "FAMILY" : "PATIENT";
        return new AccessContext(appAccount, patient, operatorType);
    }

    public AppAccount requireAppAccount(String subject) {
        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (appAccount == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return appAccount;
    }

    private AppPatientSummaryDTO toPatientSummary(Patient patient) {
        AppPatientSummaryDTO dto = new AppPatientSummaryDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setMobile(patient.getMobile());
        dto.setHospitalPatientId(patient.getHospitalPatientId());
        dto.setBindingStatus(patient.getBindingStatus());
        dto.setBound("BOUND".equals(patient.getBindingStatus()));
        dto.setBoundAt(patient.getBoundAt() == null ? null : patient.getBoundAt().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }

    private AppFamilyBindingDTO toFamilyBindingSummary(PatientFamilyBinding binding) {
        AppFamilyBindingDTO dto = new AppFamilyBindingDTO();
        dto.setId(binding.getId());
        dto.setFamilyAccountId(binding.getFamilyAccountId());
        AppAccount familyAccount = appAccountMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, binding.getFamilyAccountId());
        if (familyAccount != null) {
            dto.setDisplayName(familyAccount.getDisplayName());
            dto.setMobileMasked(maskMobile(familyAccount.getMobile()));
        }
        dto.setRelationshipType(binding.getRelationshipType());
        dto.setStatus(binding.getStatus());
        dto.setAuthorizedAt(binding.getAuthorizedAt() == null ? null : binding.getAuthorizedAt().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    private Patient resolvePatientForAccount(AppAccount appAccount) {
        if ("FAMILY".equalsIgnoreCase(appAccount.getAccountType())) {
            List<PatientFamilyBinding> bindings = patientFamilyBindingMapper.selectActiveByFamilyAccountId(DEFAULT_TENANT_ID, appAccount.getId());
            if (bindings.isEmpty()) {
                return null;
            }
            return patientMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, bindings.get(0).getPatientId());
        }
        return patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, appAccount.getId());
    }

    private Patient requireOwnedPatient(AppAccount appAccount) {
        Patient patient = patientMapper.selectByAppAccountId(DEFAULT_TENANT_ID, appAccount.getId());
        if (patient == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND.getCode(), ErrorCode.PATIENT_NOT_FOUND.getMessage());
        }
        return patient;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isValidMobile(String mobile) {
        return StringUtils.hasText(mobile) && mobile.matches("^1\\d{10}$");
    }

    private void applyAuditFields(Patient patient, String operator) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        patient.setCreator(operator);
        patient.setCreateTime(now);
        patient.setUpdater(operator);
        patient.setUpdateTime(now);
        patient.setDeleted(false);
        patient.setTenantId(DEFAULT_TENANT_ID);
    }

    private void applyAuditFields(PatientFamilyBinding binding, String operator) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        binding.setCreator(operator);
        binding.setCreateTime(now);
        binding.setUpdater(operator);
        binding.setUpdateTime(now);
        binding.setDeleted(false);
        binding.setTenantId(DEFAULT_TENANT_ID);
    }

    private void applyAuditFields(AppAccount appAccount, String operator) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        appAccount.setCreator(operator);
        appAccount.setCreateTime(now);
        appAccount.setUpdater(operator);
        appAccount.setUpdateTime(now);
        appAccount.setDeleted(false);
        appAccount.setTenantId(DEFAULT_TENANT_ID);
    }

    public record AccessContext(AppAccount appAccount, Patient patient, String operatorType) {
    }
}
