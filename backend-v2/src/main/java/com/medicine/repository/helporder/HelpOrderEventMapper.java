package com.medicine.repository.helporder;

import com.medicine.domain.helporder.HelpOrderEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HelpOrderEventMapper {

    @Insert("""
            INSERT INTO help_order_event (
                help_order_id, event_type, from_status, to_status, operator_type, operator_id, content, attachment_file_asset_id, metadata_json,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{helpOrderId}, #{eventType}, #{fromStatus}, #{toStatus}, #{operatorType}, #{operatorId}, #{content}, #{attachmentFileAssetId}, #{metadataJson},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpOrderEvent event);

    @Select("""
            SELECT id, help_order_id, event_type, from_status, to_status, operator_type, operator_id, content, attachment_file_asset_id, metadata_json,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM help_order_event
            WHERE tenant_id = #{tenantId} AND help_order_id = #{helpOrderId} AND deleted = 0
            ORDER BY create_time ASC, id ASC
            """)
    List<HelpOrderEvent> selectByHelpOrderId(@Param("tenantId") Long tenantId, @Param("helpOrderId") Long helpOrderId);
}
