package com.medicine.service.notification;

import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.JwtTokenService;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminNotificationWebSocketAuthenticator {

    private final JwtTokenService jwtTokenService;
    private final ProfessionalIdentityService professionalIdentityService;

    public AdminNotificationWebSocketAuthenticator(
            JwtTokenService jwtTokenService,
            ProfessionalIdentityService professionalIdentityService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.professionalIdentityService = professionalIdentityService;
    }

    public SessionTarget authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        JwtClaims claims = jwtTokenService.parse(token.trim());
        if (claims == null || !claims.isProfessional()) {
            return null;
        }
        if (professionalIdentityService.isPharmacist(claims)) {
            PharmacistProfile profile = professionalIdentityService.getOrCreatePharmacistProfile(claims);
            return new SessionTarget(claims.getTenantId(), "PHARMACIST", profile.getId());
        }
        if (professionalIdentityService.isDoctor(claims)) {
            DoctorProfile profile = professionalIdentityService.getOrCreateDoctorProfile(claims);
            return new SessionTarget(claims.getTenantId(), "DOCTOR", profile.getId());
        }
        return null;
    }

    public record SessionTarget(Long tenantId, String targetType, Long targetId) {
    }
}
