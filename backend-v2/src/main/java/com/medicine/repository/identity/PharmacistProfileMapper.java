package com.medicine.repository.identity;

import com.medicine.domain.identity.PharmacistProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PharmacistProfileMapper {

    @Select("""
            SELECT id, system_user_id, license_no, real_name, mobile, service_status, work_hours_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM pharmacist_profile
            WHERE tenant_id = #{tenantId} AND system_user_id = #{systemUserId} AND deleted = 0
            LIMIT 1
            """)
    PharmacistProfile selectBySystemUserId(@Param("tenantId") Long tenantId, @Param("systemUserId") Long systemUserId);

    @Select("""
            SELECT id, system_user_id, license_no, real_name, mobile, service_status, work_hours_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM pharmacist_profile
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    PharmacistProfile selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, system_user_id, license_no, real_name, mobile, service_status, work_hours_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM pharmacist_profile
            WHERE tenant_id = #{tenantId} AND service_status = 'ACTIVE' AND deleted = 0
            ORDER BY real_name ASC, id ASC
            """)
    List<PharmacistProfile> selectActive(@Param("tenantId") Long tenantId);

    @Insert("""
            INSERT INTO pharmacist_profile (
                system_user_id, license_no, real_name, mobile, service_status, work_hours_json,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{systemUserId}, #{licenseNo}, #{realName}, #{mobile}, #{serviceStatus}, #{workHoursJson},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PharmacistProfile profile);
}
