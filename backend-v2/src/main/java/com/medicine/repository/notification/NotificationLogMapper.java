package com.medicine.repository.notification;

import com.medicine.domain.notification.NotificationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationLogMapper {

    @Insert("""
            INSERT INTO notification_log (
                target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{targetType}, #{targetId}, #{channel}, #{businessType}, #{businessId}, #{scheduledAt}, #{sentAt}, #{deliveryStatus},
                #{attemptCount}, #{nextRetryAt}, #{lastAttemptAt}, #{claimToken}, #{claimedAt}, #{failureCode}, #{failureReason}, #{payloadJson},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NotificationLog log);

    @Select("""
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                   attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    NotificationLog selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                   attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="businessType != null and businessType != ''">
              AND business_type = #{businessType}
            </if>
            <if test="deliveryStatus != null and deliveryStatus != ''">
              AND delivery_status = #{deliveryStatus}
            </if>
            ORDER BY create_time DESC, id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<NotificationLog> search(
            @Param("tenantId") Long tenantId,
            @Param("businessType") String businessType,
            @Param("deliveryStatus") String deliveryStatus,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="businessType != null and businessType != ''">
              AND business_type = #{businessType}
            </if>
            <if test="deliveryStatus != null and deliveryStatus != ''">
              AND delivery_status = #{deliveryStatus}
            </if>
            </script>
            """)
    long count(
            @Param("tenantId") Long tenantId,
            @Param("businessType") String businessType,
            @Param("deliveryStatus") String deliveryStatus
    );

    @Update("""
            UPDATE notification_log
            SET channel = #{channel},
                sent_at = #{sentAt},
                delivery_status = #{deliveryStatus},
                attempt_count = #{attemptCount},
                next_retry_at = #{nextRetryAt},
                last_attempt_at = #{lastAttemptAt},
                claim_token = #{claimToken},
                claimed_at = #{claimedAt},
                failure_code = #{failureCode},
                failure_reason = #{failureReason},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateDelivery(NotificationLog log);

    @Update("""
            UPDATE notification_log
            SET channel = #{log.channel},
                sent_at = #{log.sentAt},
                delivery_status = #{log.deliveryStatus},
                attempt_count = #{log.attemptCount},
                next_retry_at = #{log.nextRetryAt},
                last_attempt_at = #{log.lastAttemptAt},
                claim_token = NULL,
                claimed_at = NULL,
                failure_code = #{log.failureCode},
                failure_reason = #{log.failureReason},
                updater = #{log.updater},
                update_time = #{log.updateTime}
            WHERE tenant_id = #{log.tenantId} AND id = #{log.id} AND claim_token = #{expectedClaimToken} AND deleted = 0
            """)
    int updateClaimedDelivery(@Param("log") NotificationLog log, @Param("expectedClaimToken") String expectedClaimToken);

    @Select("""
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                   attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND target_type = 'PATIENT' AND target_id = #{patientId} AND deleted = 0
              AND (#{afterId} IS NULL OR id > #{afterId})
            ORDER BY id ASC
            LIMIT #{limit}
            """)
    List<NotificationLog> selectPatientLogs(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId,
                                             @Param("afterId") Long afterId, @Param("limit") int limit);

    @Select("""
            SELECT MAX(id)
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND target_type = 'PATIENT' AND target_id = #{patientId} AND deleted = 0
            """)
    Long selectLatestPatientLogId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    @Update("""
            UPDATE notification_log
            SET claim_token = #{claimToken}, claimed_at = #{now}, updater = 'notification-retry', update_time = #{now}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND delivery_status = 'FAILED' AND deleted = 0
              AND next_retry_at <= #{now} AND (claimed_at IS NULL OR claimed_at < #{staleBefore})
            """)
    int claimRetry(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("claimToken") String claimToken,
                   @Param("now") LocalDateTime now, @Param("staleBefore") LocalDateTime staleBefore);

    @Select("""
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                   attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log
            WHERE delivery_status = 'FAILED' AND next_retry_at <= #{now} AND deleted = 0
              AND (claimed_at IS NULL OR claimed_at < #{staleBefore})
            ORDER BY next_retry_at ASC, id ASC LIMIT #{limit}
            """)
    List<NotificationLog> selectRetryCandidates(@Param("now") LocalDateTime now,
                                                 @Param("staleBefore") LocalDateTime staleBefore, @Param("limit") int limit);

    @Select("""
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status,
                   attempt_count, next_retry_at, last_attempt_at, claim_token, claimed_at, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log WHERE id = #{id} AND claim_token = #{claimToken} AND deleted = 0 LIMIT 1
            """)
    NotificationLog selectClaimed(@Param("id") Long id, @Param("claimToken") String claimToken);
}
