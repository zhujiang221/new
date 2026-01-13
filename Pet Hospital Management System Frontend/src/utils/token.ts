/**
 * Token管理工具函数
 * 用于存储、获取和清除JWT Token
 */

const TOKEN_KEY = 'phms_jwt_token';

/**
 * 获取Token
 */
export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch (e) {
    console.error('获取Token失败:', e);
    return null;
  }
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch (e) {
    console.error('设置Token失败:', e);
  }
}

/**
 * 清除Token
 */
export function removeToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch (e) {
    console.error('清除Token失败:', e);
  }
}

/**
 * 检查Token是否过期（可选）
 * 注意：JWT Token的过期检查通常在后端完成，这里仅提供基本检查
 */
export function isTokenExpired(token: string): boolean {
  try {
    // JWT Token格式：header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return true;
    }
    
    // 解析payload（Base64解码）
    const payload = JSON.parse(atob(parts[1]));
    
    // 检查过期时间
    if (payload.exp) {
      const expirationTime = payload.exp * 1000; // exp是秒，转换为毫秒
      return Date.now() >= expirationTime;
    }
    
    return false;
  } catch (e) {
    console.error('检查Token过期失败:', e);
    return true;
  }
}
