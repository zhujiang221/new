package com.phms.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 邮件发送工具类
 * 用于发送邮箱验证码
 */
@Component
public class EmailUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    private static final Random random = new Random();
    
    /**
     * 生成6位数字验证码
     * 
     * @return 6位数字验证码字符串
     */
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 发送验证码邮件
     * 
     * @param to 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String to, String code) {
        if (to == null || to.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            logger.warn("发送验证码失败: 参数为空, to={}, code={}", to, code);
            return false;
        }
        
        // 重试机制：最多重试3次，第一次失败后等待更长时间
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("开始发送验证码邮件: to={}, 尝试次数={}/{}", to, attempt, maxRetries);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("jianglianglovepet@qq.com");
                String trimmedTo = to.trim();
                message.setTo(trimmedTo);
                message.setSubject("宠物医院管理系统 - 验证码");
                String trimmedCode = code.trim();
                message.setText("您的验证码是：" + trimmedCode + "，有效期30分钟，请勿泄露给他人。");
                
                long startTime = System.currentTimeMillis();
                mailSender.send(message);
                long endTime = System.currentTimeMillis();
                logger.info("验证码邮件发送成功: to={}, code={}, 耗时={}ms, 尝试次数={}", 
                        to, code, (endTime - startTime), attempt);
                return true;
            } catch (org.springframework.mail.MailAuthenticationException e) {
                logger.error("验证码邮件发送失败（认证失败）: to={}, 尝试次数={}, 错误信息={}", 
                        to, attempt, e.getMessage());
                if (e.getCause() != null) {
                    logger.error("根本原因: {}", e.getCause().getMessage());
                }
                // 认证失败通常不需要重试
                if (attempt < maxRetries) {
                    long waitTime = attempt * 2000; // 递增等待时间：2秒、4秒
                    logger.warn("等待{}秒后重试...", waitTime / 1000);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logger.error("认证失败，请检查QQ邮箱授权码是否正确，或查看QQ邮箱配置说明.md");
                    return false;
                }
            } catch (org.springframework.mail.MailSendException e) {
                logger.error("验证码邮件发送失败（MailSendException）: to={}, 尝试次数={}, 错误信息={}", 
                        to, attempt, e.getMessage());
                if (e.getCause() != null) {
                    logger.error("根本原因: {}", e.getCause().getMessage());
                    if (e.getCause().getCause() != null) {
                        logger.error("更深层原因: {}", e.getCause().getCause().getMessage());
                    }
                }
                // 如果是最后一次尝试，返回失败
                if (attempt >= maxRetries) {
                    return false;
                }
                // 递增等待时间，第一次失败后等待更长时间
                long waitTime = attempt * 2000; // 2秒、4秒
                logger.warn("连接失败，等待{}秒后重试...", waitTime / 1000);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } catch (Exception e) {
                logger.error("验证码邮件发送失败（未知异常）: to={}, 尝试次数={}, 错误类型={}, 错误信息={}", 
                        to, attempt, e.getClass().getSimpleName(), e.getMessage(), e);
                // 如果是最后一次尝试，返回失败
                if (attempt >= maxRetries) {
                    return false;
                }
                // 递增等待时间
                long waitTime = attempt * 2000; // 2秒、4秒
                logger.warn("等待{}秒后重试...", waitTime / 1000);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
}

