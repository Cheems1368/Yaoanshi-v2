package com.medicine.service.notification;

import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.JwtTokenService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppNotificationWebSocketAuthenticator {

    private final JwtTokenService jwtTokenService;
    private final AppPatientService appPatientService;

    public AppNotificationWebSocketAuthenticator(
            JwtTokenService jwtTokenService,
            AppPatientService appPatientService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.appPatientService = appPatientService;
    }

    public SessionTarget authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        JwtClaims claims = jwtTokenService.parse(token.trim());
        if (claims == null || claims.isProfessional()) {
            return null;
        }
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(claims.getSubject());
        return new SessionTarget("PATIENT", context.patient().getId());
    }

    public record SessionTarget(String targetType, Long targetId) {
    }
}
