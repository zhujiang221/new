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
    
    /**
     * 批量发送通知给指定角色的所有用户
     * @param roleIds 角色ID列表（1=管理员, 2=医生, 3=用户）
     * @param title 通知标题
     * @param content 通知内容
     * @param senderId 发送者ID（管理员ID）
     * @return 发送成功的数量
     */
    int sendBroadcastNotification(List<Integer> roleIds, String title, String content, Long senderId);
}
