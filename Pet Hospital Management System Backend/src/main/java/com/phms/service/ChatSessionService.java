package com.phms.service;

import com.phms.pojo.ChatSession;

import java.util.List;

public interface ChatSessionService {
    /**
     * 创建聊天会话
     */
    ChatSession createSession(ChatSession chatSession);

    /**
     * 根据ID查询会话
     */
    ChatSession getById(Long id);

    /**
     * 根据用户ID查询会话列表
     */
    List<ChatSession> getByUserId(Long userId);

    /**
     * 根据医生ID查询会话列表
     */
    List<ChatSession> getByDoctorId(Long doctorId);

    /**
     * 根据用户ID和医生ID查询会话
     */
    ChatSession getByUserAndDoctor(Long userId, Long doctorId);

    /**
     * 关闭会话
     */
    void closeSession(Long id, Long userId);

    /**
     * 更新最后消息时间
     */
    void updateLastMessageTime(Long sessionId);

    /**
     * 查询所有会话（管理员）
     */
    Object getAllSessions(ChatSession chatSession);
}
