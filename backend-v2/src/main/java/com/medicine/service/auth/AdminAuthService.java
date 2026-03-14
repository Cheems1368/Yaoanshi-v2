package com.medicine.service.auth;

import com.medicine.api.dto.auth.AdminPermissionInfoDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AdminAuthService {

    private static final Set<String> PHARMACIST_ROLE_CODES = Set.of(
            "pharmacists",
            "pharmacist",
            "super_admin",
            "tenant_admin",
            "admin"
    );
    private static final Set<String> DOCTOR_ROLE_CODES = Set.of(
            "doctor",
            "doctors",
            "super_admin",
            "tenant_admin",
            "admin"
    );

    private final ProfessionalIdentityRepository professionalIdentityRepository;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(
            ProfessionalIdentityRepository professionalIdentityRepository,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            StringRedisTemplate stringRedisTemplate,
            PasswordEncoder passwordEncoder
    ) {
        this.professionalIdentityRepository = professionalIdentityRepository;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenPair login(String username, String password, String role, Long tenantId) {
        String normalizedUsername = normalize(username);
        String normalizedPassword = normalize(password);
        long resolvedTenantId = resolveTenantId(tenantId);
        if (!StringUtils.hasText(normalizedUsername) || !StringUtils.hasText(normalizedPassword)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        SystemUser user = professionalIdentityRepository.selectActiveSystemUserByUsername(resolvedTenantId, normalizedUsername);
        if (user == null || !StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(normalizedPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        List<String> roles = safeList(professionalIdentityRepository.selectRoleCodesByUserId(resolvedTenantId, user.getId()));
        validateRequestedRole(role, roles);

        return issueTokenPair(user, resolvedTenantId, roles);
    }

    public TokenPair refreshToken(String refreshTokenValue) {
        String tokenValue = normalize(refreshTokenValue);
        if (!StringUtils.hasText(tokenValue)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        JwtClaims claims = jwtTokenService.parse(tokenValue);
        if (claims == null
                || !claims.isProfessional()
                || claims.getUserId() == null
                || claims.getTenantId() == null
                || !"admin".equalsIgnoreCase(claims.getDomain())) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        String expectedHash = stringRedisTemplate.opsForValue().get(refreshSessionKey(claims.getTenantId(), claims.getUserId()));
        if (!hashToken(tokenValue).equals(expectedHash)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        SystemUser user = professionalIdentityRepository.selectActiveSystemUser(claims.getTenantId(), claims.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        List<String> roles = safeList(professionalIdentityRepository.selectRoleCodesByUserId(claims.getTenantId(), claims.getUserId()));
        return issueTokenPair(user, claims.getTenantId(), roles);
    }

    public AdminPermissionInfoDTO getPermissionInfo(Long tenantId, Long userId) {
        SystemUser user = professionalIdentityRepository.selectActiveSystemUser(tenantId, userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }

        List<String> roles = safeList(professionalIdentityRepository.selectRoleCodesByUserId(tenantId, userId));
        List<String> permissions = safeList(professionalIdentityRepository.selectPermissionCodesByUserId(tenantId, userId));

        AdminPermissionInfoDTO.UserInfo userInfo = new AdminPermissionInfoDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar() == null ? "" : user.getAvatar());

        AdminPermissionInfoDTO response = new AdminPermissionInfoDTO();
        response.setUser(userInfo);
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }

    public boolean logout(Long tenantId, Long userId) {
        stringRedisTemplate.delete(refreshSessionKey(tenantId, userId));
        return true;
    }

    private TokenPair issueTokenPair(SystemUser user, Long tenantId, List<String> roles) {
        String displayName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        String accessToken = jwtTokenService.generateProfessionalAccessToken(
                user.getUsername(),
                user.getId(),
                tenantId,
                displayName,
                roles
        );
        String refreshToken = jwtTokenService.generateProfessionalRefreshToken(
                user.getUsername(),
                user.getId(),
                tenantId,
                displayName,
                roles
        );

        JwtClaims refreshClaims = jwtTokenService.parse(refreshToken);
        if (refreshClaims == null || refreshClaims.getExpiresAt() == null) {
            throw new IllegalStateException("Failed to parse freshly issued admin refresh token");
        }

        Duration refreshTtl = Duration.between(Instant.now(), refreshClaims.getExpiresAt());
        stringRedisTemplate.opsForValue().set(
                refreshSessionKey(tenantId, user.getId()),
                hashToken(refreshToken),
                Math.max(refreshTtl.getSeconds(), 1),
                TimeUnit.SECONDS
        );

        TokenPair tokenPair = new TokenPair();
        tokenPair.setAccessToken(accessToken);
        tokenPair.setRefreshToken(refreshToken);
        tokenPair.setExpiresIn(jwtProperties.getAccessTokenSeconds());
        tokenPair.setRefreshExpiresIn(jwtProperties.getRefreshTokenSeconds());
        tokenPair.setDeviceId(null);
        return tokenPair;
    }

    private void validateRequestedRole(String role, List<String> roles) {
        Set<String> normalizedRoles = roles.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        boolean pharmacist = normalizedRoles.stream().anyMatch(PHARMACIST_ROLE_CODES::contains);
        boolean doctor = normalizedRoles.stream().anyMatch(DOCTOR_ROLE_CODES::contains);
        if (!pharmacist && !doctor) {
            throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
        }

        String requestedRole = normalize(role).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(requestedRole)) {
            return;
        }
        if ("pharmacist".equals(requestedRole) && !pharmacist) {
            throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
        }
        if ("doctor".equals(requestedRole) && !doctor) {
            throw new BusinessException(ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getCode(), ErrorCode.PROFESSIONAL_ROLE_MISMATCH.getMessage());
        }
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private long resolveTenantId(Long tenantId) {
        return tenantId == null ? 1L : tenantId;
    }

    private String refreshSessionKey(Long tenantId, Long userId) {
        return "admin:session:refresh:" + tenantId + ":" + userId;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash token", ex);
        }
    }
}
