package com.phms.service.impl;

import com.phms.mapper.NotificationMessageMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.NotificationMessage;
import com.phms.service.NotificationMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationMessageServiceImpl implements NotificationMessageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private NotificationMessageMapper notificationMessageMapper;

    @Override
    public void createMessage(NotificationMessage message) {
        try {
            // 验证必要字段
            if (message.getReceiverId() == null) {
                logger.error("创建消息失败：接收者ID为空");
                throw new RuntimeException("接收者ID不能为空");
            }
            if (message.getSenderId() == null) {
                logger.error("创建消息失败：发送者ID为空");
                throw new RuntimeException("发送者ID不能为空");
            }
            
            // 设置默认值
            if (message.getCreateTime() == null) {
                message.setCreateTime(new java.util.Date());
            }
            if (message.getIsRead() == null) {
                message.setIsRead(0);
            }
            // type字段现在存储预约类型ID，如果没有设置则使用默认值"1"（看病）
            if (message.getType() == null || message.getType().isEmpty()) {
                message.setType("1");
            }
            
            logger.info("准备插入消息到数据库 - 接收者ID: {}, 发送者ID: {}, 预约ID: {}, 标题: {}", 
                message.getReceiverId(), message.getSenderId(), message.getAppointmentId(), message.getTitle());
            
            // 插入消息
            notificationMessageMapper.insert(message);
            
            // 验证ID是否已生成
            if (message.getId() == null) {
                logger.error("消息插入失败：未获取到生成的ID - 接收者ID: {}, 发送者ID: {}", 
                    message.getReceiverId(), message.getSenderId());
                throw new RuntimeException("消息插入失败：未获取到生成的ID");
            }
            
            logger.info("消息已成功插入数据库 - 消息ID: {}, 接收者ID: {}, 发送者ID: {}", 
                message.getId(), message.getReceiverId(), message.getSenderId());
        } catch (Exception e) {
            logger.error("创建消息记录异常 - 接收者ID: {}, 发送者ID: {}, 预约ID: {}", 
                message.getReceiverId(), message.getSenderId(), message.getAppointmentId(), e);
            throw new RuntimeException("创建消息记录失败", e);
        }
    }

    @Override
    public Object getMessageList(NotificationMessage notificationMessage) {
        int size = 0;
        // 计算分页
        Integer begin = (notificationMessage.getPage() - 1) * notificationMessage.getLimit();
        notificationMessage.setPage(begin);

        List<NotificationMessage> rows = new ArrayList<>();
        try {
            rows = notificationMessageMapper.selectByReceiverId(notificationMessage);
            size = notificationMessageMapper.countByReceiverId(notificationMessage.getReceiverId());
        } catch (Exception e) {
            logger.error("查询消息列表异常", e);
        }
        MMGridPageVoBean<NotificationMessage> vo = new MMGridPageVoBean<>();
        vo.setTotal(size);
        vo.setRows(rows);

        return vo;
    }

    @Override
    public int getUnreadCount(Long receiverId) {
        try {
            return notificationMessageMapper.countUnreadByReceiverId(receiverId);
        } catch (Exception e) {
            logger.error("获取未读消息数量异常", e);
            return 0;
        }
    }

    @Override
    public void markAsRead(List<Long> ids, Long receiverId) {
        try {
            if (ids != null && !ids.isEmpty()) {
                // 批量标记指定消息为已读
                notificationMessageMapper.batchMarkAsRead(ids, receiverId);
            } else {
                // 如果没有指定ID，标记该接收者的所有未读消息为已读
                List<Long> allUnreadIds = new ArrayList<>();
                NotificationMessage query = new NotificationMessage();
                query.setReceiverId(receiverId);
                query.setPage(0);
                query.setLimit(1000); // 获取所有未读消息
                List<NotificationMessage> unreadMessages = notificationMessageMapper.selectByReceiverId(query);
                for (NotificationMessage msg : unreadMessages) {
                    if (msg.getIsRead() != null && msg.getIsRead() == 0) {
                        allUnreadIds.add(msg.getId());
                    }
                }
                if (!allUnreadIds.isEmpty()) {
                    notificationMessageMapper.batchMarkAsRead(allUnreadIds, receiverId);
                }
            }
        } catch (Exception e) {
            logger.error("标记消息已读异常", e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public Object checkOnLogin(Long receiverId) {
        try {
            int unreadCount = notificationMessageMapper.countUnreadByReceiverId(receiverId);
            Map<String, Object> result = new HashMap<>();
            result.put("hasUnread", unreadCount > 0);
            result.put("unreadCount", unreadCount);
            if (unreadCount > 0) {
                result.put("message", "您有" + unreadCount + "条新预约消息");
            } else {
                result.put("message", "暂无新消息");
            }
            return result;
        } catch (Exception e) {
            logger.error("登录检查未读消息异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("hasUnread", false);
            result.put("unreadCount", 0);
            result.put("message", "检查消息失败");
            return result;
        }
    }
}
