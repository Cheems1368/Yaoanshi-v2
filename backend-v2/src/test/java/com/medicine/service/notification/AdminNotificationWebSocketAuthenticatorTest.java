package com.medicine.service.notification;

import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.JwtTokenService;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationWebSocketAuthenticatorTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @InjectMocks
    private AdminNotificationWebSocketAuthenticator authenticator;

    @Test
    void shouldReturnNullWhenTokenMissing() {
        assertThat(authenticator.authenticate(null)).isNull();
    }

    @Test
    void shouldAuthenticatePharmacistToken() {
        JwtClaims claims = professionalClaims();
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(11L);

        when(jwtTokenService.parse("good-token")).thenReturn(claims);
        when(professionalIdentityService.isPharmacist(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile);

        AdminNotificationWebSocketAuthenticator.SessionTarget target = authenticator.authenticate("good-token");

        assertThat(target).isNotNull();
        assertThat(target.targetType()).isEqualTo("PHARMACIST");
        assertThat(target.targetId()).isEqualTo(11L);
    }

    @Test
    void shouldAuthenticateDoctorToken() {
        JwtClaims claims = professionalClaims();
        DoctorProfile profile = new DoctorProfile();
        profile.setId(22L);

        when(jwtTokenService.parse("doctor-token")).thenReturn(claims);
        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(profile);

        AdminNotificationWebSocketAuthenticator.SessionTarget target = authenticator.authenticate("doctor-token");

        assertThat(target).isNotNull();
        assertThat(target.targetType()).isEqualTo("DOCTOR");
        assertThat(target.targetId()).isEqualTo(22L);
    }

    private JwtClaims professionalClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("pharmacists"));
        return claims;
    }
}
