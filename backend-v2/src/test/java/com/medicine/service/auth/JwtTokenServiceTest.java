package com.medicine.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.ProfessionalTokenRecord;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private ProfessionalIdentityRepository professionalIdentityRepository;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("backend-v2");
        jwtProperties.setSecret("unit-test-secret");
        jwtProperties.setAccessTokenSeconds(7200);
        jwtProperties.setRefreshTokenSeconds(2592000);
        jwtTokenService = new JwtTokenService(new ObjectMapper(), jwtProperties, professionalIdentityRepository);
    }

    @Test
    void shouldParseProfessionalTokenFromLegacyTables() {
        ProfessionalTokenRecord tokenRecord = new ProfessionalTokenRecord();
        tokenRecord.setUserId(100L);
        tokenRecord.setTenantId(1L);
        tokenRecord.setExpiresTime(LocalDateTime.now().plusMinutes(30));

        SystemUser systemUser = new SystemUser();
        systemUser.setId(100L);
        systemUser.setTenantId(1L);
        systemUser.setUsername("medicine");
        systemUser.setNickname("药师A");

        when(professionalIdentityRepository.selectActiveProfessionalToken(eq("legacy-token"), any())).thenReturn(tokenRecord);
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 100L)).thenReturn(systemUser);
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 100L)).thenReturn(List.of("pharmacists", "common"));

        JwtClaims claims = jwtTokenService.parse("legacy-token");

        assertThat(claims).isNotNull();
        assertThat(claims.isProfessional()).isTrue();
        assertThat(claims.getUserId()).isEqualTo(100L);
        assertThat(claims.getTenantId()).isEqualTo(1L);
        assertThat(claims.getRoles()).contains("pharmacists");
        verify(professionalIdentityRepository).selectActiveProfessionalToken(eq("legacy-token"), any());
    }

    @Test
    void shouldReturnNullForUnknownProfessionalToken() {
        when(professionalIdentityRepository.selectActiveProfessionalToken(eq("missing-token"), any())).thenReturn(null);

        JwtClaims claims = jwtTokenService.parse("missing-token");

        assertThat(claims).isNull();
    }

    @Test
    void shouldHonorConfiguredShortAccessTokenTtl() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("backend-v2");
        jwtProperties.setSecret("unit-test-secret");
        jwtProperties.setAccessTokenSeconds(2);
        jwtProperties.setRefreshTokenSeconds(2592000);
        jwtTokenService = new JwtTokenService(new ObjectMapper(), jwtProperties, professionalIdentityRepository);

        JwtClaims claims = jwtTokenService.parse(
                jwtTokenService.generateAccessToken("13800138000", "APP", "patient-web", List.of("ROLE_PATIENT"))
        );

        assertThat(claims).isNotNull();
        assertThat(ChronoUnit.SECONDS.between(claims.getIssuedAt(), claims.getExpiresAt())).isBetween(1L, 2L);
    }

    @Test
    void shouldRoundTripProfessionalEffectiveRole() {
        String token = jwtTokenService.generateProfessionalAccessToken(
                "doctor",
                100L,
                1L,
                "Doctor A",
                List.of("admin"),
                "doctor"
        );

        JwtClaims claims = jwtTokenService.parse(token);

        assertThat(claims).isNotNull();
        assertThat(claims.isProfessional()).isTrue();
        assertThat(claims.getRoles()).containsExactly("admin");
        assertThat(claims.getEffectiveRole()).isEqualTo("doctor");
    }
}
