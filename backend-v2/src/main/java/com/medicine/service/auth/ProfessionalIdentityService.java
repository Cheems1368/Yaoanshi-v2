package com.medicine.service.auth;

import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ProfessionalIdentityService {

    private static final List<String> PHARMACIST_ROLE_CODES = List.of(
            "pharmacists",
            "pharmacist",
            "super_admin",
            "tenant_admin",
            "admin"
    );
    private static final List<String> DOCTOR_ROLE_CODES = List.of(
            "doctor",
            "doctors",
            "super_admin",
            "tenant_admin",
            "admin"
    );

    private final ProfessionalIdentityRepository professionalIdentityRepository;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final DoctorProfileMapper doctorProfileMapper;

    public ProfessionalIdentityService(
            ProfessionalIdentityRepository professionalIdentityRepository,
            PharmacistProfileMapper pharmacistProfileMapper,
            DoctorProfileMapper doctorProfileMapper
    ) {
        this.professionalIdentityRepository = professionalIdentityRepository;
        this.pharmacistProfileMapper = pharmacistProfileMapper;
        this.doctorProfileMapper = doctorProfileMapper;
    }

    public PharmacistProfile getOrCreatePharmacistProfile(JwtClaims claims) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null || claims.getUserId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
        if (!isPharmacist(claims)) {
            throw new BusinessException(
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getCode(),
                    ErrorCode.PATIENT_MANAGEMENT_PERMISSION_DENIED.getMessage()
            );
        }

        PharmacistProfile existing = pharmacistProfileMapper.selectBySystemUserId(claims.getTenantId(), claims.getUserId());
        if (existing != null) {
            return existing;
        }

        SystemUser systemUser = professionalIdentityRepository.selectActiveSystemUser(claims.getTenantId(), claims.getUserId());
        if (systemUser == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        PharmacistProfile profile = new PharmacistProfile();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        profile.setSystemUserId(systemUser.getId());
        profile.setRealName(defaultRealName(systemUser));
        profile.setMobile(systemUser.getMobile());
        profile.setServiceStatus("ACTIVE");
        profile.setCreator("admin-auth");
        profile.setCreateTime(now);
        profile.setUpdater("admin-auth");
        profile.setUpdateTime(now);
        profile.setDeleted(false);
        profile.setTenantId(claims.getTenantId());
        pharmacistProfileMapper.insert(profile);
        return profile;
    }

    public DoctorProfile getOrCreateDoctorProfile(JwtClaims claims) {
        validateProfessional(claims);
        if (!isDoctor(claims)) {
            throw new BusinessException(
                    ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(),
                    ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage()
            );
        }

        DoctorProfile existing = doctorProfileMapper.selectBySystemUserId(claims.getTenantId(), claims.getUserId());
        if (existing != null) {
            return existing;
        }

        SystemUser systemUser = professionalIdentityRepository.selectActiveSystemUser(claims.getTenantId(), claims.getUserId());
        if (systemUser == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        DoctorProfile profile = new DoctorProfile();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        profile.setSystemUserId(systemUser.getId());
        profile.setRealName(defaultRealName(systemUser));
        profile.setMobile(systemUser.getMobile());
        profile.setServiceStatus("ACTIVE");
        profile.setCreator("admin-auth");
        profile.setCreateTime(now);
        profile.setUpdater("admin-auth");
        profile.setUpdateTime(now);
        profile.setDeleted(false);
        profile.setTenantId(claims.getTenantId());
        doctorProfileMapper.insert(profile);
        return profile;
    }

    public boolean isPharmacist(JwtClaims claims) {
        return claims != null
                && claims.getRoles() != null
                && claims.getRoles().stream().anyMatch(role -> PHARMACIST_ROLE_CODES.contains(role == null ? "" : role.toLowerCase()));
    }

    public boolean isDoctor(JwtClaims claims) {
        return claims != null
                && claims.getRoles() != null
                && claims.getRoles().stream().anyMatch(role -> DOCTOR_ROLE_CODES.contains(role == null ? "" : role.toLowerCase()));
    }

    private void validateProfessional(JwtClaims claims) {
        if (claims == null || !claims.isProfessional() || claims.getTenantId() == null || claims.getUserId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
    }

    private String defaultRealName(SystemUser systemUser) {
        if (systemUser.getNickname() != null && !systemUser.getNickname().isBlank()) {
            return systemUser.getNickname();
        }
        return systemUser.getUsername();
    }
}
