package com.medicine.api.controller;

import com.medicine.api.dto.auth.AppLoginRequest;
import com.medicine.api.dto.auth.AppLogoutRequest;
import com.medicine.api.dto.auth.OtpSendRequest;
import com.medicine.api.dto.auth.RefreshTokenRequest;
import com.medicine.api.dto.auth.SessionTokenDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.api.dto.identity.AppCurrentAccountDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.AppAuthService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.RefreshCookieManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/auth")
public class AppAuthController {

    private final AppAuthService appAuthService;
    private final RefreshCookieManager refreshCookieManager;

    public AppAuthController(AppAuthService appAuthService, RefreshCookieManager refreshCookieManager) {
        this.appAuthService = appAuthService;
        this.refreshCookieManager = refreshCookieManager;
    }

    @PostMapping("/send-otp")
    public CommonResult<Boolean> sendOtp(@RequestBody OtpSendRequest request) {
        return CommonResult.ok(appAuthService.sendOtp(request));
    }

    @PostMapping("/login")
    public CommonResult<TokenPair> login(@RequestBody AppLoginRequest request) {
        return CommonResult.ok(appAuthService.login(request));
    }

    @PostMapping("/refresh-token")
    public CommonResult<TokenPair> refreshToken(@RequestBody RefreshTokenRequest request) {
        return CommonResult.ok(appAuthService.refreshToken(request));
    }

    @PostMapping("/session/login")
    public CommonResult<SessionTokenDTO> sessionLogin(
            @RequestBody AppLoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = appAuthService.login(request);
        refreshCookieManager.writeAppCookie(response, tokenPair.getRefreshToken());
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(SessionTokenDTO.from(tokenPair));
    }

    @PostMapping("/session/refresh")
    public CommonResult<SessionTokenDTO> sessionRefresh(
            @CookieValue(value = RefreshCookieManager.APP_COOKIE_NAME, required = false) String refreshToken,
            @RequestHeader(value = RefreshCookieManager.REFRESH_GUARD_HEADER, required = false) String refreshGuard,
            HttpServletResponse response
    ) {
        requireRefreshGuard(refreshGuard);
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.APP_AUTH_REFRESH_INVALID.getCode(), ErrorCode.APP_AUTH_REFRESH_INVALID.getMessage());
        }
        TokenPair tokenPair = appAuthService.refreshSession(refreshToken);
        refreshCookieManager.writeAppCookie(response, tokenPair.getRefreshToken());
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(SessionTokenDTO.from(tokenPair));
    }

    @PostMapping("/session/logout")
    public CommonResult<Boolean> sessionLogout(
            @CookieValue(value = RefreshCookieManager.APP_COOKIE_NAME, required = false) String refreshToken,
            @RequestHeader(value = RefreshCookieManager.REFRESH_GUARD_HEADER, required = false) String refreshGuard,
            HttpServletResponse response
    ) {
        requireRefreshGuard(refreshGuard);
        refreshCookieManager.clearAppCookie(response);
        refreshCookieManager.applyNoStore(response);
        return CommonResult.ok(!StringUtils.hasText(refreshToken) || appAuthService.logoutSession(refreshToken));
    }

    @PostMapping("/logout")
    public CommonResult<Boolean> logout(@RequestBody AppLogoutRequest request, Authentication authentication) {
        return CommonResult.ok(appAuthService.logout(authentication.getName(), request.getDeviceId()));
    }

    @GetMapping("/me")
    public CommonResult<AppCurrentAccountDTO> me(Authentication authentication) {
        String deviceId = null;
        if (authentication.getDetails() instanceof JwtClaims claims) {
            deviceId = claims.getDeviceId();
        }
        return CommonResult.ok(appAuthService.getCurrentAccount(authentication.getName(), deviceId));
    }

    private void requireRefreshGuard(String refreshGuard) {
        if (!"1".equals(refreshGuard)) {
            throw new BusinessException(403, "FORBIDDEN");
        }
    }
}
