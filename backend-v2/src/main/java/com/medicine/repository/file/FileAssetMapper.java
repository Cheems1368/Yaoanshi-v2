package com.medicine.repository.file;

import com.medicine.domain.file.FileAsset;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileAssetMapper {

    @Insert("""
            INSERT INTO file_asset (
                bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{bucket}, #{objectKey}, #{originalFilename}, #{mimeType}, #{sizeBytes}, #{sha256}, #{storageProvider}, #{storageRegion},
                #{publicUrl}, #{uploadedByType}, #{uploadedById}, #{businessType}, #{scanStatus},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FileAsset fileAsset);

    @Select("""
            SELECT id, bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                   public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM file_asset
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    FileAsset selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
