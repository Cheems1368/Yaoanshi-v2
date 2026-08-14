package com.medicine.api.controller;

import com.medicine.api.dto.auth.AppLoginRequest;
import com.medicine.api.dto.auth.AppLogoutRequest;
import com.medicine.api.dto.auth.OtpSendRequest;
import com.medicine.api.dto.auth.RefreshTokenRequest;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.api.dto.identity.AppCurrentAccountDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.AppAuthService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/auth")
public class AppAuthController {

    private final AppAuthService appAuthService;

    public AppAuthController(AppAuthService appAuthService) {
        this.appAuthService = appAuthService;
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
}
