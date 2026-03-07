package com.medicine.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(MedicineDataSourceProperties properties) {
        return DataSourceBuilder.create()
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }

    @Bean("legacyAuthDataSource")
    public DataSource legacyAuthDataSource(
            MedicineDataSourceProperties primaryProperties,
            LegacyAuthDataSourceProperties legacyProperties
    ) {
        return DataSourceBuilder.create()
                .driverClassName(firstNonBlank(legacyProperties.getDriverClassName(), primaryProperties.getDriverClassName()))
                .url(firstNonBlank(legacyProperties.getUrl(), primaryProperties.getUrl()))
                .username(firstNonBlank(legacyProperties.getUsername(), primaryProperties.getUsername()))
                .password(firstNonBlank(legacyProperties.getPassword(), primaryProperties.getPassword()))
                .build();
    }

    @Bean("legacyAuthJdbcTemplate")
    public JdbcTemplate legacyAuthJdbcTemplate(@Qualifier("legacyAuthDataSource") DataSource legacyAuthDataSource) {
        return new JdbcTemplate(legacyAuthDataSource);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }
}
