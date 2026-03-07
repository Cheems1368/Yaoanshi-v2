package com.sqlapk.medicine.module.system.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 聊天消息 DO
 *
 * @author sqlapk
 */
@TableName("chat_messages")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDO extends BaseDO {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 工单ID
     */
    private Long orderId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者类型（patient/pharmacist/doctor/system）
     */
    private String senderType;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者类型（patient/pharmacist/doctor/system）
     */
    private String receiverType;
    
    /**
     * 消息类型（text/voice/image/file/system）
     */
    private String messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 语音文件URL（语音消息时使用）
     */
    private String voiceUrl;
    
    /**
     * 语音时长（秒）
     */
    private Integer voiceDuration;
    
    /**
     * 语音转文字结果
     */
    private String voiceToText;
    
    /**
     * 附件URL列表（JSON格式）
     */
    private String attachments;
    
    /**
     * 消息状态（sent/delivered/read/failed）
     */
    private String status;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 是否撤回（0-未撤回，1-已撤回）
     */
    private Integer isRecalled;
    
    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;
    
    /**
     * 撤回原因
     */
    private String recallReason;
}
