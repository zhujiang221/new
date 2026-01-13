import axios from 'axios';
import { getToken, removeToken } from '../utils/token';
import { clearUserInfo } from '../utils/user';
import { showMessage } from '../utils/message';
import router from '../router';

const http = axios.create({
  timeout: 10000,
  withCredentials: false, // JWT不需要Cookie
});

// Request interceptor
http.interceptors.request.use(
  (config) => {
    // 添加JWT Token到请求头
    const token = getToken();
    // 排除登录、验证码等公开接口
    const url = config.url || '';
    const isPublicPath = url === '/login' || url === '/captcha' || 
                        url.startsWith('/open/') || url === '/sendEmailCode' || 
                        url === '/doRegist' || url === '/resetPassword' || 
                        url === '/sendResetPasswordCode' || url === '/regist';
    
    if (token && !isPublicPath) {
      config.headers['Authorization'] = `Bearer ${token}`;
      // 调试信息：检查token是否正确添加到请求头
      console.log(`[HTTP] 添加Token到请求头: ${config.method?.toUpperCase()} ${url}, Token: ${token.substring(0, 20)}...`);
    } else if (!isPublicPath) {
      console.warn(`[HTTP] 请求需要Token但未找到: ${config.method?.toUpperCase()} ${url}`);
    }
    
    // Only set Content-Type for POST/PUT/PATCH requests with data
    if (config.data && ['post', 'put', 'patch'].includes(config.method?.toLowerCase() || '')) {
      // Set Content-Type for form data
      if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
      }
      
      // Convert object to URLSearchParams for form data
      // Skip if already a string, FormData, or URLSearchParams
      if (typeof config.data === 'object' && !(config.data instanceof FormData)) {
        // If it's already URLSearchParams, convert to string
        if (config.data instanceof URLSearchParams) {
          config.data = config.data.toString();
        } else {
          // Convert object to URLSearchParams
          const params = new URLSearchParams();
          for (const key in config.data) {
            if (config.data[key] !== undefined && config.data[key] !== null) {
              // Handle arrays by appending multiple values with the same key
              if (Array.isArray(config.data[key])) {
                config.data[key].forEach((value: any) => {
                  params.append(key, String(value));
                });
              } else {
                params.append(key, String(config.data[key]));
              }
            }
          }
          config.data = params.toString();
        }
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 用于标记是否已经处理过401错误，避免重复提示
let isHandling401 = false;

// Response interceptor
http.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API请求错误:', error);
    // Handle 401/403 - Token过期或无效
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // 避免重复处理
      if (isHandling401) {
        return Promise.reject(error);
      }
      
      isHandling401 = true;
      
      const currentPath = window.location.pathname;
      // 如果不在登录页，才处理
      if (currentPath !== '/' && !currentPath.startsWith('/login')) {
        // 检查错误消息，判断是否是Token被新设备登录覆盖
        // 后端可能返回的格式：{ message: "该设备登录已被新设备登录覆盖，请重新登录" } 或直接是字符串
        let errorMessage = '';
        const errorData = error.response?.data;
        
        // 尝试解析错误信息
        if (typeof errorData === 'string') {
          try {
            // 尝试解析JSON字符串
            const parsed = JSON.parse(errorData);
            errorMessage = parsed.message || parsed.msg || errorData;
          } catch (e) {
            // 如果不是JSON，直接使用字符串
            errorMessage = errorData;
          }
        } else if (errorData && typeof errorData === 'object') {
          errorMessage = errorData.message || errorData.msg || '';
        }
        
        // 检查是否是Token被新设备登录覆盖
        const isNewDeviceLogin = errorMessage.includes('已在其他设备登录') || 
                                  errorMessage.includes('当前设备已自动退出') ||
                                  errorMessage.includes('新设备登录') ||
                                  errorMessage.includes('已被新设备登录覆盖');
        
        // 清除Token和用户信息
        removeToken();
        clearUserInfo();
        
        // 显示友好的提示信息
        if (isNewDeviceLogin) {
          showMessage('您的账户已在其他设备登录，当前设备已自动退出，请重新登录。若不是您本人操作，请及时修改密码', 'error');
        } else {
          showMessage(errorMessage || '登录已过期，请重新登录', 'error');
        }
        
        // 延迟重定向，让用户看到提示信息
        setTimeout(() => {
          isHandling401 = false;
          router.push('/').catch(() => {
            // 如果路由跳转失败，使用window.location
            window.location.href = '/';
          });
        }, 3000); // 1.5秒后跳转，让用户看到提示
      } else {
        isHandling401 = false;
      }
    }
    return Promise.reject(error);
  }
);

export default http;
