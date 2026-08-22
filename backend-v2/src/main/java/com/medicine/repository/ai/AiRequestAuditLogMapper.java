package com.medicine.repository.ai;

import com.medicine.domain.ai.AiRequestAuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiRequestAuditLogMapper {

    @Insert("""
            INSERT INTO ai_request_audit_log (
                account_id, request_id, provider_code, provider_host, model, request_class,
                original_message_count, transmitted_message_count, original_char_count,
                transmitted_char_count, redaction_count, status, error_code, latency_ms, expires_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{accountId}, #{requestId}, #{providerCode}, #{providerHost}, #{model}, #{requestClass},
                #{originalMessageCount}, #{transmittedMessageCount}, #{originalCharCount},
                #{transmittedCharCount}, #{redactionCount}, #{status}, #{errorCode}, #{latencyMs}, #{expiresAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiRequestAuditLog log);

    @Update("""
            UPDATE ai_request_audit_log
            SET status = #{status},
                error_code = #{errorCode},
                latency_ms = #{latencyMs},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateOutcome(AiRequestAuditLog log);
}
