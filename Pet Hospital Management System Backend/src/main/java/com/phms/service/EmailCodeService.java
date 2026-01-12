package com.phms.service;

import javax.servlet.http.HttpSession;

/**
 * 邮箱验证码服务接口
 */
public interface EmailCodeService {
    
    /**
     * 发送验证码到指定邮箱
     * 
     * @param email 邮箱地址
     * @param session HttpSession
     * @return 是否发送成功
     */
    boolean sendCode(String email, HttpSession session);
    
    /**
     * 验证验证码是否正确
     * 
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @param session HttpSession
     * @return 验证结果信息，null表示验证成功，非null表示错误信息
     */
    String verifyCode(String email, String code, HttpSession session);
    
    /**
     * 清除验证码
     * 
     * @param email 邮箱地址
     * @param session HttpSession
     */
    void clearCode(String email, HttpSession session);
}

