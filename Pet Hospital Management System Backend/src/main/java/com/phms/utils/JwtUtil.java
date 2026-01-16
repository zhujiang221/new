package com.phms.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:7200000}")
    private Long expiration; // 默认2小时（毫秒）
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    // Redis中Token存储的key前缀
    private static final String TOKEN_KEY_PREFIX = "jwt:token:";
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成JWT令牌并存储到Redis（实现单设备登录）
     * 
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
        
        // 存储Token到Redis，实现单设备登录
        if (stringRedisTemplate != null && username != null && token != null) {
            String tokenKey = TOKEN_KEY_PREFIX + username;
            
            // 如果Redis中已有该用户的Token，先删除旧Token（使旧设备登录失效）
            String oldToken = stringRedisTemplate.opsForValue().get(tokenKey);
            if (oldToken != null && !oldToken.equals(token)) {
                logger.info("检测到用户 {} 已有Token，将删除旧Token以实现单设备登录", username);
            }
            
            // 存储新Token到Redis，设置过期时间（与JWT过期时间一致）
            stringRedisTemplate.opsForValue().set(tokenKey, token, expiration, TimeUnit.MILLISECONDS);
            logger.info("Token已存储到Redis - 用户名: {}, Token前20字符: {}", username, token.length() > 20 ? token.substring(0, 20) + "..." : token);
        }
        
        return token;
    }
    
    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 从令牌中获取Claims
     * 
     * @param token JWT令牌
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("解析JWT令牌失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证令牌是否有效（包括检查Redis中的Token）
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return validateTokenWithReason(token).isValid();
    }
    
    /**
     * 验证令牌并返回详细验证结果（包括失败原因）
     * 
     * @param token JWT令牌
     * @return 验证结果对象，包含是否有效和失败原因
     */
    public TokenValidationResult validateTokenWithReason(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return new TokenValidationResult(false, "Token格式无效");
            }
            
            // 检查令牌是否过期
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                logger.warn("JWT令牌已过期");
                return new TokenValidationResult(false, "Token已过期");
            }
            
            // 检查Token是否在Redis中（实现单设备登录）
            if (stringRedisTemplate != null) {
                String username = claims.getSubject();
                if (username != null) {
                    String tokenKey = TOKEN_KEY_PREFIX + username;
                    String storedToken = stringRedisTemplate.opsForValue().get(tokenKey);
                    
                    // 如果Redis中没有Token，或者Token不匹配，说明该Token已被新登录覆盖（单设备登录）
                    if (storedToken == null) {
                        logger.warn("Token不在Redis中，可能已被新登录覆盖 - 用户名: {}", username);
                        return new TokenValidationResult(false, "您的账户已在其他设备登录，当前设备已自动退出");
                    }
                    
                    if (!storedToken.equals(token)) {
                        logger.warn("Token不匹配，该设备登录已被新设备登录覆盖 - 用户名: {}", username);
                        return new TokenValidationResult(false, "您的账户已在其他设备登录，当前设备已自动退出");
                    }
                }
            }
            
            return new TokenValidationResult(true, null);
        } catch (Exception e) {
            logger.error("验证JWT令牌失败: {}", e.getMessage());
            return new TokenValidationResult(false, "Token验证失败");
        }
    }
    
    /**
     * Token验证结果类
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String reason;
        
        public TokenValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getReason() {
            return reason;
        }
    }
    
    /**
     * 使Token失效（用于登出或新设备登录时）
     * 
     * @param username 用户名
     */
    public void invalidateToken(String username) {
        if (stringRedisTemplate != null && username != null) {
            String tokenKey = TOKEN_KEY_PREFIX + username;
            stringRedisTemplate.delete(tokenKey);
            logger.info("Token已从Redis中删除 - 用户名: {}", username);
        }
    }
    
    /**
     * 检查Token是否在Redis中（用于验证Token是否有效）
     * 
     * @param token JWT令牌
     * @return 是否在Redis中
     */
    public boolean isTokenInRedis(String token) {
        if (stringRedisTemplate == null) {
            return true; // 如果没有Redis，默认返回true（向后兼容）
        }
        
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                return false;
            }
            
            String tokenKey = TOKEN_KEY_PREFIX + username;
            String storedToken = stringRedisTemplate.opsForValue().get(tokenKey);
            return storedToken != null && storedToken.equals(token);
        } catch (Exception e) {
            logger.error("检查Token是否在Redis中失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取令牌的过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
