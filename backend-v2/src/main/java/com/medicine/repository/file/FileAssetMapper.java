package com.medicine.repository.file;

import com.medicine.domain.file.FileAsset;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileAssetMapper {

    @Insert("""
            INSERT INTO file_asset (
                bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                cleanup_attempt_count, next_cleanup_at, cleanup_failure_reason, cleanup_claim_token, cleanup_claimed_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{bucket}, #{objectKey}, #{originalFilename}, #{mimeType}, #{sizeBytes}, #{sha256}, #{storageProvider}, #{storageRegion},
                #{publicUrl}, #{uploadedByType}, #{uploadedById}, #{businessType}, #{scanStatus},
                #{cleanupAttemptCount}, #{nextCleanupAt}, #{cleanupFailureReason}, #{cleanupClaimToken}, #{cleanupClaimedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FileAsset fileAsset);

    @Select("""
            SELECT id, bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                   public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                   cleanup_attempt_count, next_cleanup_at, cleanup_failure_reason, cleanup_claim_token, cleanup_claimed_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM file_asset
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    FileAsset selectByIdAndTenant(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                   public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                   cleanup_attempt_count, next_cleanup_at, cleanup_failure_reason, cleanup_claim_token, cleanup_claimed_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM file_asset
            WHERE tenant_id = #{tenantId} AND object_key = #{objectKey} AND deleted = 0
            ORDER BY id ASC
            LIMIT 1
            """)
    FileAsset selectByObjectKeyAndTenant(@Param("tenantId") Long tenantId, @Param("objectKey") String objectKey);

    @Update("""
            UPDATE file_asset SET sha256 = #{sha256}, public_url = #{publicUrl}, scan_status = #{scanStatus}, updater = #{updater}, update_time = #{updateTime},
                next_cleanup_at = NULL, cleanup_failure_reason = NULL, cleanup_claim_token = NULL, cleanup_claimed_at = NULL
            WHERE tenant_id = #{tenantId} AND id = #{id} AND scan_status = 'PRESIGNED'
              AND cleanup_claim_token IS NULL AND deleted = 0
            """)
    int completeRegistration(FileAsset fileAsset);

    @Select("""
            SELECT id, bucket, object_key, original_filename, mime_type, size_bytes, sha256, storage_provider, storage_region,
                   public_url, uploaded_by_type, uploaded_by_id, business_type, scan_status,
                   cleanup_attempt_count, next_cleanup_at, cleanup_failure_reason, cleanup_claim_token, cleanup_claimed_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM file_asset
            WHERE scan_status IN ('PRESIGNED', 'CLEANUP_RETRY') AND deleted = 0 AND create_time < #{createdBefore}
              AND (next_cleanup_at IS NULL OR next_cleanup_at <= #{now})
              AND (cleanup_claimed_at IS NULL OR cleanup_claimed_at < #{staleBefore})
            ORDER BY create_time ASC, id ASC LIMIT #{limit}
            """)
    List<FileAsset> selectCleanupCandidates(@Param("createdBefore") LocalDateTime createdBefore,
                                             @Param("now") LocalDateTime now,
                                             @Param("staleBefore") LocalDateTime staleBefore,
                                             @Param("limit") int limit);

    @Update("""
            UPDATE file_asset SET cleanup_claim_token = #{claimToken}, cleanup_claimed_at = #{now},
                updater = 'file-cleanup', update_time = #{now}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
              AND scan_status IN ('PRESIGNED', 'CLEANUP_RETRY')
              AND (cleanup_claimed_at IS NULL OR cleanup_claimed_at < #{staleBefore})
            """)
    int claimCleanup(@Param("tenantId") Long tenantId, @Param("id") Long id,
                     @Param("claimToken") String claimToken, @Param("now") LocalDateTime now,
                     @Param("staleBefore") LocalDateTime staleBefore);

    @Update("""
            UPDATE file_asset SET scan_status = 'ORPHAN_CLEANED', deleted = b'1', cleanup_claim_token = NULL,
                cleanup_claimed_at = NULL, updater = 'file-cleanup', update_time = #{now}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND cleanup_claim_token = #{claimToken} AND deleted = 0
            """)
    int markCleanupSucceeded(@Param("tenantId") Long tenantId, @Param("id") Long id,
                             @Param("claimToken") String claimToken, @Param("now") LocalDateTime now);

    @Update("""
            UPDATE file_asset SET scan_status = 'CLEANUP_RETRY', cleanup_attempt_count = cleanup_attempt_count + 1,
                next_cleanup_at = #{nextCleanupAt}, cleanup_failure_reason = #{reason}, cleanup_claim_token = NULL,
                cleanup_claimed_at = NULL, updater = 'file-cleanup', update_time = #{now}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND cleanup_claim_token = #{claimToken} AND deleted = 0
            """)
    int markCleanupFailed(@Param("tenantId") Long tenantId, @Param("id") Long id,
                          @Param("claimToken") String claimToken, @Param("nextCleanupAt") LocalDateTime nextCleanupAt,
                          @Param("reason") String reason, @Param("now") LocalDateTime now);
}
