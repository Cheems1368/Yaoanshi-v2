package com.sqlapk.medicine.module.system.dal.mysql.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.system.dal.dataobject.chat.ChatMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDO> {

    /**
     * 获取工单的聊天历史
     */
    @Select("SELECT * FROM chat_messages WHERE order_id = #{orderId} " +
            "AND deleted = 0 ORDER BY send_time ASC")
    List<ChatMessageDO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 获取用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE receiver_id = #{receiverId} " +
            "AND status = 'sent' AND deleted = 0")
    Long countUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * 获取用户与工单相关的未读消息
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE receiver_id = #{receiverId} " +
            "AND order_id = #{orderId} AND status = 'sent' AND deleted = 0")
    Long countUnreadMessagesByOrder(@Param("receiverId") Long receiverId, @Param("orderId") Long orderId);

    /**
     * 标记消息为已读
     */
    @Select("UPDATE chat_messages SET status = 'read', read_time = NOW() " +
            "WHERE receiver_id = #{receiverId} AND order_id = #{orderId} " +
            "AND status = 'sent' AND deleted = 0")
    int markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("orderId") Long orderId);

    /**
     * 获取最近聊天记录
     */
    @Select("SELECT * FROM chat_messages WHERE (sender_id = #{userId} OR receiver_id = #{userId}) " +
            "AND deleted = 0 ORDER BY send_time DESC LIMIT #{limit}")
    List<ChatMessageDO> selectRecentMessages(@Param("userId") Long userId, @Param("limit") Integer limit);
}
