package com.medicine.service.auth;

import com.medicine.config.AuthCookieProperties;
import com.medicine.config.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieManager {

    public static final String APP_COOKIE_NAME = "yaoanshi_app_refresh";
    public static final String ADMIN_COOKIE_NAME = "yaoanshi_admin_refresh";
    public static final String REFRESH_GUARD_HEADER = "X-Auth-Refresh";
    public static final String APP_COOKIE_PATH = "/app-api/auth/session";
    public static final String ADMIN_COOKIE_PATH = "/admin-api/system/auth/session";

    private final AuthCookieProperties properties;
    private final JwtProperties jwtProperties;

    public RefreshCookieManager(AuthCookieProperties properties, JwtProperties jwtProperties) {
        this.properties = properties;
        this.jwtProperties = jwtProperties;
    }

    public void writeAppCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, APP_COOKIE_NAME, APP_COOKIE_PATH, properties.getAppSameSite(), refreshToken,
                Duration.ofSeconds(jwtProperties.getRefreshTokenSeconds()));
    }

    public void writeAdminCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, ADMIN_COOKIE_NAME, ADMIN_COOKIE_PATH, properties.getAdminSameSite(), refreshToken, null);
    }

    public void clearAppCookie(HttpServletResponse response) {
        addCookie(response, APP_COOKIE_NAME, APP_COOKIE_PATH, properties.getAppSameSite(), "", Duration.ZERO);
    }

    public void clearAdminCookie(HttpServletResponse response) {
        addCookie(response, ADMIN_COOKIE_NAME, ADMIN_COOKIE_PATH, properties.getAdminSameSite(), "", Duration.ZERO);
    }

    public void applyNoStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String path,
            String sameSite,
            String value,
            Duration maxAge
    ) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(sameSite)
                .path(path);
        if (maxAge != null) {
            builder.maxAge(maxAge);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
