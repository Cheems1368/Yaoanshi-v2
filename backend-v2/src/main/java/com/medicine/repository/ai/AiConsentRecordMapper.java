package com.medicine.repository.ai;

import com.medicine.domain.ai.AiConsentRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiConsentRecordMapper {

    @Select("""
            SELECT id, account_id, policy_version, provider_code, provider_display_name,
                   statement_hash, granted_at, creator, create_time, updater, update_time, deleted, tenant_id
            FROM ai_consent_record
            WHERE tenant_id = #{tenantId}
              AND account_id = #{accountId}
              AND policy_version = #{policyVersion}
              AND provider_code = #{providerCode}
              AND statement_hash = #{statementHash}
              AND deleted = 0
            LIMIT 1
            """)
    AiConsentRecord selectCurrent(@Param("tenantId") Long tenantId,
                                  @Param("accountId") Long accountId,
                                  @Param("policyVersion") String policyVersion,
                                  @Param("providerCode") String providerCode,
                                  @Param("statementHash") String statementHash);

    @Insert("""
            INSERT IGNORE INTO ai_consent_record (
                account_id, policy_version, provider_code, provider_display_name, statement_hash, granted_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{accountId}, #{policyVersion}, #{providerCode}, #{providerDisplayName}, #{statementHash}, #{grantedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    int insert(AiConsentRecord record);
}
