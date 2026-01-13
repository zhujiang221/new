package com.phms.filter;

import com.alibaba.fastjson.JSON;
import com.phms.model.ResultMap;
import com.phms.shiro.JwtToken;
import com.phms.utils.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT过滤器
 * 拦截非登录请求，从请求头Authorization提取Token，验证Token有效性
 */
@Component
public class JwtFilter extends BasicHttpAuthenticationFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ResultMap resultMap;
    
    private DefaultWebSecurityManager securityManager;
    
    /**
     * 通过setter注入SecurityManager，避免循环依赖
     */
    @Autowired
    public void setSecurityManager(DefaultWebSecurityManager securityManager) {
        this.securityManager = securityManager;
        // 输出注入的SecurityManager类型用于调试
        if (securityManager != null) {
            logger.info("SecurityManager注入成功，类型: {}, 类名: {}", 
                    securityManager.getClass().getName(), 
                    securityManager.getClass().getSimpleName());
        } else {
            logger.error("SecurityManager注入失败，为null");
        }
    }
    
    /**
     * 判断用户是否想要登录
     * 检测header里面是否包含Authorization字段即可
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("Authorization");
        return authorization != null;
    }
    
    /**
     * 重写getSubject方法，确保SecurityManager正确绑定
     */
    @Override
    protected Subject getSubject(ServletRequest request, ServletResponse response) {
        try {
            // 首先尝试使用父类方法
            return super.getSubject(request, response);
        } catch (Exception e) {
            // 如果父类方法失败，手动绑定SecurityManager并创建Subject
            logger.debug("使用父类getSubject失败，尝试手动绑定SecurityManager: {}", e.getMessage());
            
            // 检查SecurityManager是否已注入
            if (securityManager == null) {
                logger.error("SecurityManager未注入，无法创建Subject");
                throw new RuntimeException("SecurityManager未初始化");
            }
            
            // 输出SecurityManager的实际类型用于调试
            logger.debug("SecurityManager实际类型: {}, 类名: {}", 
                    securityManager.getClass().getName(), 
                    securityManager.getClass().getSimpleName());
            
            try {
                // 将SecurityManager绑定到当前线程的ThreadContext
                ThreadContext.bind(securityManager);
                
                try {
                    // 使用SecurityUtils获取Subject
                    return SecurityUtils.getSubject();
                } catch (Exception ex) {
                    logger.error("使用SecurityUtils.getSubject失败: {}", ex.getMessage(), ex);
                    throw new RuntimeException("无法创建Subject: " + ex.getMessage(), ex);
                }
            } catch (Exception ex) {
                logger.error("手动创建Subject失败: {}, SecurityManager类型: {}", 
                        ex.getMessage(), 
                        securityManager != null ? securityManager.getClass().getName() : "null", ex);
                throw new RuntimeException("无法创建Subject: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * 执行登录
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        String requestURI = httpServletRequest.getRequestURI();
        
        logger.debug("JWT认证开始，URI: {}, Authorization: {}", requestURI, 
                authorization != null ? (authorization.length() > 20 ? authorization.substring(0, 20) + "..." : authorization) : "null");
        
        // 提取Token（Bearer token格式）
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (authorization != null) {
            token = authorization;
        }
        
        if (token == null || token.isEmpty()) {
            logger.warn("Token为空，URI: {}", requestURI);
            throw new AuthenticationException("Token不能为空");
        }
        
        logger.debug("提取Token成功，Token前20字符: {}", token.length() > 20 ? token.substring(0, 20) + "..." : token);
        
        // 验证Token并获取详细错误信息
        JwtUtil.TokenValidationResult validationResult = jwtUtil.validateTokenWithReason(token);
        if (!validationResult.isValid()) {
            String errorMessage = validationResult.getReason();
            logger.warn("Token验证失败，URI: {}, Token前20字符: {}, 原因: {}", requestURI, 
                    token.length() > 20 ? token.substring(0, 20) + "..." : token, errorMessage);
            throw new AuthenticationException(errorMessage != null ? errorMessage : "Token无效或已过期");
        }
        
        logger.debug("Token验证成功，开始执行登录");
        
        // 创建JwtToken并执行登录
        JwtToken jwtToken = new JwtToken(token);
        
        // 使用重写的getSubject方法，确保SecurityManager正确绑定
        try {
            Subject subject = getSubject(request, response);
            subject.login(jwtToken);
            logger.info("JWT认证成功，URI: {}, 用户名: {}", requestURI, jwtUtil.getUsernameFromToken(token));
            return true;
        } catch (Exception e) {
            logger.error("JWT登录失败，URI: {}, 错误: {}", requestURI, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();
        
        // 检查是否是匿名路径（不需要认证的路径）
        if (isAnonPath(requestURI)) {
            return true;
        }
        
        // 如果有Token，尝试认证
        if (isLoginAttempt(request, response)) {
            try {
                boolean loginResult = executeLogin(request, response);
                if (loginResult) {
                    logger.debug("JWT认证通过，允许访问: {}", requestURI);
                    return true;
                } else {
                    logger.warn("JWT认证返回false，URI: {}", requestURI);
                    response401(request, response, "认证失败");
                    return false;
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                // 如果错误信息包含"新设备登录"，使用更友好的提示
                if (errorMessage != null && errorMessage.contains("新设备登录")) {
                    errorMessage = "该设备登录已被新设备登录覆盖，请重新登录";
                }
                logger.error("JWT认证异常: {}, URI: {}, 异常类型: {}", errorMessage, requestURI, e.getClass().getSimpleName(), e);
                response401(request, response, errorMessage);
                return false;
            }
        }
        
        // 没有Token且不是匿名路径，拒绝访问
        response401(request, response, "请先登录");
        return false;
    }
    
    /**
     * 判断是否是匿名路径（不需要认证的路径）
     */
    private boolean isAnonPath(String requestURI) {
        // WebSocket连接路径（WebSocket握手是HTTP请求，需要排除）
        if (requestURI.startsWith("/ws/")) {
            return true;
        }
        
        // 静态资源
        if (requestURI.startsWith("/css/") || requestURI.startsWith("/imgs/") || 
            requestURI.startsWith("/js/") || requestURI.startsWith("/plug/") || 
            requestURI.startsWith("/samples/") || requestURI.startsWith("/upload/") ||
            requestURI.startsWith("/file/")) {
            return true;
        }
        
        // 公开接口
        if (requestURI.equals("/login") || requestURI.equals("/captcha") ||
            requestURI.equals("/sendEmailCode") || requestURI.equals("/doRegist") ||
            requestURI.equals("/resetPassword") || requestURI.equals("/sendResetPasswordCode") ||
            requestURI.equals("/logout") || requestURI.equals("/regist") ||
            requestURI.startsWith("/open/") || requestURI.equals("/index") || 
            requestURI.equals("/home")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 访问拒绝时是否已经处理了
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        response401(request, response, "Token无效或已过期");
        return false;
    }
    
    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
    
    /**
     * 返回401错误
     */
    private void response401(ServletRequest req, ServletResponse resp, String message) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        
        try (PrintWriter out = httpServletResponse.getWriter()) {
            ResultMap result = resultMap.fail().message(message != null ? message : "Token无效或已过期");
            String resultJson = JSON.toJSONString(result);
            out.append(resultJson);
        } catch (Exception e) {
            logger.error("返回401错误响应失败", e);
        }
    }
}
