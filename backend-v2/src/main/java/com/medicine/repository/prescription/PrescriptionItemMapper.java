package com.medicine.repository.prescription;

import com.medicine.domain.prescription.PrescriptionItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PrescriptionItemMapper {

    @Insert("""
            INSERT INTO prescription_item (
                prescription_id, drug_name, drug_spec, dosage, frequency, route, duration_days, normalized_json, source, sort_order,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{prescriptionId}, #{drugName}, #{drugSpec}, #{dosage}, #{frequency}, #{route}, #{durationDays}, #{normalizedJson}, #{source}, #{sortOrder},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PrescriptionItem item);

    @Select("""
            SELECT id, prescription_id, drug_name, drug_spec, dosage, frequency, route, duration_days, normalized_json, source, sort_order,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM prescription_item
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND deleted = 0
            ORDER BY sort_order ASC, id ASC
            """)
    List<PrescriptionItem> selectByPrescriptionId(@Param("tenantId") Long tenantId, @Param("prescriptionId") Long prescriptionId);

    @Update("""
            UPDATE prescription_item
            SET deleted = 1, updater = #{updater}, update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND deleted = 0
            """)
    int softDeleteByPrescriptionId(
            @Param("tenantId") Long tenantId,
            @Param("prescriptionId") Long prescriptionId,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}
