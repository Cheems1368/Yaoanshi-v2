package com.medicine.repository.identity;

import com.medicine.domain.identity.AppAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppAccountMapper {

    @Select("""
            SELECT id, mobile, account_type, status, display_name, last_login_ip, last_login_time,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM app_account
            WHERE id = #{id} AND deleted = 0
            """)
    AppAccount selectById(Long id);

    @Select("""
            SELECT id, mobile, account_type, status, display_name, last_login_ip, last_login_time,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM app_account
            WHERE tenant_id = #{tenantId} AND mobile = #{mobile} AND deleted = 0
            LIMIT 1
            """)
    AppAccount selectActiveByMobile(@Param("tenantId") Long tenantId, @Param("mobile") String mobile);

    @Select("""
            SELECT id, mobile, account_type, status, display_name, last_login_ip, last_login_time,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM app_account
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    AppAccount selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Insert("""
            INSERT INTO app_account (
                mobile, account_type, status, display_name, last_login_ip, last_login_time,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{mobile}, #{accountType}, #{status}, #{displayName}, #{lastLoginIp}, #{lastLoginTime},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppAccount appAccount);

    @Update("""
            UPDATE app_account
            SET display_name = #{displayName},
                account_type = #{accountType},
                last_login_ip = #{lastLoginIp},
                last_login_time = #{lastLoginTime},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE id = #{id}
            """)
    int updateLoginInfo(AppAccount appAccount);
}
