import axios from 'axios';

const http = axios.create({
  timeout: 10000,
  withCredentials: true, // Important for Shiro session cookies
});

// Request interceptor
http.interceptors.request.use(
  (config) => {
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

// Response interceptor
http.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API请求错误:', error);
    // Handle 401/403 - redirect to login
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // 只有在确实需要清除用户信息时才清除（比如token过期）
      // 这里不清除用户信息，让用户手动退出登录
      // 因为可能是临时的网络问题或权限问题
      const currentPath = window.location.pathname;
      // 如果不在登录页，才重定向
      if (currentPath !== '/' && !currentPath.startsWith('/login')) {
        // 延迟重定向，避免频繁跳转
        setTimeout(() => {
          window.location.href = '/';
        }, 100);
      }
    }
    return Promise.reject(error);
  }
);

export default http;
