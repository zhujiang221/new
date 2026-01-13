package com.phms.controller.user;

import com.phms.mapper.UserMapper;
import com.phms.pojo.AppointmentType;
import com.phms.pojo.User;
import com.phms.service.AppointmentTypeService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 预约类型相关接口
 */
@Controller("AppointmentTypeController")
@RequestMapping("/appointmentType")
public class AppointmentTypeController {

    @Autowired
    private AppointmentTypeService appointmentTypeService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserContext userContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 获取当前登录用户，如果从Subject获取的用户role为null，则从数据库重新获取
     */
    private User getCurrentUser() {
        return userContext.getCurrentUser();
    }
    
    /**
     * 检查当前用户是否是管理员
     */
    private boolean isAdmin() {
        User user = getCurrentUser();
        if (user == null) {
            logger.warn("用户为null，无法判断是否为管理员");
            return false;
        }
        
        Integer role = user.getRole();
        if (role == null) {
            logger.warn("用户角色为null，用户ID: {}，用户名: {}", user.getId(), user.getUsername());
            return false;
        }
        
        boolean isAdmin = role == 1;
        logger.debug("用户权限检查 - 用户ID: {}，用户名: {}，角色: {}，是否为管理员: {}", 
            user.getId(), user.getUsername(), role, isAdmin);
        return isAdmin;
    }

    /**
     * 获取所有启用的预约类型（用户端使用）
     */
    @RequestMapping("/list")
    @ResponseBody
    public List<AppointmentType> list() {
        return appointmentTypeService.listEnabledTypes();
    }

    /**
     * 获取所有预约类型（管理员使用，包括禁用的）
     */
    @RequestMapping("/listAll")
    @ResponseBody
    public Object listAll() {
        // 只有管理员可以查看所有类型
        if (!isAdmin()) {
            User user = getCurrentUser();
            logger.warn("获取预约类型列表失败：无权限，用户: {}", 
                user != null ? String.format("ID=%d, username=%s, role=%s", 
                    user.getId(), user.getUsername(), user.getRole()) : "null");
            return "NO_PERMISSION";
        }
        
        try {
            List<AppointmentType> types = appointmentTypeService.listAll();
            logger.info("获取预约类型列表成功，共 {} 条", types != null ? types.size() : 0);
            return types != null ? types : new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取预约类型列表失败", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 添加预约类型（管理员）
     */
    @RequestMapping(value = "/add", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String add(AppointmentType type) {
        if (!isAdmin()) {
            User user = getCurrentUser();
            logger.warn("添加预约类型失败：无权限，用户: {}", 
                user != null ? String.format("ID=%d, username=%s, role=%s", 
                    user.getId(), user.getUsername(), user.getRole()) : "null");
            return "NO_PERMISSION";
        }
        
        logger.info("添加预约类型，接收到的数据: name={}, description={}, status={}", 
            type.getName(), type.getDescription(), type.getStatus());
        
        if (type.getName() == null || type.getName().trim().isEmpty()) {
            return "noName";
        }
        
        try {
            type.setCreateTime(new Date());
            if (type.getStatus() == null) {
                type.setStatus(1); // 默认启用
            }
            appointmentTypeService.add(type);
            logger.info("添加预约类型成功: {}", type.getName());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("添加预约类型失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 更新预约类型（管理员）
     */
    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String update(AppointmentType type) {
        if (!isAdmin()) {
            User user = getCurrentUser();
            logger.warn("更新预约类型失败：无权限，用户: {}", 
                user != null ? String.format("ID=%d, username=%s, role=%s", 
                    user.getId(), user.getUsername(), user.getRole()) : "null");
            return "NO_PERMISSION";
        }
        
        try {
            appointmentTypeService.update(type);
            logger.info("更新预约类型成功: id={}", type.getId());
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("更新预约类型失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 删除预约类型（管理员）
     */
    @RequestMapping(value = "/delete", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String delete(Long id) {
        if (!isAdmin()) {
            User user = getCurrentUser();
            logger.warn("删除预约类型失败：无权限，用户: {}", 
                user != null ? String.format("ID=%d, username=%s, role=%s", 
                    user.getId(), user.getUsername(), user.getRole()) : "null");
            return "NO_PERMISSION";
        }
        
        if (id == null) {
            return "noId";
        }
        
        try {
            appointmentTypeService.deleteById(id);
            logger.info("删除预约类型成功: id={}", id);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("删除预约类型失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR: " + e.getMessage();
        }
    }
}

