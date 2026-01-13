package com.phms.service;

/**
 * 邮箱验证码服务接口
 */
public interface EmailCodeService {
    
    /**
     * 发送验证码到指定邮箱
     * 
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendCode(String email);
    
    /**
     * 验证验证码是否正确
     * 
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return 验证结果信息，null表示验证成功，非null表示错误信息
     */
    String verifyCode(String email, String code);
    
    /**
     * 清除验证码
     * 
     * @param email 邮箱地址
     */
    void clearCode(String email);
}

