package com.medicine.repository.identity;

import com.medicine.domain.identity.ProfessionalTokenRecord;
import com.medicine.domain.identity.SystemUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProfessionalIdentityRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfessionalIdentityRepository(@Qualifier("legacyAuthJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProfessionalTokenRecord selectActiveProfessionalToken(String accessToken, Instant now) {
        List<ProfessionalTokenRecord> rows = jdbcTemplate.query(
                """
                SELECT id, user_id, user_type, access_token, refresh_token, client_id, scopes, expires_time, deleted, tenant_id
                FROM system_oauth2_access_token
                WHERE access_token = ?
                  AND deleted = 0
                  AND expires_time > FROM_UNIXTIME(?)
                ORDER BY id DESC
                LIMIT 1
                """,
                (rs, rowNum) -> mapTokenRecord(rs),
                accessToken,
                now.getEpochSecond()
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public SystemUser selectActiveSystemUser(Long tenantId, Long userId) {
        List<SystemUser> rows = jdbcTemplate.query(
                """
                SELECT id, username, password, nickname, avatar, mobile, status, deleted, tenant_id
                FROM system_users
                WHERE tenant_id = ?
                  AND id = ?
                  AND status = 0
                  AND deleted = 0
                LIMIT 1
                """,
                (rs, rowNum) -> mapSystemUser(rs),
                tenantId,
                userId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public SystemUser selectActiveSystemUserByUsername(Long tenantId, String username) {
        List<SystemUser> rows = jdbcTemplate.query(
                """
                SELECT id, username, password, nickname, avatar, mobile, status, deleted, tenant_id
                FROM system_users
                WHERE tenant_id = ?
                  AND username = ?
                  AND status = 0
                  AND deleted = 0
                ORDER BY id ASC
                LIMIT 1
                """,
                (rs, rowNum) -> mapSystemUser(rs),
                tenantId,
                username
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<String> selectRoleCodesByUserId(Long tenantId, Long userId) {
        return jdbcTemplate.query(
                """
                SELECT r.code
                FROM system_role r
                INNER JOIN system_user_role ur
                        ON ur.role_id = r.id
                       AND ur.deleted = 0
                       AND ur.tenant_id = ?
                WHERE ur.user_id = ?
                  AND r.deleted = 0
                  AND r.status = 0
                  AND r.tenant_id = ?
                ORDER BY r.sort ASC, r.id ASC
                """,
                (rs, rowNum) -> rs.getString("code"),
                tenantId,
                userId,
                tenantId
        );
    }

    public List<String> selectPermissionCodesByUserId(Long tenantId, Long userId) {
        return jdbcTemplate.query(
                """
                SELECT DISTINCT m.permission
                FROM system_menu m
                INNER JOIN system_role_menu rm
                        ON rm.menu_id = m.id
                       AND rm.deleted = 0
                       AND rm.tenant_id = ?
                INNER JOIN system_user_role ur
                        ON ur.role_id = rm.role_id
                       AND ur.deleted = 0
                       AND ur.tenant_id = ?
                INNER JOIN system_role r
                        ON r.id = ur.role_id
                       AND r.deleted = 0
                       AND r.status = 0
                       AND r.tenant_id = ?
                WHERE ur.user_id = ?
                  AND m.deleted = 0
                  AND m.status = 0
                  AND m.permission <> ''
                ORDER BY m.permission ASC
                """,
                (rs, rowNum) -> rs.getString("permission"),
                tenantId,
                tenantId,
                tenantId,
                userId
        );
    }

    private ProfessionalTokenRecord mapTokenRecord(ResultSet rs) throws SQLException {
        ProfessionalTokenRecord record = new ProfessionalTokenRecord();
        record.setId(rs.getLong("id"));
        record.setUserId(rs.getLong("user_id"));
        record.setUserType(rs.getObject("user_type", Integer.class));
        record.setAccessToken(rs.getString("access_token"));
        record.setRefreshToken(rs.getString("refresh_token"));
        record.setClientId(rs.getString("client_id"));
        record.setScopes(rs.getString("scopes"));
        record.setExpiresTime(toLocalDateTime(rs.getTimestamp("expires_time")));
        record.setDeleted(rs.getBoolean("deleted"));
        record.setTenantId(rs.getLong("tenant_id"));
        return record;
    }

    private SystemUser mapSystemUser(ResultSet rs) throws SQLException {
        SystemUser user = new SystemUser();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setAvatar(rs.getString("avatar"));
        user.setMobile(rs.getString("mobile"));
        user.setStatus(rs.getObject("status", Integer.class));
        user.setDeleted(rs.getBoolean("deleted"));
        user.setTenantId(rs.getLong("tenant_id"));
        return user;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
