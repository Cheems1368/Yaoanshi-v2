package com.medicine.repository.helporder;

import com.medicine.domain.helporder.HelpOrderVoiceTranscript;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HelpOrderVoiceTranscriptMapper {

    @Insert("""
            INSERT INTO help_order_voice_transcript (
                help_order_id, help_order_event_attachment_id, playback_file_asset_id,
                transcript_source_file_asset_id, duration_seconds, provider, status,
                transcript_text, error_code, transcribed_by_type, transcribed_by_id, transcribed_at,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{helpOrderId}, #{helpOrderEventAttachmentId}, #{playbackFileAssetId},
                #{transcriptSourceFileAssetId}, #{durationSeconds}, #{provider}, #{status},
                #{transcriptText}, #{errorCode}, #{transcribedByType}, #{transcribedById}, #{transcribedAt},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpOrderVoiceTranscript transcript);

    @Select("""
            SELECT id, help_order_id, help_order_event_attachment_id, playback_file_asset_id,
                   transcript_source_file_asset_id, duration_seconds, provider, status,
                   transcript_text, error_code, transcribed_by_type, transcribed_by_id, transcribed_at,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM help_order_voice_transcript
            WHERE tenant_id = #{tenantId}
              AND help_order_event_attachment_id = #{attachmentId}
              AND deleted = 0
            LIMIT 1
            """)
    HelpOrderVoiceTranscript selectByAttachmentId(@Param("tenantId") Long tenantId, @Param("attachmentId") Long attachmentId);

    @Update("""
            UPDATE help_order_voice_transcript
            SET status = #{status},
                transcript_text = #{transcriptText},
                error_code = #{errorCode},
                transcribed_by_type = #{transcribedByType},
                transcribed_by_id = #{transcribedById},
                transcribed_at = #{transcribedAt},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
              AND deleted = 0
            """)
    int updateResult(HelpOrderVoiceTranscript transcript);
}
