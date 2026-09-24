package com.medicine.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AppNotificationWebSocketPropertiesTest {

    @Test
    void shouldAllowPackagedAndLocalDebugOriginsByDefault() {
        assertThat(new AppNotificationWebSocketProperties().getAllowedOrigins())
                .contains("https://localhost", "http://localhost")
                .doesNotContain("https://evil.example");

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties properties = yaml.getObject();
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("medicine.app-notifications.websocket.allowed-origins"))
                .contains("https://localhost,http://localhost,");
    }

    @Test
    void shouldRestrictProductionDefaultToPackagedHttpsOrigin() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-prod.yml"));
        Properties properties = yaml.getObject();
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("medicine.app-notifications.websocket.allowed-origins"))
                .isEqualTo("${MEDICINE_APP_WS_ALLOWED_ORIGINS:https://localhost}");
    }
}
