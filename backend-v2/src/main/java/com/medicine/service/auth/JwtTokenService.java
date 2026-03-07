package com.medicine.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.config.JwtProperties;
import com.medicine.domain.identity.ProfessionalTokenRecord;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;
    private final ProfessionalIdentityRepository professionalIdentityRepository;

    public JwtTokenService(
            ObjectMapper objectMapper,
            JwtProperties jwtProperties,
            ProfessionalIdentityRepository professionalIdentityRepository
    ) {
        this.objectMapper = objectMapper;
        this.jwtProperties = jwtProperties;
        this.professionalIdentityRepository = professionalIdentityRepository;
    }

    public String generateAccessToken(String subject, String domain, String deviceId, List<String> roles) {
        return generateToken(subject, domain, deviceId, roles, jwtProperties.getAccessTokenSeconds());
    }

    public String generateRefreshToken(String subject, String domain, String deviceId, List<String> roles) {
        return generateToken(subject, domain, deviceId, roles, jwtProperties.getRefreshTokenSeconds());
    }

    public JwtClaims parse(String token) {
        try {
            if (!token.contains(".")) {
                return parseProfessionalAccessToken(token);
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                return null;
            }
            String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }
            JwtClaims claims = new JwtClaims();
            claims.setSubject((String) payload.get("sub"));
            claims.setIssuer((String) payload.get("iss"));
            claims.setDomain((String) payload.get("domain"));
            claims.setAccountType((String) payload.getOrDefault("accountType", "APP"));
            claims.setDeviceId((String) payload.get("deviceId"));
            claims.setJti((String) payload.get("jti"));
            claims.setRoles(objectMapper.convertValue(payload.get("roles"), new TypeReference<>() {
            }));
            claims.setIssuedAt(Instant.ofEpochSecond(((Number) payload.get("iat")).longValue()));
            claims.setExpiresAt(Instant.ofEpochSecond(exp));
            return claims;
        } catch (Exception ex) {
            return null;
        }
    }

    private JwtClaims parseProfessionalAccessToken(String token) {
        ProfessionalTokenRecord tokenRecord = professionalIdentityRepository.selectActiveProfessionalToken(token, Instant.now());
        if (tokenRecord == null) {
            return null;
        }
        SystemUser systemUser = professionalIdentityRepository.selectActiveSystemUser(
                tokenRecord.getTenantId(),
                tokenRecord.getUserId()
        );
        if (systemUser == null) {
            return null;
        }

        JwtClaims claims = new JwtClaims();
        claims.setSubject(systemUser.getUsername());
        claims.setIssuer("legacy-oauth2");
        claims.setDomain("admin");
        claims.setAccountType("PROFESSIONAL");
        claims.setUserId(systemUser.getId());
        claims.setTenantId(tokenRecord.getTenantId());
        claims.setDisplayName(systemUser.getNickname());
        claims.setRoles(safeRoles(professionalIdentityRepository.selectRoleCodesByUserId(
                tokenRecord.getTenantId(),
                systemUser.getId()
        )));
        claims.setExpiresAt(tokenRecord.getExpiresTime().toInstant(ZoneOffset.UTC));
        return claims;
    }

    private List<String> safeRoles(List<String> roles) {
        return roles == null ? Collections.emptyList() : roles;
    }

    private String generateToken(String subject, String domain, String deviceId, List<String> roles, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new HashMap<>();
            payload.put("iss", jwtProperties.getIssuer());
            payload.put("sub", subject);
            payload.put("domain", domain);
            payload.put("accountType", "APP");
            payload.put("deviceId", deviceId);
            payload.put("roles", roles);
            payload.put("jti", UUID.randomUUID().toString());
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", now.plusSeconds(ttlSeconds).getEpochSecond());

            String encodedHeader = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signature = sign(encodedHeader + "." + encodedPayload);
            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate JWT token", ex);
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
