package com.medicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "medicine.auth.cookies")
public class AuthCookieProperties {

    private boolean secure;
    private String appSameSite = "Lax";
    private String adminSameSite = "Lax";

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public String getAppSameSite() { return appSameSite; }
    public void setAppSameSite(String appSameSite) { this.appSameSite = appSameSite; }
    public String getAdminSameSite() { return adminSameSite; }
    public void setAdminSameSite(String adminSameSite) { this.adminSameSite = adminSameSite; }
}
