package com.medicine.repository.prescription;

import com.medicine.domain.prescription.Prescription;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PrescriptionMapper {

    @Insert("""
            INSERT INTO prescription (
                patient_id, source_type, status, ocr_confidence, uploaded_file_asset_id, submitted_by_type, submitted_by_id,
                reviewed_by_pharmacist_id, reviewed_at, reject_reason, generated_medication_plan_id,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{patientId}, #{sourceType}, #{status}, #{ocrConfidence}, #{uploadedFileAssetId}, #{submittedByType}, #{submittedById},
                #{reviewedByPharmacistId}, #{reviewedAt}, #{rejectReason}, #{generatedMedicationPlanId},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Prescription prescription);

    @Select("""
            SELECT pr.id, pr.patient_id, pr.source_type, pr.status, pr.ocr_confidence, pr.uploaded_file_asset_id, pr.submitted_by_type, pr.submitted_by_id,
                   pr.reviewed_by_pharmacist_id, pr.reviewed_at, pr.reject_reason, pr.generated_medication_plan_id,
                   pr.creator, pr.create_time, pr.updater, pr.update_time, pr.deleted, pr.tenant_id,
                   p.name AS patient_name, p.hospital_patient_id AS hospital_patient_id
            FROM prescription pr
            INNER JOIN patient p ON p.id = pr.patient_id AND p.deleted = 0
            WHERE pr.tenant_id = #{tenantId} AND pr.id = #{id} AND pr.deleted = 0
            LIMIT 1
            """)
    Prescription selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT pr.id, pr.patient_id, pr.source_type, pr.status, pr.ocr_confidence, pr.uploaded_file_asset_id, pr.submitted_by_type, pr.submitted_by_id,
                   pr.reviewed_by_pharmacist_id, pr.reviewed_at, pr.reject_reason, pr.generated_medication_plan_id,
                   pr.creator, pr.create_time, pr.updater, pr.update_time, pr.deleted, pr.tenant_id,
                   p.name AS patient_name, p.hospital_patient_id AS hospital_patient_id
            FROM prescription pr
            INNER JOIN patient p ON p.id = pr.patient_id AND p.deleted = 0
            WHERE pr.tenant_id = #{tenantId}
              AND pr.deleted = 0
            <if test="status != null and status != ''">
              AND pr.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (
                   p.name LIKE CONCAT('%', #{keyword}, '%')
                OR p.hospital_patient_id LIKE CONCAT('%', #{keyword}, '%')
                OR CAST(pr.id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY pr.create_time DESC, pr.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<Prescription> searchForAdmin(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM prescription pr
            INNER JOIN patient p ON p.id = pr.patient_id AND p.deleted = 0
            WHERE pr.tenant_id = #{tenantId}
              AND pr.deleted = 0
            <if test="status != null and status != ''">
              AND pr.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (
                   p.name LIKE CONCAT('%', #{keyword}, '%')
                OR p.hospital_patient_id LIKE CONCAT('%', #{keyword}, '%')
                OR CAST(pr.id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    long countForAdmin(@Param("tenantId") Long tenantId, @Param("keyword") String keyword, @Param("status") String status);

    @Select("""
            <script>
            SELECT pr.id, pr.patient_id, pr.source_type, pr.status, pr.ocr_confidence, pr.uploaded_file_asset_id, pr.submitted_by_type, pr.submitted_by_id,
                   pr.reviewed_by_pharmacist_id, pr.reviewed_at, pr.reject_reason, pr.generated_medication_plan_id,
                   pr.creator, pr.create_time, pr.updater, pr.update_time, pr.deleted, pr.tenant_id,
                   p.name AS patient_name, p.hospital_patient_id AS hospital_patient_id
            FROM prescription pr
            INNER JOIN patient p ON p.id = pr.patient_id AND p.deleted = 0
            WHERE pr.tenant_id = #{tenantId}
              AND pr.patient_id = #{patientId}
              AND pr.deleted = 0
            <if test="status != null and status != ''">
              AND pr.status = #{status}
            </if>
            ORDER BY pr.create_time DESC, pr.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<Prescription> searchForPatient(
            @Param("tenantId") Long tenantId,
            @Param("patientId") Long patientId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM prescription
            WHERE tenant_id = #{tenantId}
              AND patient_id = #{patientId}
              AND deleted = 0
            <if test="status != null and status != ''">
              AND status = #{status}
            </if>
            </script>
            """)
    long countForPatient(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId, @Param("status") String status);

    @Update("""
            UPDATE prescription
            SET status = #{status},
                reviewed_by_pharmacist_id = #{reviewedByPharmacistId},
                reviewed_at = #{reviewedAt},
                reject_reason = #{rejectReason},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateReviewResult(Prescription prescription);

    @Update("""
            UPDATE prescription
            SET status = #{status},
                ocr_confidence = #{ocrConfidence},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int updateOcrResult(Prescription prescription);
}
