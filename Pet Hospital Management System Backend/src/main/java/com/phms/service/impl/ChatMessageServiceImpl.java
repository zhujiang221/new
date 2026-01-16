package com.phms.service.impl;

import com.phms.mapper.ChatMessageMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.ChatMessage;
import com.phms.service.ChatMessageService;
import com.phms.service.ChatSessionService;
import com.phms.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private NotificationWebSocketHandler webSocketHandler;

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        try {
            // 验证必要字段
            if (chatMessage.getSessionId() == null) {
                throw new RuntimeException("会话ID不能为空");
            }
            if (chatMessage.getSenderId() == null) {
                throw new RuntimeException("发送者ID不能为空");
            }
            if (chatMessage.getReceiverId() == null) {
                throw new RuntimeException("接收者ID不能为空");
            }
            if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                throw new RuntimeException("消息内容不能为空");
            }

            // 设置默认值
            if (chatMessage.getMessageType() == null || chatMessage.getMessageType().isEmpty()) {
                chatMessage.setMessageType("text");
            }
            if (chatMessage.getIsRead() == null) {
                chatMessage.setIsRead(0); // 未读
            }
            if (chatMessage.getIsRevoked() == null) {
                chatMessage.setIsRevoked(0); // 未撤回
            }
            if (chatMessage.getCreateTime() == null) {
                chatMessage.setCreateTime(new Date());
            }

            // 插入消息
            chatMessageMapper.insert(chatMessage);

            // 更新会话的最后消息时间
            chatSessionService.updateLastMessageTime(chatMessage.getSessionId());

            // 通过WebSocket实时推送消息给接收者
            try {
                // 查询完整的消息信息（包含发送者信息）
                ChatMessage fullMessage = chatMessageMapper.selectByPrimaryKey(chatMessage.getId());
                if (fullMessage != null) {
                    webSocketHandler.sendChatMessage(chatMessage.getReceiverId(), fullMessage);
                }
            } catch (Exception e) {
                logger.warn("WebSocket推送消息失败，消息已保存到数据库 - 接收者ID: {}", 
                    chatMessage.getReceiverId(), e);
            }

            logger.info("发送消息成功 - 消息ID: {}, 会话ID: {}, 发送者ID: {}", 
                chatMessage.getId(), chatMessage.getSessionId(), chatMessage.getSenderId());
            return chatMessage;
        } catch (Exception e) {
            logger.error("发送消息失败 - 会话ID: {}, 发送者ID: {}", 
                chatMessage.getSessionId(), chatMessage.getSenderId(), e);
            throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatMessage getById(Long id) {
        try {
            return chatMessageMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("查询消息失败 - ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<ChatMessage> getBySessionId(Long sessionId, Integer page, Integer limit) {
        try {
            // 计算分页
            Integer begin = null;
            if (page != null && limit != null) {
                begin = (page - 1) * limit;
            }
            return chatMessageMapper.selectBySessionId(sessionId, begin, limit);
        } catch (Exception e) {
            logger.error("查询消息列表失败 - 会话ID: {}", sessionId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long sessionId, Long receiverId) {
        try {
            chatMessageMapper.markAsRead(sessionId, receiverId);
            logger.info("标记消息已读成功 - 会话ID: {}, 接收者ID: {}", sessionId, receiverId);
        } catch (Exception e) {
            logger.error("标记消息已读失败 - 会话ID: {}, 接收者ID: {}", sessionId, receiverId, e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public int getUnreadCountBySession(Long sessionId, Long receiverId) {
        try {
            return chatMessageMapper.countUnreadBySession(sessionId, receiverId);
        } catch (Exception e) {
            logger.error("获取未读消息数失败 - 会话ID: {}, 接收者ID: {}", sessionId, receiverId, e);
            return 0;
        }
    }

    @Override
    public int getUnreadCountByReceiver(Long receiverId) {
        try {
            return chatMessageMapper.countUnreadByReceiver(receiverId);
        } catch (Exception e) {
            logger.error("获取未读消息总数失败 - 接收者ID: {}", receiverId, e);
            return 0;
        }
    }

    @Override
    public Object getAllMessages(ChatMessage chatMessage) {
        try {
            // 计算分页
            Integer begin = (chatMessage.getPage() - 1) * chatMessage.getLimit();
            chatMessage.setPage(begin);

            List<ChatMessage> rows = chatMessageMapper.selectAll(chatMessage);
            int total = chatMessageMapper.countByCondition(chatMessage);

            MMGridPageVoBean<ChatMessage> vo = new MMGridPageVoBean<>();
            vo.setTotal(total);
            vo.setRows(rows);
            return vo;
        } catch (Exception e) {
            logger.error("查询所有消息失败", e);
            MMGridPageVoBean<ChatMessage> vo = new MMGridPageVoBean<>();
            vo.setTotal(0);
            return vo;
        }
    }

    @Override
    @Transactional
    public String revokeMessage(Long messageId, Long senderId) {
        try {
            // 验证消息是否存在
            ChatMessage message = chatMessageMapper.selectByPrimaryKey(messageId);
            if (message == null) {
                return "消息不存在";
            }

            // 验证是否是发送者本人
            if (!message.getSenderId().equals(senderId)) {
                return "只能撤回自己发送的消息";
            }

            // 验证消息是否已撤回
            if (message.getIsRevoked() != null && message.getIsRevoked() == 1) {
                return "消息已被撤回";
            }

            // 验证是否在3分钟内（180秒）
            long createTime = message.getCreateTime().getTime();
            long now = System.currentTimeMillis();
            long diffSeconds = (now - createTime) / 1000;
            if (diffSeconds > 180) {
                return "消息发送超过3分钟，无法撤回";
            }
            if (diffSeconds < 0) {
                return "消息时间异常";
            }

            // 执行撤回
            int result = chatMessageMapper.revokeMessage(messageId, senderId);
            if (result > 0) {
                // 通过WebSocket通知接收者消息已撤回
                try {
                    ChatMessage revokedMessage = chatMessageMapper.selectByPrimaryKey(messageId);
                    if (revokedMessage != null) {
                        webSocketHandler.sendChatMessage(revokedMessage.getReceiverId(), revokedMessage);
                    }
                } catch (Exception e) {
                    logger.warn("WebSocket推送撤回消息失败，消息已撤回 - 消息ID: {}", messageId, e);
                }

                logger.info("撤回消息成功 - 消息ID: {}, 发送者ID: {}", messageId, senderId);
                return "SUCCESS";
            } else {
                return "撤回失败，可能是消息已过期或已被撤回";
            }
        } catch (Exception e) {
            logger.error("撤回消息失败 - 消息ID: {}, 发送者ID: {}", messageId, senderId, e);
            return e.getMessage();
        }
    }
}
