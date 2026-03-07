package com.sqlapk.medicine.module.medication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 集成服务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "medicine.integration")
public class IntegrationConfig {

    /**
     * 患者档案集成配置
     */
    private PatientIntegration patient = new PatientIntegration();

    /**
     * 药品信息集成配置
     */
    private DrugIntegration drug = new DrugIntegration();

    /**
     * 用户权限集成配置
     */
    private UserPermissionIntegration userPermission = new UserPermissionIntegration();

    /**
     * 通用集成配置
     */
    private CommonIntegration common = new CommonIntegration();

    @Data
    public static class PatientIntegration {
        /**
         * 患者档案服务地址
         */
        private String baseUrl = "http://localhost:8080";

        /**
         * 患者档案服务路径
         */
        private String path = "/api/patient";

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryCount = 3;

        /**
         * 是否启用患者档案集成
         */
        private boolean enabled = false;

        /**
         * 是否启用缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private int cacheExpireSeconds = 300;
    }

    @Data
    public static class DrugIntegration {
        /**
         * 药品信息服务地址
         */
        private String baseUrl = "http://localhost:8080";

        /**
         * 药品信息服务路径
         */
        private String path = "/api/drug";

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryCount = 3;

        /**
         * 是否启用药品信息集成
         */
        private boolean enabled = false;

        /**
         * 是否启用缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private int cacheExpireSeconds = 600;

        /**
         * 过敏检查服务地址
         */
        private String allergyCheckUrl = "http://localhost:8080";

        /**
         * 过敏检查服务路径
         */
        private String allergyCheckPath = "/api/allergy-check";
    }

    @Data
    public static class UserPermissionIntegration {
        /**
         * 用户权限服务地址
         */
        private String baseUrl = "http://localhost:8080";

        /**
         * 用户权限服务路径
         */
        private String path = "/api/user-permission";

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryCount = 3;

        /**
         * 是否启用用户权限集成
         */
        private boolean enabled = false;

        /**
         * 是否启用缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private int cacheExpireSeconds = 180;

        /**
         * 权限检查服务地址
         */
        private String permissionCheckUrl = "http://localhost:8080";

        /**
         * 权限检查服务路径
         */
        private String permissionCheckPath = "/api/permission-check";
    }

    @Data
    public static class CommonIntegration {
        /**
         * 是否启用集成服务
         */
        private boolean enabled = false;

        /**
         * 是否启用降级模式
         */
        private boolean fallbackEnabled = true;

        /**
         * 降级模式超时时间（毫秒）
         */
        private int fallbackTimeout = 2000;

        /**
         * 是否启用集成服务监控
         */
        private boolean monitoringEnabled = true;

        /**
         * 监控数据保留时间（小时）
         */
        private int monitoringRetentionHours = 24;

        /**
         * 是否启用集成服务日志
         */
        private boolean loggingEnabled = true;

        /**
         * 日志级别
         */
        private String logLevel = "INFO";

        /**
         * 是否启用集成服务统计
         */
        private boolean statisticsEnabled = true;

        /**
         * 统计数据保留时间（小时）
         */
        private int statisticsRetentionHours = 168; // 7天
    }
}
