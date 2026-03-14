package com.medicine.repository.identity;

import com.medicine.domain.identity.DoctorProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DoctorProfileMapper {

    @Select("""
            SELECT id, system_user_id, specialty_code, title_name, real_name, mobile, service_status, work_hours_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM doctor_profile
            WHERE tenant_id = #{tenantId} AND system_user_id = #{systemUserId} AND deleted = 0
            LIMIT 1
            """)
    DoctorProfile selectBySystemUserId(@Param("tenantId") Long tenantId, @Param("systemUserId") Long systemUserId);

    @Select("""
            SELECT id, system_user_id, specialty_code, title_name, real_name, mobile, service_status, work_hours_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM doctor_profile
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    DoctorProfile selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Insert("""
            INSERT INTO doctor_profile (
                system_user_id, specialty_code, title_name, real_name, mobile, service_status, work_hours_json,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{systemUserId}, #{specialtyCode}, #{titleName}, #{realName}, #{mobile}, #{serviceStatus}, #{workHoursJson},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DoctorProfile doctorProfile);
}
