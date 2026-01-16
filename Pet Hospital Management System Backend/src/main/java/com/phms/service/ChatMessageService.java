package com.phms.service;

import com.phms.pojo.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    /**
     * 发送消息
     */
    ChatMessage sendMessage(ChatMessage chatMessage);

    /**
     * 根据ID查询消息
     */
    ChatMessage getById(Long id);

    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessage> getBySessionId(Long sessionId, Integer page, Integer limit);

    /**
     * 标记会话消息为已读
     */
    void markAsRead(Long sessionId, Long receiverId);

    /**
     * 获取会话的未读消息数
     */
    int getUnreadCountBySession(Long sessionId, Long receiverId);

    /**
     * 获取用户的未读消息总数
     */
    int getUnreadCountByReceiver(Long receiverId);

    /**
     * 查询所有消息（管理员）
     */
    Object getAllMessages(ChatMessage chatMessage);

    /**
     * 撤回消息
     */
    String revokeMessage(Long messageId, Long senderId);
}
