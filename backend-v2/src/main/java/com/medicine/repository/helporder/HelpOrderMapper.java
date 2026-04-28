package com.medicine.repository.helporder;

import com.medicine.domain.helporder.HelpOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HelpOrderMapper {

    @Insert("""
            INSERT INTO help_order (
                order_no, patient_id, submitted_by_type, submitted_by_id, category, title, description, status, priority,
                requires_offline_emergency, current_handler_type, current_handler_id, responsible_pharmacist_id, assigned_doctor_id,
                first_responded_at, closed_at, reject_reason, service_notice,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{orderNo}, #{patientId}, #{submittedByType}, #{submittedById}, #{category}, #{title}, #{description}, #{status}, #{priority},
                #{requiresOfflineEmergency}, #{currentHandlerType}, #{currentHandlerId}, #{responsiblePharmacistId}, #{assignedDoctorId},
                #{firstRespondedAt}, #{closedAt}, #{rejectReason}, #{serviceNotice},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpOrder helpOrder);

    @Select("""
            SELECT ho.id, ho.order_no, ho.patient_id, ho.submitted_by_type, ho.submitted_by_id, ho.category, ho.title, ho.description, ho.status, ho.priority,
                   ho.requires_offline_emergency, ho.current_handler_type, ho.current_handler_id, ho.responsible_pharmacist_id, ho.assigned_doctor_id,
                   ho.first_responded_at, ho.closed_at, ho.reject_reason, ho.service_notice,
                   ho.creator, ho.create_time, ho.updater, ho.update_time, ho.deleted, ho.tenant_id,
                   p.name AS patient_name
            FROM help_order ho
            INNER JOIN patient p ON p.id = ho.patient_id AND p.deleted = 0
            WHERE ho.tenant_id = #{tenantId} AND ho.id = #{id} AND ho.deleted = 0
            LIMIT 1
            """)
    HelpOrder selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT ho.id, ho.order_no, ho.patient_id, ho.submitted_by_type, ho.submitted_by_id, ho.category, ho.title, ho.description, ho.status, ho.priority,
                   ho.requires_offline_emergency, ho.current_handler_type, ho.current_handler_id, ho.responsible_pharmacist_id, ho.assigned_doctor_id,
                   ho.first_responded_at, ho.closed_at, ho.reject_reason, ho.service_notice,
                   ho.creator, ho.create_time, ho.updater, ho.update_time, ho.deleted, ho.tenant_id,
                   p.name AS patient_name
            FROM help_order ho
            INNER JOIN patient p ON p.id = ho.patient_id AND p.deleted = 0
            WHERE ho.tenant_id = #{tenantId} AND ho.deleted = 0
            <if test="patientId != null">
              AND ho.patient_id = #{patientId}
            </if>
            <if test="status != null and status != ''">
              AND ho.status = #{status}
            </if>
            <if test="category != null and category != ''">
              AND ho.category = #{category}
            </if>
            ORDER BY ho.create_time DESC, ho.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<HelpOrder> searchForApp(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("status") String status,
            @Param("category") String category,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM help_order
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="patientId != null">
              AND patient_id = #{patientId}
            </if>
            <if test="status != null and status != ''">
              AND status = #{status}
            </if>
            <if test="category != null and category != ''">
              AND category = #{category}
            </if>
            </script>
            """)
    long countForApp(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("status") String status,
            @Param("category") String category
    );

    @Select("""
            <script>
            SELECT ho.id, ho.order_no, ho.patient_id, ho.submitted_by_type, ho.submitted_by_id, ho.category, ho.title, ho.description, ho.status, ho.priority,
                   ho.requires_offline_emergency, ho.current_handler_type, ho.current_handler_id, ho.responsible_pharmacist_id, ho.assigned_doctor_id,
                   ho.first_responded_at, ho.closed_at, ho.reject_reason, ho.service_notice,
                   ho.creator, ho.create_time, ho.updater, ho.update_time, ho.deleted, ho.tenant_id,
                   p.name AS patient_name
            FROM help_order ho
            INNER JOIN patient p ON p.id = ho.patient_id AND p.deleted = 0
            WHERE ho.tenant_id = #{tenantId} AND ho.deleted = 0
            <if test="statuses != null and statuses.size > 0">
              AND ho.status IN
              <foreach collection="statuses" item="status" open="(" separator="," close=")">
                #{status}
              </foreach>
            </if>
            <if test="scopeType == 'PHARMACIST'">
              AND (
                   ho.responsible_pharmacist_id = #{scopeId}
                   OR (ho.current_handler_type = 'PHARMACIST' AND ho.current_handler_id = #{scopeId})
                   OR (ho.responsible_pharmacist_id IS NULL AND ho.current_handler_id IS NULL)
                   OR ho.status = 'SUBMITTED'
              )
            </if>
            <if test="scopeType == 'DOCTOR'">
              AND (
                   ho.assigned_doctor_id = #{scopeId}
                   OR (ho.assigned_doctor_id IS NULL AND ho.status IN ('WAITING_DOCTOR', 'DOCTOR_IN_PROGRESS'))
              )
            </if>
            ORDER BY ho.create_time DESC, ho.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<HelpOrder> searchForAdmin(
            @Param("tenantId") Long tenantId,
            @Param("scopeType") String scopeType,
            @Param("scopeId") Long scopeId,
            @Param("statuses") List<String> statuses,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM help_order
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="statuses != null and statuses.size > 0">
              AND status IN
              <foreach collection="statuses" item="status" open="(" separator="," close=")">
                #{status}
              </foreach>
            </if>
            <if test="scopeType == 'PHARMACIST'">
              AND (
                   responsible_pharmacist_id = #{scopeId}
                   OR (current_handler_type = 'PHARMACIST' AND current_handler_id = #{scopeId})
                   OR (responsible_pharmacist_id IS NULL AND current_handler_id IS NULL)
                   OR status = 'SUBMITTED'
              )
            </if>
            <if test="scopeType == 'DOCTOR'">
              AND (
                   assigned_doctor_id = #{scopeId}
                   OR (assigned_doctor_id IS NULL AND status IN ('WAITING_DOCTOR', 'DOCTOR_IN_PROGRESS'))
              )
            </if>
            </script>
            """)
    long countForAdmin(
            @Param("tenantId") Long tenantId,
            @Param("scopeType") String scopeType,
            @Param("scopeId") Long scopeId,
            @Param("statuses") List<String> statuses
    );

    @Update("""
            UPDATE help_order
            SET status = #{status},
                current_handler_type = #{currentHandlerType},
                current_handler_id = #{currentHandlerId},
                assigned_doctor_id = #{assignedDoctorId},
                first_responded_at = #{firstRespondedAt},
                closed_at = #{closedAt},
                reject_reason = #{rejectReason},
                service_notice = #{serviceNotice},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateState(HelpOrder helpOrder);
}
