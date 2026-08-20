package com.medicine.service.auth;

import com.medicine.config.AuthCookieProperties;
import com.medicine.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshCookieManagerTest {

    @Test
    void shouldWritePersistentSecureAppCookieAndSessionAdminCookie() {
        AuthCookieProperties properties = new AuthCookieProperties();
        properties.setSecure(true);
        properties.setAppSameSite("None");
        properties.setAdminSameSite("Strict");
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setRefreshTokenSeconds(2592000);
        RefreshCookieManager manager = new RefreshCookieManager(properties, jwtProperties);

        MockHttpServletResponse appResponse = new MockHttpServletResponse();
        manager.writeAppCookie(appResponse, "app-refresh");
        String appCookie = appResponse.getHeader("Set-Cookie");
        assertThat(appCookie).contains("yaoanshi_app_refresh=app-refresh", "Path=/app-api/auth/session",
                "Max-Age=2592000", "Secure", "HttpOnly", "SameSite=None");

        MockHttpServletResponse adminResponse = new MockHttpServletResponse();
        manager.writeAdminCookie(adminResponse, "admin-refresh");
        String adminCookie = adminResponse.getHeader("Set-Cookie");
        assertThat(adminCookie).contains("yaoanshi_admin_refresh=admin-refresh", "Path=/admin-api/system/auth/session",
                "Secure", "HttpOnly", "SameSite=Strict");
        assertThat(adminCookie).doesNotContain("Max-Age");
    }

    @Test
    void shouldClearCookiesAndDisableCaching() {
        AuthCookieProperties properties = new AuthCookieProperties();
        JwtProperties jwtProperties = new JwtProperties();
        RefreshCookieManager manager = new RefreshCookieManager(properties, jwtProperties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        manager.clearAppCookie(response);
        manager.applyNoStore(response);

        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0", "HttpOnly");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
    }
}
