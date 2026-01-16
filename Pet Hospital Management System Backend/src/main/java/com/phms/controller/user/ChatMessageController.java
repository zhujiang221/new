package com.phms.controller.user;

import com.phms.pojo.ChatMessage;
import com.phms.pojo.ChatSession;
import com.phms.pojo.User;
import com.phms.service.ChatMessageService;
import com.phms.service.ChatSessionService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 聊天消息控制器
 */
@Controller("ChatMessageController")
@RequestMapping("/api/chat/message")
public class ChatMessageController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserContext userContext;

    /**
     * 发送消息
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public Object sendMessage(ChatMessage chatMessage) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }

            // 验证会话是否存在且用户有权限
            ChatSession session = chatSessionService.getById(chatMessage.getSessionId());
            if (session == null) {
                return "SESSION_NOT_FOUND";
            }
            if (!session.getUserId().equals(user.getId()) && !session.getDoctorId().equals(user.getId())) {
                return "NOT_PERMISSION";
            }
            if (session.getStatus() != 1) {
                return "SESSION_CLOSED";
            }

            // 设置发送者和接收者
            chatMessage.setSenderId(user.getId());
            if (session.getUserId().equals(user.getId())) {
                chatMessage.setReceiverId(session.getDoctorId());
            } else {
                chatMessage.setReceiverId(session.getUserId());
            }

            ChatMessage result = chatMessageService.sendMessage(chatMessage);
            return result;
        } catch (Exception e) {
            logger.error("发送消息失败", e);
            return e.getMessage();
        }
    }

    /**
     * 查询消息列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getMessageList(@RequestParam Long sessionId,
                                  @RequestParam(required = false, defaultValue = "1") Integer page,
                                  @RequestParam(required = false, defaultValue = "50") Integer limit) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }

            // 验证会话权限
            ChatSession session = chatSessionService.getById(sessionId);
            if (session == null) {
                return "SESSION_NOT_FOUND";
            }
            if (!session.getUserId().equals(user.getId()) && !session.getDoctorId().equals(user.getId())) {
                return "NOT_PERMISSION";
            }

            List<ChatMessage> messages = chatMessageService.getBySessionId(sessionId, page, limit);
            return messages;
        } catch (Exception e) {
            logger.error("查询消息列表失败", e);
            return "ERROR";
        }
    }

    /**
     * 标记消息为已读
     */
    @RequestMapping(value = "/read", method = RequestMethod.PUT)
    @ResponseBody
    public String markAsRead(@RequestParam Long sessionId) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }

            // 验证会话权限
            ChatSession session = chatSessionService.getById(sessionId);
            if (session == null) {
                return "SESSION_NOT_FOUND";
            }
            if (!session.getUserId().equals(user.getId()) && !session.getDoctorId().equals(user.getId())) {
                return "NOT_PERMISSION";
            }

            chatMessageService.markAsRead(sessionId, user.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("标记消息已读失败", e);
            return e.getMessage();
        }
    }

    /**
     * 获取未读消息数
     */
    @RequestMapping(value = "/unread", method = RequestMethod.GET)
    @ResponseBody
    public Object getUnreadCount(@RequestParam(required = false) Long sessionId) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return 0;
            }

            if (sessionId != null) {
                // 获取指定会话的未读消息数
                return chatMessageService.getUnreadCountBySession(sessionId, user.getId());
            } else {
                // 获取所有未读消息总数
                return chatMessageService.getUnreadCountByReceiver(user.getId());
            }
        } catch (Exception e) {
            logger.error("获取未读消息数失败", e);
            return 0;
        }
    }

    /**
     * 查询所有消息（管理员）
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Object getAllMessages(ChatMessage chatMessage) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 1) {
                return "NOT_ADMIN";
            }
            // 设置默认分页参数
            if (chatMessage.getPage() == null || chatMessage.getPage() < 1) {
                chatMessage.setPage(1);
            }
            if (chatMessage.getLimit() == null || chatMessage.getLimit() < 1) {
                chatMessage.setLimit(10);
            }
            return chatMessageService.getAllMessages(chatMessage);
        } catch (Exception e) {
            logger.error("查询所有消息失败", e);
            return "ERROR";
        }
    }

    /**
     * 撤回消息
     */
    @RequestMapping(value = "/{id}/revoke", method = RequestMethod.PUT)
    @ResponseBody
    public String revokeMessage(@PathVariable("id") Long id) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }

            String result = chatMessageService.revokeMessage(id, user.getId());
            return result;
        } catch (Exception e) {
            logger.error("撤回消息失败", e);
            return e.getMessage();
        }
    }
}
