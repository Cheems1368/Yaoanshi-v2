package com.medicine.repository.notification;

import com.medicine.domain.notification.NotificationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationLogMapper {

    @Insert("""
            INSERT INTO notification_log (
                target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status, failure_code, failure_reason, payload_json,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{targetType}, #{targetId}, #{channel}, #{businessType}, #{businessId}, #{scheduledAt}, #{sentAt}, #{deliveryStatus}, #{failureCode}, #{failureReason}, #{payloadJson},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NotificationLog log);

    @Select("""
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status, failure_code, failure_reason, payload_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM notification_log
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    NotificationLog selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT id, target_type, target_id, channel, business_type, business_id, scheduled_at, sent_at, delivery_status, failure_code, failure_reason, payload_json,
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
                failure_code = #{failureCode},
                failure_reason = #{failureReason},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateDelivery(NotificationLog log);
}
