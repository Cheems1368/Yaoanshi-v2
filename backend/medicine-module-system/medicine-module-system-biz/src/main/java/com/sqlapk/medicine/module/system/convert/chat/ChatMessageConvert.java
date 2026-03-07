package com.sqlapk.medicine.module.system.convert.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageRespDTO;
import com.sqlapk.medicine.module.system.dal.dataobject.chat.ChatMessageDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天消息转换器
 *
 * @author sqlapk
 */
@Mapper
public interface ChatMessageConvert {

    ChatMessageConvert INSTANCE = Mappers.getMapper(ChatMessageConvert.class);

    /**
     * 请求DTO转DO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "senderType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sendTime", ignore = true)
    @Mapping(target = "readTime", ignore = true)
    @Mapping(target = "isRecalled", ignore = true)
    @Mapping(target = "recallTime", ignore = true)
    @Mapping(target = "recallReason", ignore = true)
    @Mapping(target = "voiceToText", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    ChatMessageDO convert(ChatMessageReqDTO reqDTO);

    /**
     * DO转响应DTO
     */
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "isRecalled", expression = "java(recordDO.getIsRecalled() != null && recordDO.getIsRecalled() == 1)")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "stringToList")
    ChatMessageRespDTO convert(ChatMessageDO recordDO);

    /**
     * String转List<String>映射方法
     */
    @org.mapstruct.Named("stringToList")
    default List<String> stringToList(String attachmentsJson) {
        if (attachmentsJson == null || attachmentsJson.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(attachmentsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
