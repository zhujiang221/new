package com.phms.service.impl;

import com.phms.mapper.ChatSessionMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.ChatSession;
import com.phms.service.ChatSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Override
    @Transactional
    public ChatSession createSession(ChatSession chatSession) {
        try {
            // 检查是否已存在会话
            ChatSession existing = chatSessionMapper.selectByUserAndDoctor(
                chatSession.getUserId(), chatSession.getDoctorId());
            if (existing != null && existing.getStatus() == 1) {
                return existing; // 返回已存在的会话
            }

            // 设置默认值
            if (chatSession.getStatus() == null) {
                chatSession.setStatus(1); // 进行中
            }
            if (chatSession.getCreateTime() == null) {
                chatSession.setCreateTime(new Date());
            }

            // 插入会话
            chatSessionMapper.insert(chatSession);
            logger.info("创建聊天会话成功 - ID: {}, 用户ID: {}, 医生ID: {}", 
                chatSession.getId(), chatSession.getUserId(), chatSession.getDoctorId());
            return chatSession;
        } catch (Exception e) {
            logger.error("创建聊天会话失败 - 用户ID: {}, 医生ID: {}", 
                chatSession.getUserId(), chatSession.getDoctorId(), e);
            throw new RuntimeException("创建聊天会话失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatSession getById(Long id) {
        try {
            return chatSessionMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("查询聊天会话失败 - ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<ChatSession> getByUserId(Long userId) {
        try {
            return chatSessionMapper.selectByUserId(userId);
        } catch (Exception e) {
            logger.error("查询用户会话列表失败 - 用户ID: {}", userId, e);
            return null;
        }
    }

    @Override
    public List<ChatSession> getByDoctorId(Long doctorId) {
        try {
            return chatSessionMapper.selectByDoctorId(doctorId);
        } catch (Exception e) {
            logger.error("查询医生会话列表失败 - 医生ID: {}", doctorId, e);
            return null;
        }
    }

    @Override
    public ChatSession getByUserAndDoctor(Long userId, Long doctorId) {
        try {
            return chatSessionMapper.selectByUserAndDoctor(userId, doctorId);
        } catch (Exception e) {
            logger.error("查询会话失败 - 用户ID: {}, 医生ID: {}", userId, doctorId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public void closeSession(Long id, Long userId) {
        try {
            ChatSession session = chatSessionMapper.selectByPrimaryKey(id);
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            // 只有用户或医生可以关闭会话
            if (!session.getUserId().equals(userId) && !session.getDoctorId().equals(userId)) {
                throw new RuntimeException("无权操作此会话");
            }

            // 更新会话状态为已关闭
            chatSessionMapper.updateStatus(id, 0);
            logger.info("关闭聊天会话成功 - 会话ID: {}", id);
        } catch (Exception e) {
            logger.error("关闭聊天会话失败 - 会话ID: {}, 用户ID: {}", id, userId, e);
            throw new RuntimeException("关闭会话失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLastMessageTime(Long sessionId) {
        try {
            chatSessionMapper.updateLastMessageTime(sessionId);
        } catch (Exception e) {
            logger.error("更新最后消息时间失败 - 会话ID: {}", sessionId, e);
        }
    }

    @Override
    public Object getAllSessions(ChatSession chatSession) {
        try {
            // 计算分页
            Integer begin = (chatSession.getPage() - 1) * chatSession.getLimit();
            chatSession.setPage(begin);

            List<ChatSession> rows = chatSessionMapper.selectAll(chatSession);
            int total = chatSessionMapper.countByCondition(chatSession);

            MMGridPageVoBean<ChatSession> vo = new MMGridPageVoBean<>();
            vo.setTotal(total);
            vo.setRows(rows);
            return vo;
        } catch (Exception e) {
            logger.error("查询所有会话失败", e);
            MMGridPageVoBean<ChatSession> vo = new MMGridPageVoBean<>();
            vo.setTotal(0);
            return vo;
        }
    }
}
