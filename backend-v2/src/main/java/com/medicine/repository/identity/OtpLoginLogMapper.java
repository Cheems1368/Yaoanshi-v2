package com.medicine.repository.identity;

import com.medicine.domain.identity.OtpLoginLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OtpLoginLogMapper {

    @Insert("""
            INSERT INTO otp_login_log (
                mobile, scene, provider, request_id, send_status, verify_status, client_ip, error_code, error_message,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{mobile}, #{scene}, #{provider}, #{requestId}, #{sendStatus}, #{verifyStatus}, #{clientIp}, #{errorCode}, #{errorMessage},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OtpLoginLog log);

    @Update("""
            UPDATE otp_login_log
            SET verify_status = #{verifyStatus},
                error_code = #{errorCode},
                error_message = #{errorMessage},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE request_id = #{requestId}
            """)
    int updateVerifyStatusByRequestId(OtpLoginLog log);
}
