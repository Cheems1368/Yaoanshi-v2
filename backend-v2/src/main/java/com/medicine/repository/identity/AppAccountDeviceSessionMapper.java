package com.medicine.repository.identity;

import com.medicine.domain.identity.AppAccountDeviceSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppAccountDeviceSessionMapper {

    @Select("""
            SELECT id, app_account_id, device_id, device_name, platform, access_token_jti, refresh_token_hash,
                   refresh_token_expires_at, last_seen_at, revoked_at, revoked_reason,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM app_account_device_session
            WHERE tenant_id = #{tenantId} AND app_account_id = #{appAccountId} AND device_id = #{deviceId} AND deleted = 0
            LIMIT 1
            """)
    AppAccountDeviceSession selectByAccountIdAndDeviceId(Long tenantId, Long appAccountId, String deviceId);

    @Insert("""
            INSERT INTO app_account_device_session (
                app_account_id, device_id, device_name, platform, access_token_jti, refresh_token_hash,
                refresh_token_expires_at, last_seen_at, revoked_at, revoked_reason,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{appAccountId}, #{deviceId}, #{deviceName}, #{platform}, #{accessTokenJti}, #{refreshTokenHash},
                #{refreshTokenExpiresAt}, #{lastSeenAt}, #{revokedAt}, #{revokedReason},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppAccountDeviceSession session);

    @Update("""
            UPDATE app_account_device_session
            SET device_name = #{deviceName},
                platform = #{platform},
                access_token_jti = #{accessTokenJti},
                refresh_token_hash = #{refreshTokenHash},
                refresh_token_expires_at = #{refreshTokenExpiresAt},
                last_seen_at = #{lastSeenAt},
                revoked_at = #{revokedAt},
                revoked_reason = #{revokedReason},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE id = #{id}
            """)
    int updateSession(AppAccountDeviceSession session);
}
