package com.medicine.repository.helporder;

import com.medicine.api.dto.helporder.HelpOrderAttachmentDTO;
import com.medicine.domain.helporder.HelpOrderEventAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HelpOrderEventAttachmentMapper {

    @Insert("""
            INSERT INTO help_order_event_attachment (
                help_order_event_id, file_asset_id, attachment_type, sort_order,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{helpOrderEventId}, #{fileAssetId}, #{attachmentType}, #{sortOrder},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpOrderEventAttachment attachment);

    @Select("""
            SELECT id, help_order_event_id, file_asset_id, attachment_type, sort_order,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM help_order_event_attachment
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
              AND deleted = 0
            LIMIT 1
            """)
    HelpOrderEventAttachment selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT hoea.id,
                   hoea.file_asset_id AS fileAssetId,
                   hoea.attachment_type AS attachmentType,
                   fa.original_filename AS originalFilename,
                   fa.mime_type AS mimeType,
                   fa.size_bytes AS sizeBytes,
                   fa.object_key AS downloadUrl
            FROM help_order_event_attachment hoea
            JOIN file_asset fa ON fa.tenant_id = hoea.tenant_id
                AND fa.id = hoea.file_asset_id
                AND fa.deleted = 0
            WHERE hoea.tenant_id = #{tenantId}
              AND hoea.help_order_event_id = #{eventId}
              AND hoea.deleted = 0
            ORDER BY hoea.sort_order ASC, hoea.id ASC
            """)
    List<HelpOrderAttachmentDTO> selectDtosByEventId(@Param("tenantId") Long tenantId, @Param("eventId") Long eventId);
}
