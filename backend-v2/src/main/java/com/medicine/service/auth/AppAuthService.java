package com.medicine.service.auth;

import com.medicine.api.dto.auth.AppLoginRequest;
import com.medicine.api.dto.auth.OtpSendRequest;
import com.medicine.api.dto.auth.RefreshTokenRequest;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.api.dto.identity.AppCurrentAccountDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.AppAccountDeviceSession;
import com.medicine.domain.identity.OtpLoginLog;
import com.medicine.repository.identity.AppAccountDeviceSessionMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.OtpLoginLogMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AppAuthService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration OTP_RATE_LIMIT = Duration.ofSeconds(60);
    private static final String APP_DOMAIN = "APP";
    private static final String DEFAULT_OTP_CODE = "123456";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final AppPatientService appPatientService;
    private final AppAccountMapper appAccountMapper;
    private final AppAccountDeviceSessionMapper appAccountDeviceSessionMapper;
    private final OtpLoginLogMapper otpLoginLogMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public AppAuthService(
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            AppPatientService appPatientService,
            AppAccountMapper appAccountMapper,
            AppAccountDeviceSessionMapper appAccountDeviceSessionMapper,
            OtpLoginLogMapper otpLoginLogMapper,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.appPatientService = appPatientService;
        this.appAccountMapper = appAccountMapper;
        this.appAccountDeviceSessionMapper = appAccountDeviceSessionMapper;
        this.otpLoginLogMapper = otpLoginLogMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean sendOtp(OtpSendRequest request) {
        String mobile = normalize(request.getMobile());
        String scene = StringUtils.hasText(request.getScene()) ? request.getScene().trim() : "APP_LOGIN";
        if (!isValidMobile(mobile)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        String otpRateKey = otpRateKey(mobile);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(otpRateKey))) {
            throw new BusinessException(ErrorCode.APP_AUTH_OTP_RATE_LIMITED.getCode(), ErrorCode.APP_AUTH_OTP_RATE_LIMITED.getMessage());
        }

        String requestId = UUID.randomUUID().toString();
        OtpLoginLog otpLoginLog = new OtpLoginLog();
        otpLoginLog.setMobile(mobile);
        otpLoginLog.setScene(scene);
        otpLoginLog.setProvider("LOCAL_STUB");
        otpLoginLog.setRequestId(requestId);
        otpLoginLog.setSendStatus("SUCCESS");
        otpLoginLog.setVerifyStatus("NOT_VERIFIED");
        applyAuditFields(otpLoginLog, "app-auth");
        otpLoginLogMapper.insert(otpLoginLog);

        stringRedisTemplate.opsForValue().set(otpCodeKey(mobile), requestId + "|" + DEFAULT_OTP_CODE, OTP_TTL);
        stringRedisTemplate.opsForValue().set(otpRateKey, requestId, OTP_RATE_LIMIT);
        return true;
    }

    public TokenPair login(AppLoginRequest request) {
        String mobile = normalize(request.getMobile());
        String otpCode = normalize(request.getOtpCode());
        String deviceId = normalize(request.getDeviceId());
        String deviceName = normalize(request.getDeviceName());
        String platform = StringUtils.hasText(request.getPlatform()) ? request.getPlatform().trim().toUpperCase() : "WEB";

        if (!isValidMobile(mobile) || !StringUtils.hasText(otpCode) || !StringUtils.hasText(deviceId)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        String otpPayload = stringRedisTemplate.opsForValue().get(otpCodeKey(mobile));
        if (!StringUtils.hasText(otpPayload)) {
            throw new BusinessException(ErrorCode.APP_AUTH_OTP_EXPIRED.getCode(), ErrorCode.APP_AUTH_OTP_EXPIRED.getMessage());
        }

        OtpPayload parsedOtp = parseOtpPayload(otpPayload);
        if (parsedOtp == null || !otpCode.equals(parsedOtp.code())) {
            updateOtpVerifyStatus(parsedOtp == null ? null : parsedOtp.requestId(), "FAILED", ErrorCode.APP_AUTH_OTP_INVALID.getMessage());
            throw new BusinessException(ErrorCode.APP_AUTH_OTP_INVALID.getCode(), ErrorCode.APP_AUTH_OTP_INVALID.getMessage());
        }

        stringRedisTemplate.delete(otpCodeKey(mobile));
        updateOtpVerifyStatus(parsedOtp.requestId(), "SUCCESS", null);

        AppAccount appAccount = upsertPatientAccount(mobile);
        return issueLoginTokenPair(appAccount, deviceId, deviceName, platform);
    }

    public TokenPair refreshToken(RefreshTokenRequest request) {
        String refreshToken = normalize(request.getRefreshToken());
        String deviceId = normalize(request.getDeviceId());

        if (!StringUtils.hasText(refreshToken) || !StringUtils.hasText(deviceId)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        JwtClaims claims = jwtTokenService.parse(refreshToken);
        if (claims == null || !APP_DOMAIN.equals(claims.getDomain()) || !deviceId.equals(claims.getDeviceId())) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }

        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, claims.getSubject());
        if (appAccount == null) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }

        AppAccountDeviceSession session = appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(DEFAULT_TENANT_ID, appAccount.getId(), deviceId);
        if (session == null || session.getRevokedAt() != null || session.getRefreshTokenExpiresAt() == null
                || session.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }

        String refreshTokenHash = hashToken(refreshToken);
        String redisRefreshHash = stringRedisTemplate.opsForValue().get(refreshSessionKey(appAccount.getId(), deviceId));
        if (!refreshTokenHash.equals(session.getRefreshTokenHash()) || !refreshTokenHash.equals(redisRefreshHash)) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }

        return rotateTokenPair(appAccount, session, refreshTokenHash);
    }

    public TokenPair refreshSession(String refreshToken) {
        JwtClaims claims = jwtTokenService.parse(normalize(refreshToken));
        if (claims == null || !APP_DOMAIN.equals(claims.getDomain()) || !StringUtils.hasText(claims.getDeviceId())) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        request.setDeviceId(claims.getDeviceId());
        return refreshToken(request);
    }

    public boolean logoutSession(String refreshToken) {
        String tokenValue = normalize(refreshToken);
        JwtClaims claims = jwtTokenService.parse(tokenValue);
        if (claims == null || !APP_DOMAIN.equals(claims.getDomain()) || !StringUtils.hasText(claims.getDeviceId())) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, claims.getSubject());
        if (appAccount == null) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        AppAccountDeviceSession session = appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(
                DEFAULT_TENANT_ID, appAccount.getId(), claims.getDeviceId());
        String tokenHash = hashToken(tokenValue);
        String redisHash = stringRedisTemplate.opsForValue().get(refreshSessionKey(appAccount.getId(), claims.getDeviceId()));
        if (session == null || session.getRevokedAt() != null || !tokenHash.equals(session.getRefreshTokenHash())
                || !tokenHash.equals(redisHash)) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        session.setRevokedAt(now);
        session.setRevokedReason("logout");
        session.setLastSeenAt(now);
        session.setUpdater("app-auth");
        session.setUpdateTime(now);
        if (appAccountDeviceSessionMapper.revokeSession(session, tokenHash) != 1) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        stringRedisTemplate.delete(refreshSessionKey(appAccount.getId(), claims.getDeviceId()));
        return true;
    }

    public boolean logout(String subject, String deviceId) {
        if (!StringUtils.hasText(subject) || !StringUtils.hasText(deviceId)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        if (appAccount == null) {
            return true;
        }

        AppAccountDeviceSession session = appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(DEFAULT_TENANT_ID, appAccount.getId(), deviceId);
        if (session != null) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            session.setRevokedAt(now);
            session.setRevokedReason("logout");
            session.setLastSeenAt(now);
            session.setUpdater("app-auth");
            session.setUpdateTime(now);
            appAccountDeviceSessionMapper.updateSession(session);
        }

        stringRedisTemplate.delete(refreshSessionKey(appAccount.getId(), deviceId));
        return true;
    }

    public AppCurrentAccountDTO getCurrentAccount(String subject, String deviceId) {
        AppAccount appAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, subject);
        var patientSummary = appPatientService.getCurrentPatient(subject);

        AppCurrentAccountDTO dto = new AppCurrentAccountDTO();
        dto.setMobile(subject);
        dto.setDisplayName(appAccount == null ? null : appAccount.getDisplayName());
        dto.setAccountType(appAccount == null ? null : appAccount.getAccountType());
        dto.setBound(patientSummary != null);
        dto.setDeviceId(deviceId);
        dto.setPatient(patientSummary);
        dto.setLastLoginAt(appAccount == null || appAccount.getLastLoginTime() == null
                ? null
                : appAccount.getLastLoginTime().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }

    private AppAccount upsertPatientAccount(String mobile) {
        AppAccount existingAccount = appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, mobile);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (existingAccount != null) {
            existingAccount.setDisplayName(mobile);
            existingAccount.setLastLoginTime(now);
            existingAccount.setLastLoginIp("127.0.0.1");
            existingAccount.setUpdater("app-auth");
            existingAccount.setUpdateTime(now);
            appAccountMapper.updateLoginInfo(existingAccount);
            return existingAccount;
        }

        AppAccount appAccount = new AppAccount();
        appAccount.setMobile(mobile);
        appAccount.setAccountType("PATIENT");
        appAccount.setStatus(ACCOUNT_STATUS_ACTIVE);
        appAccount.setDisplayName(mobile);
        appAccount.setLastLoginIp("127.0.0.1");
        appAccount.setLastLoginTime(now);
        applyAuditFields(appAccount, "app-auth");
        appAccountMapper.insert(appAccount);
        return appAccount;
    }

    private TokenPair issueLoginTokenPair(AppAccount appAccount, String deviceId, String deviceName, String platform) {
        TokenPair tokenPair = createTokenPair(appAccount, deviceId);
        persistLoginSession(appAccount, tokenPair, deviceId, deviceName, platform);
        return tokenPair;
    }

    private TokenPair rotateTokenPair(
            AppAccount appAccount,
            AppAccountDeviceSession session,
            String expectedRefreshTokenHash
    ) {
        TokenPair tokenPair = createTokenPair(appAccount, session.getDeviceId());
        JwtClaims accessClaims = jwtTokenService.parse(tokenPair.getAccessToken());
        JwtClaims refreshClaims = jwtTokenService.parse(tokenPair.getRefreshToken());
        if (accessClaims == null || refreshClaims == null) {
            throw new IllegalStateException("Failed to parse freshly issued tokens");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        session.setAccessTokenJti(accessClaims.getJti());
        session.setRefreshTokenHash(hashToken(tokenPair.getRefreshToken()));
        session.setRefreshTokenExpiresAt(LocalDateTime.ofInstant(refreshClaims.getExpiresAt(), ZoneOffset.UTC));
        session.setLastSeenAt(now);
        session.setUpdater("app-auth");
        session.setUpdateTime(now);

        if (appAccountDeviceSessionMapper.rotateSession(session, expectedRefreshTokenHash) != 1) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }

        writeRefreshState(appAccount.getId(), session.getDeviceId(), session.getRefreshTokenHash(), refreshClaims.getExpiresAt());
        return tokenPair;
    }

    private TokenPair createTokenPair(AppAccount appAccount, String deviceId) {
        List<String> roles = "FAMILY".equalsIgnoreCase(appAccount.getAccountType())
                ? List.of("ROLE_FAMILY")
                : List.of("ROLE_PATIENT");
        String accessToken = jwtTokenService.generateAccessToken(appAccount.getMobile(), APP_DOMAIN, deviceId, roles);
        String refreshToken = jwtTokenService.generateRefreshToken(appAccount.getMobile(), APP_DOMAIN, deviceId, roles);

        TokenPair tokenPair = new TokenPair();
        tokenPair.setAccessToken(accessToken);
        tokenPair.setRefreshToken(refreshToken);
        tokenPair.setExpiresIn(jwtProperties.getAccessTokenSeconds());
        tokenPair.setRefreshExpiresIn(jwtProperties.getRefreshTokenSeconds());
        tokenPair.setDeviceId(deviceId);
        return tokenPair;
    }

    private void persistLoginSession(
            AppAccount appAccount,
            TokenPair tokenPair,
            String deviceId,
            String deviceName,
            String platform
    ) {
        String accessToken = tokenPair.getAccessToken();
        String refreshToken = tokenPair.getRefreshToken();

        JwtClaims accessClaims = jwtTokenService.parse(accessToken);
        JwtClaims refreshClaims = jwtTokenService.parse(refreshToken);
        if (accessClaims == null || refreshClaims == null) {
            throw new IllegalStateException("Failed to parse freshly issued tokens");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AppAccountDeviceSession session = appAccountDeviceSessionMapper.selectByAccountIdAndDeviceId(DEFAULT_TENANT_ID, appAccount.getId(), deviceId);
        if (session == null) {
            session = new AppAccountDeviceSession();
            session.setAppAccountId(appAccount.getId());
            session.setDeviceId(deviceId);
            session.setCreator("app-auth");
            session.setCreateTime(now);
            session.setDeleted(false);
            session.setTenantId(DEFAULT_TENANT_ID);
        }

        session.setDeviceName(StringUtils.hasText(deviceName) ? deviceName : deviceId);
        session.setPlatform(StringUtils.hasText(platform) ? platform : "WEB");
        session.setAccessTokenJti(accessClaims.getJti());
        session.setRefreshTokenHash(hashToken(refreshToken));
        session.setRefreshTokenExpiresAt(LocalDateTime.ofInstant(refreshClaims.getExpiresAt(), ZoneOffset.UTC));
        session.setLastSeenAt(now);
        session.setRevokedAt(null);
        session.setRevokedReason(null);
        session.setUpdater("app-auth");
        session.setUpdateTime(now);

        if (session.getId() == null) {
            appAccountDeviceSessionMapper.insert(session);
        } else {
            appAccountDeviceSessionMapper.updateSession(session);
        }

        writeRefreshState(appAccount.getId(), deviceId, session.getRefreshTokenHash(), refreshClaims.getExpiresAt());
    }

    private void writeRefreshState(Long appAccountId, String deviceId, String refreshTokenHash, Instant expiresAt) {
        Duration refreshTtl = Duration.between(Instant.now(), expiresAt);
        stringRedisTemplate.opsForValue().set(
                refreshSessionKey(appAccountId, deviceId),
                refreshTokenHash,
                Math.max(refreshTtl.getSeconds(), 1),
                TimeUnit.SECONDS
        );
    }

    private void updateOtpVerifyStatus(String requestId, String verifyStatus, String errorCode) {
        if (!StringUtils.hasText(requestId)) {
            return;
        }

        OtpLoginLog log = new OtpLoginLog();
        log.setRequestId(requestId);
        log.setVerifyStatus(verifyStatus);
        log.setErrorCode(errorCode);
        log.setErrorMessage(errorCode);
        log.setUpdater("app-auth");
        log.setUpdateTime(LocalDateTime.now(ZoneOffset.UTC));
        otpLoginLogMapper.updateVerifyStatusByRequestId(log);
    }

    private OtpPayload parseOtpPayload(String value) {
        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) {
            return null;
        }
        return new OtpPayload(parts[0], parts[1]);
    }

    private String refreshSessionKey(Long appAccountId, String deviceId) {
        return "app:session:refresh:" + appAccountId + ":" + deviceId;
    }

    private String otpCodeKey(String mobile) {
        return "app:otp:code:" + mobile;
    }

    private String otpRateKey(String mobile) {
        return "app:otp:rate:" + mobile;
    }

    private boolean isValidMobile(String mobile) {
        return mobile.matches("^1\\d{10}$");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash token", ex);
        }
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

    private void applyAuditFields(OtpLoginLog otpLoginLog, String operator) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        otpLoginLog.setCreator(operator);
        otpLoginLog.setCreateTime(now);
        otpLoginLog.setUpdater(operator);
        otpLoginLog.setUpdateTime(now);
        otpLoginLog.setDeleted(false);
        otpLoginLog.setTenantId(DEFAULT_TENANT_ID);
    }

    private record OtpPayload(String requestId, String code) {
    }
}
