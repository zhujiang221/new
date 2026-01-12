package com.phms.service.impl;

import com.phms.service.EmailCodeService;
import com.phms.utils.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * 邮箱验证码服务实现类
 */
@Service
public class EmailCodeServiceImpl implements EmailCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
    
    @Autowired
    private EmailUtil emailUtil;
    
    // Session中存储验证码的key前缀
    private static final String CODE_KEY_PREFIX = "email_code_";
    // Session中存储发送时间的key前缀
    private static final String SEND_TIME_KEY_PREFIX = "email_send_time_";
    // Session中存储验证码生成时间的key前缀
    private static final String CREATE_TIME_KEY_PREFIX = "email_create_time_";
    
    // 验证码有效期（毫秒）
    private static final long CODE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟
    // 发送间隔（毫秒）
    private static final long SEND_INTERVAL = 60 * 1000; // 60秒
    
    @Override
    public boolean sendCode(String email, HttpSession session) {
        try {
            // 检查是否在1分钟内已发送过
            String sendTimeKey = SEND_TIME_KEY_PREFIX + email;
            Long lastSendTime = (Long) session.getAttribute(sendTimeKey);
            long currentTime = System.currentTimeMillis();
            
            if (lastSendTime != null) {
                long timeSinceLastSend = currentTime - lastSendTime;
                if (timeSinceLastSend < SEND_INTERVAL) {
                    long remainingSeconds = (SEND_INTERVAL - timeSinceLastSend) / 1000;
                    logger.warn("验证码发送过于频繁: email={}, 还需等待{}秒", email, remainingSeconds);
                    return false;
                }
            }
            
            // 生成验证码
            String code = emailUtil.generateCode();
            
            // 发送邮件
            boolean sendSuccess = emailUtil.sendVerificationCode(email, code);
            if (!sendSuccess) {
                logger.error("验证码邮件发送失败: email={}", email);
                return false;
            }
            
            // 存储验证码、发送时间和生成时间到Session
            String codeKey = CODE_KEY_PREFIX + email;
            String createTimeKey = CREATE_TIME_KEY_PREFIX + email;
            session.setAttribute(codeKey, code);
            session.setAttribute(sendTimeKey, currentTime);
            session.setAttribute(createTimeKey, currentTime);
            
            logger.info("验证码发送成功: email={}, code={}", email, code);
            return true;
        } catch (Exception e) {
            logger.error("发送验证码异常: email={}", email, e);
            return false;
        }
    }
    
    @Override
    public String verifyCode(String email, String code, HttpSession session) {
        if (email == null || email.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        if (code == null || code.trim().isEmpty()) {
            return "验证码不能为空";
        }
        
        String codeKey = CODE_KEY_PREFIX + email;
        String createTimeKey = CREATE_TIME_KEY_PREFIX + email;
        String storedCode = (String) session.getAttribute(codeKey);
        Long createTime = (Long) session.getAttribute(createTimeKey);
        
        if (storedCode == null || createTime == null) {
            logger.warn("验证码不存在或已过期: email={}", email);
            return "验证码不存在或已过期，请重新获取";
        }
        
        // 检查验证码是否过期
        long currentTime = System.currentTimeMillis();
        if (currentTime - createTime > CODE_EXPIRE_TIME) {
            logger.warn("验证码已过期: email={}, 已过期{}秒", email, (currentTime - createTime - CODE_EXPIRE_TIME) / 1000);
            clearCode(email, session);
            return "验证码已过期，请重新获取";
        }
        
        // 检查验证码是否匹配
        if (!storedCode.equals(code.trim())) {
            logger.warn("验证码错误: email={}, input={}, stored={}", email, code, storedCode);
            return "验证码错误，请重新输入";
        }
        
        // 验证码正确，清除验证码
        clearCode(email, session);
        logger.info("验证码验证成功: email={}", email);
        return null; // null表示验证成功
    }
    
    @Override
    public void clearCode(String email, HttpSession session) {
        String codeKey = CODE_KEY_PREFIX + email;
        String sendTimeKey = SEND_TIME_KEY_PREFIX + email;
        String createTimeKey = CREATE_TIME_KEY_PREFIX + email;
        session.removeAttribute(codeKey);
        session.removeAttribute(sendTimeKey);
        session.removeAttribute(createTimeKey);
        logger.debug("验证码已清除: email={}", email);
    }
}

