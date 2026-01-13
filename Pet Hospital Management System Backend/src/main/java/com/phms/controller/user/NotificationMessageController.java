package com.phms.controller.user;

import com.phms.pojo.NotificationMessage;
import com.phms.pojo.User;
import com.phms.service.NotificationMessageService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 消息通知控制器
 */
@Controller("NotificationMessageController")
@RequestMapping("/notification")
public class NotificationMessageController {
    @Autowired
    private NotificationMessageService notificationMessageService;
    
    @Autowired
    private UserContext userContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取消息列表（分页）
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getMessageList(NotificationMessage notificationMessage) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            // 设置接收者ID为当前登录用户
            notificationMessage.setReceiverId(user.getId());
            // 设置默认分页参数
            if (notificationMessage.getPage() == null || notificationMessage.getPage() < 1) {
                notificationMessage.setPage(1);
            }
            if (notificationMessage.getLimit() == null || notificationMessage.getLimit() < 1) {
                notificationMessage.setLimit(10);
            }
            return notificationMessageService.getMessageList(notificationMessage);
        } catch (Exception e) {
            logger.error("获取消息列表异常", e);
            return "ERROR";
        }
    }

    /**
     * 获取未读消息数量
     */
    @RequestMapping("/unreadCount")
    @ResponseBody
    public Object getUnreadCount() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return 0;
            }
            int count = notificationMessageService.getUnreadCount(user.getId());
            return count;
        } catch (Exception e) {
            logger.error("获取未读消息数量异常", e);
            return 0;
        }
    }

    /**
     * 标记消息已读（支持单个或批量）
     */
    @RequestMapping("/markRead")
    @ResponseBody
    public String markAsRead(@RequestParam(required = false) List<Long> ids) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            notificationMessageService.markAsRead(ids, user.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("标记消息已读异常", e);
            return "ERROR";
        }
    }

    /**
     * 登录时检查未读消息（返回汇总信息）
     */
    @RequestMapping("/checkOnLogin")
    @ResponseBody
    public Object checkOnLogin() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            return notificationMessageService.checkOnLogin(user.getId());
        } catch (Exception e) {
            logger.error("登录检查未读消息异常", e);
            return "ERROR";
        }
    }
}
