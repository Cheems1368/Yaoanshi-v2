package com.medicine.api.controller;

import com.medicine.api.dto.auth.AppLoginRequest;
import com.medicine.api.dto.auth.SessionTokenDTO;
import com.medicine.api.dto.auth.TokenPair;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.CommonResult;
import com.medicine.config.AuthCookieProperties;
import com.medicine.config.JwtProperties;
import com.medicine.service.auth.AdminAuthService;
import com.medicine.service.auth.AppAuthService;
import com.medicine.service.auth.RefreshCookieManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionControllerTest {

    @Mock
    private AppAuthService appAuthService;

    @Mock
    private AdminAuthService adminAuthService;

    private AppAuthController appAuthController;

    @BeforeEach
    void setUp() {
        AuthCookieProperties properties = new AuthCookieProperties();
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setRefreshTokenSeconds(2592000);
        RefreshCookieManager cookieManager = new RefreshCookieManager(properties, jwtProperties);
        appAuthController = new AppAuthController(appAuthService, cookieManager);
    }

    @Test
    void shouldReturnAccessOnlyDtoAndSetHttpOnlyCookie() {
        TokenPair tokenPair = tokenPair();
        when(appAuthService.login(any(AppLoginRequest.class))).thenReturn(tokenPair);
        MockHttpServletResponse response = new MockHttpServletResponse();

        CommonResult<SessionTokenDTO> result = appAuthController.sessionLogin(new AppLoginRequest(), response);

        assertThat(result.getData().getAccessToken()).isEqualTo("access-token");
        assertThat(result.getData().getDeviceId()).isEqualTo("patient-device");
        assertThat(response.getHeader("Set-Cookie")).contains("yaoanshi_app_refresh=refresh-token", "HttpOnly");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    void shouldRejectRefreshWithoutGuardHeader() {
        assertThatThrownBy(() -> appAuthController.sessionRefresh("refresh-token", null, new MockHttpServletResponse()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FORBIDDEN");
    }

    private TokenPair tokenPair() {
        TokenPair tokenPair = new TokenPair();
        tokenPair.setAccessToken("access-token");
        tokenPair.setRefreshToken("refresh-token");
        tokenPair.setExpiresIn(7200L);
        tokenPair.setRefreshExpiresIn(2592000L);
        tokenPair.setDeviceId("patient-device");
        return tokenPair;
    }
}
