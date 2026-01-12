package com.phms.websocket;

import com.alibaba.fastjson.JSON;
import com.phms.pojo.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息通知WebSocket处理器
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    
    // 维护在线用户连接映射（userId -> WebSocketSession）
    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session中获取用户ID（需要在连接时通过参数传递）
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            logger.info("WebSocket连接建立 - 用户ID: {}", userId);
        } else {
            logger.warn("WebSocket连接建立失败 - 无法获取用户ID");
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            logger.info("WebSocket连接关闭 - 用户ID: {}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息（如果需要双向通信）
        logger.debug("收到WebSocket消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误", exception);
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    /**
     * 从WebSocketSession中获取用户ID
     * 客户端连接时需要传递userId参数，例如：ws://localhost:8186/ws/notification?userId=123
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userIdStr = query.substring(query.indexOf("userId=") + 7);
                // 如果还有其他参数，需要截取
                int nextParamIndex = userIdStr.indexOf("&");
                if (nextParamIndex > 0) {
                    userIdStr = userIdStr.substring(0, nextParamIndex);
                }
                return Long.parseLong(userIdStr);
            }
            // 如果URL中没有userId参数，尝试从session属性中获取
            Object userIdObj = session.getAttributes().get("userId");
            if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            }
        } catch (Exception e) {
            logger.error("获取用户ID失败", e);
        }
        return null;
    }

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param message 消息内容（JSON字符串）
     */
    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                logger.info("WebSocket消息推送成功 - 用户ID: {}", userId);
            } catch (IOException e) {
                logger.error("WebSocket消息推送失败 - 用户ID: " + userId, e);
                // 连接异常，移除session
                userSessions.remove(userId);
            }
        } else {
            logger.debug("用户不在线，消息将保存到数据库 - 用户ID: {}", userId);
        }
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return true=在线，false=离线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}
