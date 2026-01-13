package com.phms.utils;

import com.phms.mapper.UserMapper;
import com.phms.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 用于从请求中获取当前登录用户信息
 * 支持JWT无状态认证
 */
@Component
public class UserContext {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取当前登录用户
     * 优先从Subject获取，如果为null则从JWT Token中获取
     * 
     * @return 当前用户，如果未登录返回null
     */
    public User getCurrentUser() {
        // 首先尝试从Subject获取（如果Session可用）
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        
        if (user != null) {
            return user;
        }
        
        // 如果Subject中没有，从JWT Token中获取
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            
            String authorization = request.getHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                return null;
            }
            
            // 提取Token
            String token = null;
            if (authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            } else {
                token = authorization;
            }
            
            if (token == null || token.isEmpty()) {
                return null;
            }
            
            // 从Token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return null;
            }
            
            // 从数据库查询用户
            return userMapper.getByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
}
