package com.phms.mapper;

import com.phms.pojo.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {
    /**
     * 插入聊天消息
     */
    int insert(ChatMessage record);

    /**
     * 根据ID查询消息
     */
    ChatMessage selectByPrimaryKey(Long id);

    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId, @Param("page") Integer page, @Param("limit") Integer limit);

    /**
     * 标记消息为已读
     */
    int markAsRead(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);

    /**
     * 统计会话的未读消息数
     */
    int countUnreadBySession(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);

    /**
     * 统计用户的未读消息总数
     */
    int countUnreadByReceiver(@Param("receiverId") Long receiverId);

    /**
     * 查询所有消息（管理员）
     */
    List<ChatMessage> selectAll(ChatMessage chatMessage);

    /**
     * 统计消息数量
     */
    int countByCondition(ChatMessage chatMessage);

    /**
     * 撤回消息
     */
    int revokeMessage(@Param("id") Long id, @Param("senderId") Long senderId);
}
