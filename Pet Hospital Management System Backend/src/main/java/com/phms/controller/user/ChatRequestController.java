package com.phms.controller.user;

import com.phms.pojo.ChatRequest;
import com.phms.pojo.User;
import com.phms.service.ChatRequestService;
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
 * 聊天申请控制器
 */
@Controller("ChatRequestController")
@RequestMapping("/api/chat/request")
public class ChatRequestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChatRequestService chatRequestService;

    @Autowired
    private UserContext userContext;

    /**
     * 用户发起聊天申请
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public String createRequest(@RequestParam Long doctorId, 
                                @RequestParam(required = false) String requestMessage) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 3) {
                return "NOT_USER";
            }
            logger.info("收到聊天申请请求 - 用户ID: {}, 医生ID: {}, 留言: {}", 
                user.getId(), doctorId, requestMessage);
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setUserId(user.getId());
            chatRequest.setDoctorId(doctorId);
            chatRequest.setRequestMessage(requestMessage);
            chatRequestService.createRequest(chatRequest);
            logger.info("聊天申请创建成功 - 申请ID: {}", chatRequest.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("创建聊天申请失败", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 医生同意申请
     */
    @RequestMapping(value = "/{id}/approve", method = RequestMethod.PUT)
    @ResponseBody
    public String approveRequest(@PathVariable Long id) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 2) {
                return "NOT_DOCTOR";
            }
            chatRequestService.approveRequest(id, user.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("同意聊天申请失败", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 医生拒绝申请
     */
    @RequestMapping(value = "/{id}/reject", method = RequestMethod.PUT)
    @ResponseBody
    public String rejectRequest(@PathVariable Long id) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 2) {
                return "NOT_DOCTOR";
            }
            chatRequestService.rejectRequest(id, user.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("拒绝聊天申请失败", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 查询申请列表（用户或医生）
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getRequestList(@RequestParam(required = false) Integer status) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            List<ChatRequest> list;
            if (user.getRole() == 3) {
                // 用户查询自己的申请
                list = chatRequestService.getByUserId(user.getId(), status);
                logger.info("用户查询申请列表 - 用户ID: {}, 状态: {}, 结果数量: {}", 
                    user.getId(), status, list != null ? list.size() : 0);
            } else if (user.getRole() == 2) {
                // 医生查询发给自己的申请
                list = chatRequestService.getByDoctorId(user.getId(), status);
                logger.info("医生查询申请列表 - 医生ID: {}, 状态: {}, 结果数量: {}", 
                    user.getId(), status, list != null ? list.size() : 0);
            } else {
                return "NOT_PERMISSION";
            }
            return list;
        } catch (Exception e) {
            logger.error("查询申请列表失败", e);
            return "ERROR";
        }
    }

    /**
     * 查询所有申请（管理员）
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Object getAllRequests(ChatRequest chatRequest) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return "NOT_LOGIN";
            }
            if (user.getRole() != 1) {
                return "NOT_ADMIN";
            }
            // 设置默认分页参数
            if (chatRequest.getPage() == null || chatRequest.getPage() < 1) {
                chatRequest.setPage(1);
            }
            if (chatRequest.getLimit() == null || chatRequest.getLimit() < 1) {
                chatRequest.setLimit(10);
            }
            return chatRequestService.getAllRequests(chatRequest);
        } catch (Exception e) {
            logger.error("查询所有申请失败", e);
            return "ERROR";
        }
    }
}
