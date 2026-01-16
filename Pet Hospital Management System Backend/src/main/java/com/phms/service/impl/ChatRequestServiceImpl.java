package com.phms.service.impl;

import com.alibaba.fastjson.JSON;
import com.phms.mapper.ChatRequestMapper;
import com.phms.mapper.ChatSessionMapper;
import com.phms.mapper.UserMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.ChatRequest;
import com.phms.pojo.ChatSession;
import com.phms.pojo.NotificationMessage;
import com.phms.pojo.User;
import com.phms.service.ChatRequestService;
import com.phms.service.ChatSessionService;
import com.phms.service.NotificationMessageService;
import com.phms.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatRequestServiceImpl implements ChatRequestService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ChatRequestMapper chatRequestMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionService chatSessionService;
    
    @Autowired
    private NotificationMessageService notificationMessageService;
    
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void createRequest(ChatRequest chatRequest) {
        try {
            // 检查是否已有待审核或已同意的申请
            ChatRequest existing = chatRequestMapper.selectExistingRequest(
                chatRequest.getUserId(), chatRequest.getDoctorId());
            if (existing != null) {
                if (existing.getStatus() == 0) {
                    throw new RuntimeException("您已向该医生发起过申请，请等待审核");
                } else if (existing.getStatus() == 1) {
                    throw new RuntimeException("您已与该医生建立聊天会话");
                }
            }

            // 设置默认值
            chatRequest.setStatus(0); // 待审核
            chatRequest.setCreateTime(new Date());
            chatRequest.setUpdateTime(new Date());

            // 插入申请
            chatRequestMapper.insert(chatRequest);
            logger.info("创建聊天申请成功 - ID: {}, 用户ID: {}, 医生ID: {}", 
                chatRequest.getId(), chatRequest.getUserId(), chatRequest.getDoctorId());
        } catch (Exception e) {
            logger.error("创建聊天申请失败 - 用户ID: {}, 医生ID: {}", 
                chatRequest.getUserId(), chatRequest.getDoctorId(), e);
            throw new RuntimeException("创建聊天申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatRequest getById(Long id) {
        try {
            return chatRequestMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("查询聊天申请失败 - ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<ChatRequest> getByUserId(Long userId, Integer status) {
        try {
            List<ChatRequest> list = chatRequestMapper.selectByUserId(userId, status);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            logger.error("查询用户申请列表失败 - 用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChatRequest> getByDoctorId(Long doctorId, Integer status) {
        try {
            List<ChatRequest> list = chatRequestMapper.selectByDoctorId(doctorId, status);
            logger.info("查询医生申请列表 - 医生ID: {}, 状态: {}, 查询结果数量: {}", 
                doctorId, status, list != null ? list.size() : 0);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            logger.error("查询医生申请列表失败 - 医生ID: {}, 状态: {}", doctorId, status, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void approveRequest(Long id, Long doctorId) {
        try {
            ChatRequest request = chatRequestMapper.selectByPrimaryKey(id);
            if (request == null) {
                throw new RuntimeException("申请不存在");
            }
            if (!request.getDoctorId().equals(doctorId)) {
                throw new RuntimeException("无权操作此申请");
            }
            if (request.getStatus() != 0) {
                throw new RuntimeException("申请状态不正确");
            }

            // 更新申请状态为已同意
            chatRequestMapper.updateStatus(id, 1);

            // 创建聊天会话
            ChatSession session = new ChatSession();
            session.setUserId(request.getUserId());
            session.setDoctorId(request.getDoctorId());
            session.setRequestId(id);
            session.setStatus(1); // 进行中
            session.setCreateTime(new Date());
            chatSessionMapper.insert(session);

            logger.info("同意聊天申请成功 - 申请ID: {}, 会话ID: {}", id, session.getId());
            
            // 发送通知给用户
            try {
                // 获取医生信息
                User doctor = userMapper.selectByPrimaryKey(doctorId);
                String doctorName = doctor != null && doctor.getName() != null ? doctor.getName() : "医生";
                
                // 构建通知内容
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("requestId", id);
                contentMap.put("sessionId", session.getId());
                contentMap.put("doctorName", doctorName);
                contentMap.put("message", "医生已同意您的好友申请，现在可以开始聊天了");
                
                String contentJson = JSON.toJSONString(contentMap);
                
                // 创建通知消息
                NotificationMessage notification = new NotificationMessage();
                notification.setReceiverId(request.getUserId()); // 接收者是用户
                notification.setSenderId(doctorId); // 发送者是医生
                notification.setType("CHAT_REQUEST_APPROVED");
                notification.setTitle("好友申请已同意");
                notification.setContent(contentJson);
                notification.setIsRead(0); // 未读
                notification.setCreateTime(new Date());
                
                // 保存通知消息
                notificationMessageService.createMessage(notification);
                logger.info("好友申请同意通知已创建 - 申请ID: {}, 用户ID: {}", id, request.getUserId());
                
                // 通过WebSocket推送通知给用户
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "notification");
                wsMessage.put("data", notification);
                String wsMessageJson = JSON.toJSONString(wsMessage);
                notificationWebSocketHandler.sendMessageToUser(request.getUserId(), wsMessageJson);
                logger.info("好友申请同意通知已通过WebSocket推送 - 用户ID: {}", request.getUserId());
                
            } catch (Exception e) {
                // 通知发送失败不影响申请同意流程
                logger.error("发送好友申请同意通知失败 - 申请ID: {}, 用户ID: {}", id, request.getUserId(), e);
            }
        } catch (Exception e) {
            logger.error("同意聊天申请失败 - 申请ID: {}, 医生ID: {}", id, doctorId, e);
            throw new RuntimeException("同意申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void rejectRequest(Long id, Long doctorId) {
        try {
            ChatRequest request = chatRequestMapper.selectByPrimaryKey(id);
            if (request == null) {
                throw new RuntimeException("申请不存在");
            }
            if (!request.getDoctorId().equals(doctorId)) {
                throw new RuntimeException("无权操作此申请");
            }
            if (request.getStatus() != 0) {
                throw new RuntimeException("申请状态不正确");
            }

            // 更新申请状态为已拒绝
            chatRequestMapper.updateStatus(id, 2);
            logger.info("拒绝聊天申请成功 - 申请ID: {}", id);
        } catch (Exception e) {
            logger.error("拒绝聊天申请失败 - 申请ID: {}, 医生ID: {}", id, doctorId, e);
            throw new RuntimeException("拒绝申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object getAllRequests(ChatRequest chatRequest) {
        try {
            // 计算分页
            Integer begin = (chatRequest.getPage() - 1) * chatRequest.getLimit();
            chatRequest.setPage(begin);

            List<ChatRequest> rows = chatRequestMapper.selectAll(chatRequest);
            int total = chatRequestMapper.countByCondition(chatRequest);

            MMGridPageVoBean<ChatRequest> vo = new MMGridPageVoBean<>();
            vo.setTotal(total);
            vo.setRows(rows);
            return vo;
        } catch (Exception e) {
            logger.error("查询所有申请失败", e);
            MMGridPageVoBean<ChatRequest> vo = new MMGridPageVoBean<>();
            vo.setTotal(0);
            return vo;
        }
    }
}
