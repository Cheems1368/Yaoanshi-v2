package com.medicine.repository.prescription;

import com.medicine.domain.prescription.PrescriptionOcrRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PrescriptionOcrRecordMapper {

    @Insert("""
            INSERT INTO prescription_ocr_record (
                prescription_id, provider, request_payload_json, response_payload_json, confidence, status, manual_fallback_used, executed_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{prescriptionId}, #{provider}, #{requestPayloadJson}, #{responsePayloadJson}, #{confidence}, #{status}, #{manualFallbackUsed}, #{executedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PrescriptionOcrRecord record);

    @Select("""
            SELECT id, prescription_id, provider, request_payload_json, response_payload_json, confidence, status, manual_fallback_used, executed_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM prescription_ocr_record
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND deleted = 0
            ORDER BY executed_at DESC, id DESC
            LIMIT 1
            """)
    PrescriptionOcrRecord selectLatestByPrescriptionId(@Param("tenantId") Long tenantId, @Param("prescriptionId") Long prescriptionId);
}
