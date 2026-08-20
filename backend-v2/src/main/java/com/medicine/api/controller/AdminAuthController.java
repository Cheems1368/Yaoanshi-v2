package com.medicine.api.controller;

import com.medicine.api.dto.auth.AdminLoginRequest;
import com.medicine.api.dto.auth.AdminPermissionInfoDTO;
import com.medicine.api.dto.auth.SessionTokenDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.AdminAuthService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.RefreshCookieManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/system/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final RefreshCookieManager refreshCookieManager;

    public AdminAuthController(AdminAuthService adminAuthService, RefreshCookieManager refreshCookieManager) {
        this.adminAuthService = adminAuthService;
        this.refreshCookieManager = refreshCookieManager;
    }

    @PostMapping("/login")
    public CommonResult<TokenPair> login(
            @RequestBody AdminLoginRequest request,
            @RequestHeader(value = "tenant-id", required = false) Long tenantId
    ) {
        return CommonResult.ok(adminAuthService.login(
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                tenantId
        ));
    }

    @PostMapping("/refresh-token")
    public CommonResult<TokenPair> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return CommonResult.ok(adminAuthService.refreshToken(refreshToken));
    }

    @PostMapping("/session/login")
    public CommonResult<SessionTokenDTO> sessionLogin(
            @RequestBody AdminLoginRequest request,
            @RequestHeader(value = "tenant-id", required = false) Long tenantId,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = adminAuthService.login(request.getUsername(), request.getPassword(), request.getRole(), tenantId);
        refreshCookieManager.writeAdminCookie(response, tokenPair.getRefreshToken());
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(SessionTokenDTO.from(tokenPair));
    }

    @PostMapping("/session/refresh")
    public CommonResult<SessionTokenDTO> sessionRefresh(
            @CookieValue(value = RefreshCookieManager.ADMIN_COOKIE_NAME, required = false) String refreshToken,
            @RequestHeader(value = RefreshCookieManager.REFRESH_GUARD_HEADER, required = false) String refreshGuard,
            HttpServletResponse response
    ) {
        requireRefreshGuard(refreshGuard);
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTH_INVALID.getCode(), ErrorCode.ADMIN_AUTH_INVALID.getMessage());
        }
        TokenPair tokenPair = adminAuthService.refreshSession(refreshToken);
        refreshCookieManager.writeAdminCookie(response, tokenPair.getRefreshToken());
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(SessionTokenDTO.from(tokenPair));
    }

    @PostMapping("/session/logout")
    public CommonResult<Boolean> sessionLogout(
            @CookieValue(value = RefreshCookieManager.ADMIN_COOKIE_NAME, required = false) String refreshToken,
            @RequestHeader(value = RefreshCookieManager.REFRESH_GUARD_HEADER, required = false) String refreshGuard,
            HttpServletResponse response
    ) {
        requireRefreshGuard(refreshGuard);
        refreshCookieManager.clearAdminCookie(response);
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(!StringUtils.hasText(refreshToken) || adminAuthService.logoutSession(refreshToken));
    }

    @GetMapping("/get-permission-info")
    public CommonResult<AdminPermissionInfoDTO> getPermissionInfo(Authentication authentication) {
        JwtClaims claims = requireClaims(authentication);
        return CommonResult.ok(adminAuthService.getPermissionInfo(claims.getTenantId(), claims.getUserId()));
    }

    @PostMapping("/logout")
    public CommonResult<Boolean> logout(Authentication authentication) {
        JwtClaims claims = requireClaims(authentication);
        return CommonResult.ok(adminAuthService.logout(claims.getTenantId(), claims.getUserId()));
    }

    private JwtClaims requireClaims(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtClaims claims)
                || claims.getTenantId() == null || claims.getUserId() == null) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }
        return claims;
    }

    private void requireRefreshGuard(String refreshGuard) {
        if (!"1".equals(refreshGuard)) {
            throw new BusinessException(403, "FORBIDDEN");
        }
    }
}
