package com.medicine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI backendV2OpenApi() {
        return new OpenAPI().info(new Info()
                .title("Yaoshian MVP API v1")
                .version("v1")
                .description("backend-v2 baseline API"));
    }
}
