package com.phms.controller.user;

import com.phms.pojo.ChatSession;
import com.phms.pojo.User;
import com.phms.service.ChatSessionService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 聊天会话控制器
 */
@Controller("ChatSessionController")
@RequestMapping("/api/chat/session")
public class ChatSessionController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserContext userContext;

    /**
     * 查询会话列表（用户或医生）
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getSessionList() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            List<ChatSession> list;
            if (user.getRole() == 3) {
                // 用户查询自己的会话
                list = chatSessionService.getByUserId(user.getId());
            } else if (user.getRole() == 2) {
                // 医生查询自己的会话
                list = chatSessionService.getByDoctorId(user.getId());
            } else {
                return "NOT_PERMISSION";
            }
            return list;
        } catch (Exception e) {
            logger.error("查询会话列表失败", e);
            return "ERROR";
        }
    }

    /**
     * 获取会话详情
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Object getSession(@PathVariable Long id) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            ChatSession session = chatSessionService.getById(id);
            if (session == null) {
                return "NOT_FOUND";
            }
            // 验证权限：只有会话的参与者可以查看
            if (!session.getUserId().equals(user.getId()) && !session.getDoctorId().equals(user.getId())) {
                return "NOT_PERMISSION";
            }
            return session;
        } catch (Exception e) {
            logger.error("查询会话详情失败", e);
            return "ERROR";
        }
    }

    /**
     * 关闭会话
     */
    @RequestMapping(value = "/{id}/close", method = RequestMethod.PUT)
    @ResponseBody
    public String closeSession(@PathVariable Long id) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            chatSessionService.closeSession(id, user.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("关闭会话失败", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 查询所有会话（管理员）
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Object getAllSessions(ChatSession chatSession) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 1) {
                return "NOT_ADMIN";
            }
            // 设置默认分页参数
            if (chatSession.getPage() == null || chatSession.getPage() < 1) {
                chatSession.setPage(1);
            }
            if (chatSession.getLimit() == null || chatSession.getLimit() < 1) {
                chatSession.setLimit(10);
            }
            return chatSessionService.getAllSessions(chatSession);
        } catch (Exception e) {
            logger.error("查询所有会话失败", e);
            return "ERROR";
        }
    }
}
