package com.phms.service.impl;

import com.phms.service.EmailCodeService;
import com.phms.utils.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现类
 */
@Service
public class EmailCodeServiceImpl implements EmailCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
    
    @Autowired
    private EmailUtil emailUtil;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // Redis中存储验证码的key前缀
    private static final String CODE_KEY_PREFIX = "email_code:";
    // Redis中存储发送时间的key前缀
    private static final String SEND_TIME_KEY_PREFIX = "email_send_time:";
    
    // 验证码有效期（秒）
    private static final long CODE_EXPIRE_TIME = 30 * 60; // 30分钟
    // 发送间隔（秒）
    private static final long SEND_INTERVAL = 60; // 60秒
    
    @Override
    public boolean sendCode(String email) {
        try {
            // 检查是否在1分钟内已发送过
            String sendTimeKey = SEND_TIME_KEY_PREFIX + email;
            String lastSendTimeStr = stringRedisTemplate.opsForValue().get(sendTimeKey);
            long currentTime = System.currentTimeMillis();
            
            if (lastSendTimeStr != null) {
                try {
                    Long lastSendTime = Long.parseLong(lastSendTimeStr);
                    long timeSinceLastSend = currentTime - lastSendTime;
                    if (timeSinceLastSend < SEND_INTERVAL * 1000) {
                        long remainingSeconds = (SEND_INTERVAL * 1000 - timeSinceLastSend) / 1000;
                        logger.warn("验证码发送过于频繁: email={}, 还需等待{}秒", email, remainingSeconds);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("解析上次发送时间失败: email={}", email);
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
            
            // 存储验证码到Redis，设置30分钟过期
            String codeKey = CODE_KEY_PREFIX + email;
            stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
            
            // 存储发送时间到Redis，设置60秒过期（用于限制发送频率）
            stringRedisTemplate.opsForValue().set(sendTimeKey, String.valueOf(currentTime), SEND_INTERVAL, TimeUnit.SECONDS);
            
            logger.info("验证码发送成功: email={}, code={}", email, code);
            return true;
        } catch (Exception e) {
            logger.error("发送验证码异常: email={}", email, e);
            return false;
        }
    }
    
    @Override
    public String verifyCode(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        if (code == null || code.trim().isEmpty()) {
            return "验证码不能为空";
        }
        
        String codeKey = CODE_KEY_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            logger.warn("验证码不存在或已过期: email={}", email);
            return "验证码不存在或已过期，请重新获取";
        }
        
        // 检查验证码是否匹配
        if (!storedCode.equals(code.trim())) {
            logger.warn("验证码错误: email={}, input={}, stored={}", email, code, storedCode);
            return "验证码错误，请重新输入";
        }
        
        // 验证码正确，清除验证码（一次性使用）
        stringRedisTemplate.delete(codeKey);
        logger.info("验证码验证成功: email={}", email);
        return null; // null表示验证成功
    }
    
    @Override
    public void clearCode(String email) {
        String codeKey = CODE_KEY_PREFIX + email;
        String sendTimeKey = SEND_TIME_KEY_PREFIX + email;
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(sendTimeKey);
        logger.debug("验证码已清除: email={}", email);
    }
}
