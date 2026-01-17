package com.phms.interceptor;

import com.alibaba.fastjson.JSON;
import com.phms.pojo.ApiLog;
import com.phms.service.ApiLogService;
import com.phms.utils.JwtUtil;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API日志拦截器
 * 记录所有API请求的详细信息
 */
@Component
public class ApiLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogInterceptor.class);

    @Autowired
    private ApiLogService apiLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserContext userContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 将开始时间存储到request属性中
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 获取开始时间
            Long startTime = (Long) request.getAttribute("startTime");
            long executeTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

            // 创建API日志对象
            ApiLog apiLog = new ApiLog();
            
            // 基本信息
            apiLog.setRequestUrl(request.getRequestURI());
            apiLog.setRequestMethod(request.getMethod());
            apiLog.setIpAddress(getClientIpAddress(request));
            apiLog.setUserAgent(request.getHeader("User-Agent"));
            apiLog.setExecuteTime(executeTime);
            apiLog.setCreateTime(new Date());

            // 获取用户ID（从JWT Token中）
            Long userId = getUserIdFromRequest(request);
            apiLog.setUserId(userId);

            // 请求参数
            String requestParams = getRequestParams(request);
            apiLog.setRequestParams(requestParams);

            // 请求体（如果是POST/PUT/PATCH）
            String requestBody = getRequestBody(request);
            if (requestBody != null && !requestBody.isEmpty()) {
                // 限制请求体长度，避免存储过大的数据
                if (requestBody.length() > 5000) {
                    requestBody = requestBody.substring(0, 5000) + "...(已截断)";
                }
                apiLog.setRequestBody(requestBody);
            }

            // 响应状态
            apiLog.setResponseStatus(response.getStatus());
            
            // 响应体（如果是ContentCachingResponseWrapper）
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String responseBody = new String(content, StandardCharsets.UTF_8);
                    // 限制响应体长度
                    if (responseBody.length() > 5000) {
                        responseBody = responseBody.substring(0, 5000) + "...(已截断)";
                    }
                    apiLog.setResponseBody(responseBody);
                }
            }

            // 错误信息
            if (ex != null) {
                apiLog.setErrorMessage(ex.getMessage());
                apiLog.setStatus(0); // 失败
            } else {
                // 根据HTTP状态码判断成功或失败
                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    apiLog.setStatus(1); // 成功
                } else {
                    apiLog.setStatus(0); // 失败
                }
            }

            // 异步保存日志（避免影响请求响应时间）
            saveLogAsync(apiLog);

        } catch (Exception e) {
            logger.error("记录API日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            // 从请求头获取Token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
            // 从Token中解析用户名，然后查询用户ID
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                try {
                    com.phms.pojo.User user = userContext.getCurrentUser();
                    if (user != null) {
                        return user.getId();
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
            return null;
            }
            
            // 如果Token解析失败，尝试从UserContext获取
            try {
                com.phms.pojo.User user = userContext.getCurrentUser();
                if (user != null) {
                    return user.getId();
                }
            } catch (Exception e) {
                // 忽略
            }
        } catch (Exception e) {
            logger.debug("获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            // 过滤敏感信息
            if (paramName.toLowerCase().contains("password") || 
                paramName.toLowerCase().contains("pwd")) {
                paramValue = "******";
            }
            params.put(paramName, paramValue);
        }
        if (params.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(params);
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    // 过滤敏感信息
                    if (body.contains("password") || body.contains("pwd")) {
                        body = body.replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
                        body = body.replaceAll("(\"pwd\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
                    }
                    return body;
                }
            }
        } catch (Exception e) {
            logger.debug("获取请求体失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 异步保存日志
     */
    private void saveLogAsync(ApiLog apiLog) {
        // 使用新线程异步保存，避免阻塞请求
        new Thread(() -> {
            try {
                apiLogService.saveApiLog(apiLog);
            } catch (Exception e) {
                logger.error("异步保存API日志失败: {}", e.getMessage(), e);
            }
        }).start();
    }
}
