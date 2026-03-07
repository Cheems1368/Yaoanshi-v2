package com.medicine.repository.prescription;

import com.medicine.domain.prescription.PrescriptionAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PrescriptionAttachmentMapper {

    @Insert("""
            INSERT INTO prescription_attachment (
                prescription_id, file_asset_id, attachment_type,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{prescriptionId}, #{fileAssetId}, #{attachmentType},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PrescriptionAttachment attachment);

    @Select("""
            SELECT id, prescription_id, file_asset_id, attachment_type,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM prescription_attachment
            WHERE tenant_id = #{tenantId} AND prescription_id = #{prescriptionId} AND deleted = 0
            ORDER BY id ASC
            """)
    List<PrescriptionAttachment> selectByPrescriptionId(@Param("tenantId") Long tenantId, @Param("prescriptionId") Long prescriptionId);
}
