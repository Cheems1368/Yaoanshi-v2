package com.medicine.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AdminNotificationWebSocketProperties.class,
        AppNotificationWebSocketProperties.class,
        AiProperties.class,
        JwtProperties.class,
        LegacyAuthDataSourceProperties.class,
        MedicineDataSourceProperties.class,
        ObjectStorageProperties.class,
        OcrProperties.class,
        SpeechProperties.class
})
public class BackendPropertiesConfig {
}
