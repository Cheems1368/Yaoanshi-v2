package com.sqlapk.medicine.module.system.controller.app.chat;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.security.core.annotations.PreAuthenticated;
import com.sqlapk.medicine.module.system.api.ChatServiceApi;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageReqDTO;
import com.sqlapk.medicine.module.system.api.dto.chat.ChatMessageRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 聊天控制器
 *
 * @author sqlapk
 */
@Tag(name = "用户 App - 聊天")
@RestController
@RequestMapping("/app-api/chat")
@Validated
@Slf4j
public class ChatController {

    @Autowired
    private ChatServiceApi chatServiceApi;

    @PostMapping("/send")
    @Operation(summary = "发送聊天消息")
    @PreAuthenticated
    public CommonResult<Long> sendMessage(@Valid @RequestBody ChatMessageReqDTO reqDTO,
                                          @Parameter(description = "发送者ID") @RequestParam("senderId") Long senderId,
                                          @Parameter(description = "发送者类型") @RequestParam("senderType") String senderType) {
        return success(chatServiceApi.sendMessage(reqDTO, senderId, senderType));
    }

    @GetMapping("/history/{orderId}")
    @Operation(summary = "获取工单聊天历史")
    @PreAuthenticated
    public CommonResult<List<ChatMessageRespDTO>> getChatHistory(
            @Parameter(description = "工单ID", required = true) @PathVariable("orderId") Long orderId) {
        return success(chatServiceApi.getChatHistory(orderId));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量")
    @PreAuthenticated
    public CommonResult<Long> getUnreadMessageCount(
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId) {
        return success(chatServiceApi.getUnreadMessageCount(userId));
    }

    @PostMapping("/mark-read")
    @Operation(summary = "标记消息为已读")
    @PreAuthenticated
    public CommonResult<Integer> markMessagesAsRead(
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId,
            @Parameter(description = "工单ID", required = true) @RequestParam("orderId") Long orderId) {
        return success(chatServiceApi.markMessagesAsRead(userId, orderId));
    }

    @PostMapping("/recall/{messageId}")
    @Operation(summary = "撤回消息")
    @PreAuthenticated
    public CommonResult<Boolean> recallMessage(
            @Parameter(description = "消息ID", required = true) @PathVariable("messageId") Long messageId,
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId,
            @Parameter(description = "撤回原因") @RequestParam(value = "reason", defaultValue = "用户撤回") String reason) {
        return success(chatServiceApi.recallMessage(messageId, userId, reason));
    }

    @PostMapping("/voice-to-text")
    @Operation(summary = "语音转文字")
    @PreAuthenticated
    public CommonResult<String> convertVoiceToText(
            @Parameter(description = "语音文件URL", required = true) @RequestParam("voiceUrl") String voiceUrl) {
        return success(chatServiceApi.convertVoiceToText(voiceUrl));
    }
}
