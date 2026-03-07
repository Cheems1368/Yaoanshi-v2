package com.sqlapk.medicine.framework.test.core.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TruncatedTimeStrategy 测试类
 */
public class TruncatedTimeStrategyTest {

    @Test
    public void testSecondsPrecision() {
        TruncatedTimeStrategy strategy = new TruncatedTimeStrategy(TruncatedTimeStrategy.Precision.SECONDS);
        
        // 测试 LocalDateTime
        LocalDateTime dateTime = strategy.manufacturePojo(LocalDateTime.class);
        assertEquals(0, dateTime.getNano(), "LocalDateTime should be truncated to seconds");
        
        // 测试 Instant
        Instant instant = strategy.manufacturePojo(Instant.class);
        assertEquals(0, instant.getNano(), "Instant should be truncated to seconds");
        
        // 测试 OffsetDateTime
        OffsetDateTime offsetDateTime = strategy.manufacturePojo(OffsetDateTime.class);
        assertEquals(0, offsetDateTime.getNano(), "OffsetDateTime should be truncated to seconds");
        
        // 测试 LocalTime
        LocalTime time = strategy.manufacturePojo(LocalTime.class);
        assertEquals(0, time.getNano(), "LocalTime should be truncated to seconds");
    }

    @Test
    public void testMicrosPrecision() {
        TruncatedTimeStrategy strategy = new TruncatedTimeStrategy(TruncatedTimeStrategy.Precision.MICROS);
        
        // 测试 LocalDateTime
        LocalDateTime dateTime = strategy.manufacturePojo(LocalDateTime.class);
        assertTrue(dateTime.getNano() % 1000 == 0, "LocalDateTime should be truncated to microseconds");
        
        // 测试 Instant
        Instant instant = strategy.manufacturePojo(Instant.class);
        assertTrue(instant.getNano() % 1000 == 0, "Instant should be truncated to microseconds");
        
        // 测试 OffsetDateTime
        OffsetDateTime offsetDateTime = strategy.manufacturePojo(OffsetDateTime.class);
        assertTrue(offsetDateTime.getNano() % 1000 == 0, "OffsetDateTime should be truncated to microseconds");
        
        // 测试 LocalTime
        LocalTime time = strategy.manufacturePojo(LocalTime.class);
        assertTrue(time.getNano() % 1000 == 0, "LocalTime should be truncated to microseconds");
    }

    @Test
    public void testDefaultPrecision() {
        TruncatedTimeStrategy strategy = new TruncatedTimeStrategy();
        assertEquals(TruncatedTimeStrategy.Precision.SECONDS, strategy.getPrecision());
    }

    @Test
    public void testRandomUtilsIntegration() {
        // 测试 RandomUtils 是否使用了 TruncatedTimeStrategy
        LocalDateTime dateTime = RandomUtils.randomPojo(LocalDateTime.class);
        assertEquals(0, dateTime.getNano(), "RandomUtils should use TruncatedTimeStrategy");
    }
}
