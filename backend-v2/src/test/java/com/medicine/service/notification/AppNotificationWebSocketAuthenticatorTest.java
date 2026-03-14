package com.medicine.service.notification;

import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppNotificationWebSocketAuthenticatorTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AppPatientService appPatientService;

    @InjectMocks
    private AppNotificationWebSocketAuthenticator authenticator;

    @Test
    void shouldReturnNullWhenTokenMissing() {
        assertThat(authenticator.authenticate(null)).isNull();
    }

    @Test
    void shouldAuthenticateBoundPatientToken() {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("13800138000");
        claims.setAccountType("APP");

        AppAccount account = new AppAccount();
        account.setId(101L);
        Patient patient = new Patient();
        patient.setId(201L);

        when(jwtTokenService.parse("good-token")).thenReturn(claims);
        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(account, patient, "PATIENT"));

        AppNotificationWebSocketAuthenticator.SessionTarget target = authenticator.authenticate("good-token");

        assertThat(target).isNotNull();
        assertThat(target.targetType()).isEqualTo("PATIENT");
        assertThat(target.targetId()).isEqualTo(201L);
    }
}
