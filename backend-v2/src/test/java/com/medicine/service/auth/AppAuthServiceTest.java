package com.medicine.service.auth;

import com.medicine.api.dto.auth.AppLoginRequest;
import com.medicine.api.dto.auth.OtpSendRequest;
import com.medicine.api.dto.auth.RefreshTokenRequest;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.AppAccountDeviceSession;
import com.medicine.repository.identity.AppAccountDeviceSessionMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.OtpLoginLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppAuthServiceTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private AppAccountMapper appAccountMapper;

    @Mock
    private AppAccountDeviceSessionMapper appAccountDeviceSessionMapper;

    @Mock
    private OtpLoginLogMapper otpLoginLogMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AppAuthService appAuthService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getAccessTokenSeconds()).thenReturn(7200L);
        lenient().when(jwtProperties.getRefreshTokenSeconds()).thenReturn(2592000L);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSendOtpUsingRedisAndPersistLog() {
        when(stringRedisTemplate.hasKey("app:otp:rate:13800138000")).thenReturn(false);

        boolean result = appAuthService.sendOtp(otpSendRequest());

        assertThat(result).isTrue();
        verify(otpLoginLogMapper).insert(any());
        verify(valueOperations).set(eq("app:otp:code:13800138000"), anyString(), any());
        verify(valueOperations).set(eq("app:otp:rate:13800138000"), anyString(), any());
    }

    @Test
    void shouldLoginPersistSessionAndStoreRefreshHashInRedis() {
        when(valueOperations.get("app:otp:code:13800138000")).thenReturn("req-1|123456");
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(null);
        doAnswer(invocation -> {
            AppAccount appAccount = invocation.getArgument(0);
            appAccount.setId(1001L);
            return 1;
        }).when(appAccountMapper).insert(any(AppAccount.class));
        when(jwtTokenService.generateAccessToken(anyString(), anyString(), anyString(), any())).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(anyString(), anyString(), anyString(), any())).thenReturn("refresh-token");
        when(jwtTokenService.parse("access-token")).thenReturn(jwtClaims("13800138000", "patient-web", "access-jti", Instant.now().plusSeconds(7200)));
        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(null);

        TokenPair tokenPair = appAuthService.login(appLoginRequest());

        assertThat(tokenPair.getAccessToken()).isEqualTo("access-token");
        assertThat(tokenPair.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(tokenPair.getRefreshExpiresIn()).isEqualTo(2592000L);
        assertThat(tokenPair.getDeviceId()).isEqualTo("patient-web");
        verify(appAccountMapper).insert(any(AppAccount.class));
        verify(appAccountDeviceSessionMapper).insert(any(AppAccountDeviceSession.class));
        verify(valueOperations).set(eq("app:session:refresh:1001:patient-web"), anyString(), anyLong(), any());
    }

    @Test
    void shouldRotateRefreshTokenAgainstRedisAndDatabaseSession() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");
        session.setDeviceName("Codex");
        session.setPlatform("WEB");
        session.setRefreshTokenHash(hashToken("refresh-token"));
        session.setRefreshTokenExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(10));

        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);
        when(valueOperations.get("app:session:refresh:1001:patient-web")).thenReturn(hashToken("refresh-token"));
        when(jwtTokenService.generateAccessToken(anyString(), anyString(), anyString(), any())).thenReturn("new-access-token");
        when(jwtTokenService.generateRefreshToken(anyString(), anyString(), anyString(), any())).thenReturn("new-refresh-token");
        when(jwtTokenService.parse("new-access-token")).thenReturn(jwtClaims("13800138000", "patient-web", "new-access-jti", Instant.now().plusSeconds(7200)));
        when(jwtTokenService.parse("new-refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "new-refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountDeviceSessionMapper.rotateSession(any(AppAccountDeviceSession.class), eq(hashToken("refresh-token")))).thenReturn(1);

        TokenPair tokenPair = appAuthService.refreshToken(refreshTokenRequest());

        assertThat(tokenPair.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(tokenPair.getRefreshExpiresIn()).isEqualTo(2592000L);
        assertThat(tokenPair.getDeviceId()).isEqualTo("patient-web");
        verify(appAccountDeviceSessionMapper).rotateSession(any(AppAccountDeviceSession.class), eq(hashToken("refresh-token")));
        verify(valueOperations).set(eq("app:session:refresh:1001:patient-web"), anyString(), anyLong(), any());
    }

    @Test
    void shouldRejectConcurrentRefreshWhenDatabaseCompareAndSetLoses() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");
        session.setRefreshTokenHash(hashToken("refresh-token"));
        session.setRefreshTokenExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(10));

        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);
        when(valueOperations.get("app:session:refresh:1001:patient-web")).thenReturn(hashToken("refresh-token"));
        when(jwtTokenService.generateAccessToken(anyString(), anyString(), anyString(), any())).thenReturn("new-access-token");
        when(jwtTokenService.generateRefreshToken(anyString(), anyString(), anyString(), any())).thenReturn("new-refresh-token");
        when(jwtTokenService.parse("new-access-token")).thenReturn(jwtClaims("13800138000", "patient-web", "new-access-jti", Instant.now().plusSeconds(7200)));
        when(jwtTokenService.parse("new-refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "new-refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountDeviceSessionMapper.rotateSession(any(AppAccountDeviceSession.class), eq(hashToken("refresh-token")))).thenReturn(0);

        assertThatThrownBy(() -> appAuthService.refreshToken(refreshTokenRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("APP_AUTH_REFRESH_INVALID");

        verify(valueOperations, never()).set(eq("app:session:refresh:1001:patient-web"), anyString(), anyLong(), any());
    }

    @Test
    void shouldRejectRefreshWhenRedisStateDoesNotMatch() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");
        session.setRefreshTokenHash("db-refresh-hash");
        session.setRefreshTokenExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(10));

        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);
        when(valueOperations.get("app:session:refresh:1001:patient-web")).thenReturn("redis-mismatch");

        assertThatThrownBy(() -> appAuthService.refreshToken(refreshTokenRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("APP_AUTH_REFRESH_INVALID");

        verify(appAccountDeviceSessionMapper, never()).updateSession(any(AppAccountDeviceSession.class));
    }

    @Test
    void shouldLogoutRevokeSessionAndDeleteRedisKey() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");

        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);

        boolean result = appAuthService.logout("13800138000", "patient-web");

        assertThat(result).isTrue();
        verify(appAccountDeviceSessionMapper).updateSession(any(AppAccountDeviceSession.class));
        verify(stringRedisTemplate).delete("app:session:refresh:1001:patient-web");
    }

    @Test
    void shouldConditionallyRevokeCookieSessionBeforeDeletingRedisState() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");
        session.setRefreshTokenHash(hashToken("refresh-token"));

        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);
        when(valueOperations.get("app:session:refresh:1001:patient-web")).thenReturn(hashToken("refresh-token"));
        when(appAccountDeviceSessionMapper.revokeSession(session, hashToken("refresh-token"))).thenReturn(1);

        assertThat(appAuthService.logoutSession("refresh-token")).isTrue();

        verify(appAccountDeviceSessionMapper).revokeSession(session, hashToken("refresh-token"));
        verify(stringRedisTemplate).delete("app:session:refresh:1001:patient-web");
    }

    @Test
    void shouldNotDeleteRedisStateWhenCookieLogoutLosesDatabaseRace() {
        AppAccount appAccount = new AppAccount();
        appAccount.setId(1001L);
        appAccount.setMobile("13800138000");

        AppAccountDeviceSession session = new AppAccountDeviceSession();
        session.setId(2001L);
        session.setAppAccountId(1001L);
        session.setDeviceId("patient-web");
        session.setRefreshTokenHash(hashToken("refresh-token"));

        when(jwtTokenService.parse("refresh-token")).thenReturn(jwtClaims("13800138000", "patient-web", "refresh-jti", Instant.now().plusSeconds(2592000)));
        when(appAccountMapper.selectActiveByMobile(1L, "13800138000")).thenReturn(appAccount);
        when(appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(1L, 1001L, "patient-web")).thenReturn(session);
        when(valueOperations.get("app:session:refresh:1001:patient-web")).thenReturn(hashToken("refresh-token"));
        when(appAccountDeviceSessionMapper.revokeSession(session, hashToken("refresh-token"))).thenReturn(0);

        assertThatThrownBy(() -> appAuthService.logoutSession("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("APP_AUTH_REFRESH_INVALID");

        verify(stringRedisTemplate, never()).delete("app:session:refresh:1001:patient-web");
    }

    private OtpSendRequest otpSendRequest() {
        OtpSendRequest request = new OtpSendRequest();
        request.setMobile("13800138000");
        request.setScene("APP_LOGIN");
        return request;
    }

    private AppLoginRequest appLoginRequest() {
        AppLoginRequest request = new AppLoginRequest();
        request.setMobile("13800138000");
        request.setOtpCode("123456");
        request.setDeviceId("patient-web");
        request.setDeviceName("Codex");
        request.setPlatform("web");
        return request;
    }

    private RefreshTokenRequest refreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        request.setDeviceId("patient-web");
        return request;
    }

    private JwtClaims jwtClaims(String subject, String deviceId, String jti, Instant expiresAt) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(subject);
        claims.setDomain("APP");
        claims.setDeviceId(deviceId);
        claims.setJti(jti);
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
