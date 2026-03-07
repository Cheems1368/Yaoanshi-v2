package com.sqlapk.medicine.framework.test.core.util;

import uk.co.jemos.podam.api.DataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.api.PodamUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间精度截断策略
 * 用于解决PODAM生成的时间精度与数据库不匹配的问题
 *
 * @author 芋道源码
 */
public class TruncatedTimeStrategy extends PodamFactoryImpl {

    /**
     * 时间精度模式
     */
    public enum Precision {
        /**
         * 秒级精度（适用于datetime）
         */
        SECONDS,
        /**
         * 微秒级精度（适用于datetime(6)）
         */
        MICROS
    }

    private final Precision precision;

    public TruncatedTimeStrategy() {
        this(Precision.SECONDS); // 默认使用秒级精度
    }

    public TruncatedTimeStrategy(Precision precision) {
        this.precision = precision;
        initializeTimeManufacturers();
    }

    private void initializeTimeManufacturers() {
        // LocalDateTime - 截断到指定精度
        getStrategy().addOrReplaceTypeManufacturer(LocalDateTime.class,
                (dataProviderStrategy, attributeMetadata, map) -> {
                    LocalDateTime now = LocalDateTime.now();
                    return truncateLocalDateTime(now);
                });

        // Instant - 截断到指定精度
        getStrategy().addOrReplaceTypeManufacturer(Instant.class,
                (dataProviderStrategy, attributeMetadata, map) -> {
                    Instant now = Instant.now();
                    return truncateInstant(now);
                });

        // OffsetDateTime - 截断到指定精度
        getStrategy().addOrReplaceTypeManufacturer(OffsetDateTime.class,
                (dataProviderStrategy, attributeMetadata, map) -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    return truncateOffsetDateTime(now);
                });

        // LocalTime - 截断到指定精度
        getStrategy().addOrReplaceTypeManufacturer(LocalTime.class,
                (dataProviderStrategy, attributeMetadata, map) -> {
                    LocalTime now = LocalTime.now();
                    return truncateLocalTime(now);
                });
    }

    private LocalDateTime truncateLocalDateTime(LocalDateTime dateTime) {
        if (precision == Precision.SECONDS) {
            return dateTime.truncatedTo(ChronoUnit.SECONDS);
        } else {
            return dateTime.truncatedTo(ChronoUnit.MICROS);
        }
    }

    private Instant truncateInstant(Instant instant) {
        if (precision == Precision.SECONDS) {
            return instant.truncatedTo(ChronoUnit.SECONDS);
        } else {
            return instant.truncatedTo(ChronoUnit.MICROS);
        }
    }

    private OffsetDateTime truncateOffsetDateTime(OffsetDateTime dateTime) {
        if (precision == Precision.SECONDS) {
            return dateTime.truncatedTo(ChronoUnit.SECONDS);
        } else {
            return dateTime.truncatedTo(ChronoUnit.MICROS);
        }
    }

    private LocalTime truncateLocalTime(LocalTime time) {
        if (precision == Precision.SECONDS) {
            return time.truncatedTo(ChronoUnit.SECONDS);
        } else {
            return time.truncatedTo(ChronoUnit.MICROS);
        }
    }

    /**
     * 获取当前精度模式
     */
    public Precision getPrecision() {
        return precision;
    }
}
