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
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jianglianglovepet@qq.com");
            String trimmedTo = to.trim();
            message.setTo(trimmedTo);
            message.setSubject("宠物医院管理系统");
            String trimmedCode = code.trim();
            message.setText("您的验证码是：" + trimmedCode + "，有效期30分钟，请勿泄露给他人。");
            
            mailSender.send(message);
            logger.info("验证码邮件发送成功: to={}, code={}", to, code);
            return true;
        } catch (Exception e) {
            logger.error("验证码邮件发送失败: to={}", to, e);
            return false;
        }
    }
}

