package com.sqlapk.medicine.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "medicine.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * HTTP 请求时，访问令牌的请求 Header
     */
    @NotEmpty(message = "Token Header 不能为空")
    private String tokenHeader = "Authorization";

    /**
     * mock 模式的开关
     */
    @NotNull(message = "mock 模式的开关不能为空")
    private Boolean mockEnable = false;
    
    /**
     * mock 模式的密钥
     * 一定要配置密钥，保证安全性
     */
    @NotEmpty(message = "mock 模式的密钥不能为空") // 这里设置了一个默认值，因为实际上只有 mockEnable 为 true 时才需要配置
    private String mockSecret = "${MOCK_SECRET:test}";

    /**
     * 免登录的 URL 列表
     */
    private List<String> permitAllUrls = Collections.emptyList();

    /**
     * PasswordEncoder 加密复杂度，越高开销越大
     */
    private Integer passwordEncoderLength = 10;

    /**
     * 密码策略配置
     */
    private PasswordPolicy passwordPolicy = new PasswordPolicy();

    /**
     * 密码策略
     */
    @Data
    public static class PasswordPolicy {
        /**
         * 最小长度
         */
        private Integer minLength = 12;

        /**
         * 最大长度
         */
        private Integer maxLength = 128;

        /**
         * 需要大写字母
         */
        private Boolean requireUppercase = true;

        /**
         * 需要小写字母
         */
        private Boolean requireLowercase = true;

        /**
         * 需要数字
         */
        private Boolean requireDigit = true;

        /**
         * 需要特殊字符
         */
        private Boolean requireSpecialChar = true;

        /**
         * 密码有效期（天）
         */
        private Integer expirationDays = 90;

        /**
         * 登录失败锁定次数
         */
        private Integer maxLoginAttempts = 5;

        /**
         * 锁定时间（分钟）
         */
        private Integer lockoutDuration = 30;
    }
}
