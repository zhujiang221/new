package com.phms.service;

import com.phms.pojo.NotificationMessage;

import java.util.List;

public interface NotificationMessageService {
    /**
     * 创建消息记录
     */
    void createMessage(NotificationMessage message);

    /**
     * 获取消息列表（分页）
     */
    Object getMessageList(NotificationMessage notificationMessage);

    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long receiverId);

    /**
     * 标记消息为已读（单个或批量）
     */
    void markAsRead(List<Long> ids, Long receiverId);

    /**
     * 登录时检查未读消息（返回汇总信息）
     */
    Object checkOnLogin(Long receiverId);
}
