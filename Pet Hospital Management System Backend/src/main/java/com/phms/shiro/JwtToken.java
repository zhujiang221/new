package com.phms.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * JWT Token类
 * 实现Shiro的AuthenticationToken接口，用于JWT认证
 */
public class JwtToken implements AuthenticationToken {
    
    private String token;
    
    public JwtToken(String token) {
        this.token = token;
    }
    
    @Override
    public Object getPrincipal() {
        return token;
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    public String getToken() {
        return token;
    }
}
