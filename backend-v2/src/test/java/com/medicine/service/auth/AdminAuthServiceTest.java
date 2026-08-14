package com.medicine.service.auth;

import com.medicine.api.dto.auth.AdminPermissionInfoDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private ProfessionalIdentityRepository professionalIdentityRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getAccessTokenSeconds()).thenReturn(7200L);
        lenient().when(jwtProperties.getRefreshTokenSeconds()).thenReturn(2592000L);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldLoginAndStoreRefreshSession() {
        SystemUser user = systemUser();
        when(professionalIdentityRepository.selectActiveSystemUserByUsername(1L, "pharmacist")).thenReturn(user);
        when(passwordEncoder.matches("secret-123", user.getPassword())).thenReturn(true);
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("pharmacist"));
        when(jwtTokenService.generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("pharmacist"))).thenReturn("admin-access");
        when(jwtTokenService.generateProfessionalRefreshToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("pharmacist"))).thenReturn("admin-refresh");
        when(jwtTokenService.parse("admin-refresh")).thenReturn(professionalClaims(Instant.now().plusSeconds(2592000)));

        TokenPair tokenPair = adminAuthService.login("pharmacist", "secret-123", "pharmacist", 1L);

        assertThat(tokenPair.getAccessToken()).isEqualTo("admin-access");
        assertThat(tokenPair.getRefreshToken()).isEqualTo("admin-refresh");
        assertThat(tokenPair.getDeviceId()).isNull();
        verify(valueOperations).set(eq("admin:session:refresh:1:1001"), anyString(), anyLong(), any());
    }

    @Test
    void shouldLoginWithRequestedDoctorEffectiveRoleForDualRoleUser() {
        SystemUser user = systemUser();
        when(professionalIdentityRepository.selectActiveSystemUserByUsername(1L, "pharmacist")).thenReturn(user);
        when(passwordEncoder.matches("secret-123", user.getPassword())).thenReturn(true);
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("admin"));
        when(jwtTokenService.generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("doctor"))).thenReturn("doctor-access");
        when(jwtTokenService.generateProfessionalRefreshToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("doctor"))).thenReturn("doctor-refresh");
        when(jwtTokenService.parse("doctor-refresh")).thenReturn(professionalClaims(Instant.now().plusSeconds(2592000), "doctor"));

        TokenPair tokenPair = adminAuthService.login("pharmacist", "secret-123", "doctor", 1L);

        assertThat(tokenPair.getAccessToken()).isEqualTo("doctor-access");
        verify(jwtTokenService).generateProfessionalAccessToken(
                eq("pharmacist"),
                eq(1001L),
                eq(1L),
                eq("Pharmacist A"),
                eq(List.of("admin")),
                eq("doctor")
        );
        verify(jwtTokenService).generateProfessionalRefreshToken(
                eq("pharmacist"),
                eq(1001L),
                eq(1L),
                eq("Pharmacist A"),
                eq(List.of("admin")),
                eq("doctor")
        );
    }

    @Test
    void shouldRejectInvalidPassword() {
        SystemUser user = systemUser();
        when(professionalIdentityRepository.selectActiveSystemUserByUsername(1L, "pharmacist")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> adminAuthService.login("pharmacist", "wrong-password", "pharmacist", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("ADMIN_AUTH_INVALID");

        verify(jwtTokenService, never()).generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), any());
    }

    @Test
    void shouldRejectRoleMismatch() {
        SystemUser user = systemUser();
        when(professionalIdentityRepository.selectActiveSystemUserByUsername(1L, "pharmacist")).thenReturn(user);
        when(passwordEncoder.matches("secret-123", user.getPassword())).thenReturn(true);
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("pharmacist"));

        assertThatThrownBy(() -> adminAuthService.login("pharmacist", "secret-123", "doctor", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PROFESSIONAL_ROLE_MISMATCH");
    }

    @Test
    void shouldRotateRefreshTokenWhenRedisMatches() {
        when(jwtTokenService.parse("refresh-token")).thenReturn(professionalClaims(Instant.now().plusSeconds(1200)));
        when(valueOperations.get("admin:session:refresh:1:1001")).thenReturn(hashToken("refresh-token"));
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 1001L)).thenReturn(systemUser());
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("pharmacist"));
        when(jwtTokenService.generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("pharmacist"))).thenReturn("new-access");
        when(jwtTokenService.generateProfessionalRefreshToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("pharmacist"))).thenReturn("new-refresh");
        when(jwtTokenService.parse("new-refresh")).thenReturn(professionalClaims(Instant.now().plusSeconds(2592000)));

        TokenPair tokenPair = adminAuthService.refreshToken("refresh-token");

        assertThat(tokenPair.getAccessToken()).isEqualTo("new-access");
        assertThat(tokenPair.getRefreshToken()).isEqualTo("new-refresh");
        verify(valueOperations).set(eq("admin:session:refresh:1:1001"), anyString(), anyLong(), any());
    }

    @Test
    void shouldPreserveEffectiveRoleWhenRefreshingToken() {
        when(jwtTokenService.parse("refresh-token")).thenReturn(professionalClaims(Instant.now().plusSeconds(1200), "doctor"));
        when(valueOperations.get("admin:session:refresh:1:1001")).thenReturn(hashToken("refresh-token"));
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 1001L)).thenReturn(systemUser());
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("admin"));
        when(jwtTokenService.generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("doctor"))).thenReturn("new-doctor-access");
        when(jwtTokenService.generateProfessionalRefreshToken(anyString(), anyLong(), anyLong(), anyString(), any(), eq("doctor"))).thenReturn("new-doctor-refresh");
        when(jwtTokenService.parse("new-doctor-refresh")).thenReturn(professionalClaims(Instant.now().plusSeconds(2592000), "doctor"));

        TokenPair tokenPair = adminAuthService.refreshToken("refresh-token");

        assertThat(tokenPair.getAccessToken()).isEqualTo("new-doctor-access");
        verify(jwtTokenService).generateProfessionalAccessToken(
                eq("pharmacist"),
                eq(1001L),
                eq(1L),
                eq("Pharmacist A"),
                eq(List.of("admin")),
                eq("doctor")
        );
    }

    @Test
    void shouldRejectRefreshWhenRedisDoesNotMatch() {
        when(jwtTokenService.parse("refresh-token")).thenReturn(professionalClaims(Instant.now().plusSeconds(1200)));
        when(valueOperations.get("admin:session:refresh:1:1001")).thenReturn("redis-mismatch");

        assertThatThrownBy(() -> adminAuthService.refreshToken("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("ADMIN_AUTH_INVALID");

        verify(jwtTokenService, never()).generateProfessionalAccessToken(anyString(), anyLong(), anyLong(), anyString(), any(), any());
    }

    @Test
    void shouldReturnPermissionInfoShapeExpectedByFrontend() {
        SystemUser user = systemUser();
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 1001L)).thenReturn(user);
        when(professionalIdentityRepository.selectRoleCodesByUserId(1L, 1001L)).thenReturn(List.of("pharmacist", "admin"));
        when(professionalIdentityRepository.selectPermissionCodesByUserId(1L, 1001L)).thenReturn(List.of("system:user:list"));

        AdminPermissionInfoDTO info = adminAuthService.getPermissionInfo(1L, 1001L);

        assertThat(info.getUser().getId()).isEqualTo(1001L);
        assertThat(info.getUser().getUsername()).isEqualTo("pharmacist");
        assertThat(info.getUser().getNickname()).isEqualTo("Pharmacist A");
        assertThat(info.getUser().getAvatar()).isEqualTo("https://example.test/avatar.png");
        assertThat(info.getRoles()).containsExactly("pharmacist", "admin");
        assertThat(info.getPermissions()).containsExactly("system:user:list");
    }

    @Test
    void shouldLogoutDeleteRedisSession() {
        boolean result = adminAuthService.logout(1L, 1001L);

        assertThat(result).isTrue();
        verify(stringRedisTemplate).delete("admin:session:refresh:1:1001");
    }

    private SystemUser systemUser() {
        SystemUser user = new SystemUser();
        user.setId(1001L);
        user.setUsername("pharmacist");
        user.setPassword("$2a$10$example");
        user.setNickname("Pharmacist A");
        user.setAvatar("https://example.test/avatar.png");
        user.setTenantId(1L);
        return user;
    }

    private JwtClaims professionalClaims(Instant expiresAt) {
        return professionalClaims(expiresAt, "pharmacist");
    }

    private JwtClaims professionalClaims(Instant expiresAt, String effectiveRole) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("pharmacist");
        claims.setDomain("admin");
        claims.setAccountType("PROFESSIONAL");
        claims.setUserId(1001L);
        claims.setTenantId(1L);
        claims.setDisplayName("Pharmacist A");
        claims.setRoles(List.of("pharmacist"));
        claims.setEffectiveRole(effectiveRole);
        claims.setJti("refresh-jti");
        claims.setExpiresAt(expiresAt);
        return claims;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
