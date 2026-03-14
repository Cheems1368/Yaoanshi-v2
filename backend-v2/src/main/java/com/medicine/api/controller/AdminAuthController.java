package com.medicine.api.controller;

import com.medicine.api.dto.auth.AdminLoginRequest;
import com.medicine.api.dto.auth.AdminPermissionInfoDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.AdminAuthService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
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

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
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
}
