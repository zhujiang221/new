import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 导入path模块，规范路径解析

// 提取代理目标常量，简化配置，便于后续维护
const PROXY_TARGET = 'http://localhost:8186'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 关键：使用path.resolve规范路径，避免不同系统的路径问题
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0', // 允许局域网访问（手机/其他电脑可访问）
    port: 5173, // 优先使用5173端口
    strictPort: false,
    open: true, // 自动打开浏览器
   
    // 确保前端资源不被代理拦截
    fs: {
      strict: false
    },
    proxy: {
      // Login and authentication
      '/login': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/logout': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/regist': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/doRegist': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/sendEmailCode': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/sendResetPasswordCode': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/resetPassword': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/captcha': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Admin APIs（排除前端路由页面）
      // 排除的前端路由：users, pets, diagnosis, apply, free-time, schedule, service-type, 
      // notices, assess, standards, pet-daily, tj-apply, tj-daily, medicine, medicine-record, 
      // chat, broadcast, appointment-type, api-log
      '^/admin/(?!roles$|pages$|users$|user-role$|pets$|diagnosis$|apply$|free-time$|schedule$|service-type$|notices$|assess$|standards$|pet-daily$|tj-apply$|tj-daily$|medicine$|medicine-record$|chat$|broadcast$|appointment-type$|api-log$).*': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // User APIs - 代理所有API路径，排除前端路由页面
      // 注意：Vite代理配置中，更具体的规则应该放在前面
      // 策略：先精确匹配API路径，再排除前端路由页面
      
      // 1. 精确匹配所有已知的API子路径（优先级最高）
      // 匹配格式：/user/{module}/{action}，如 /user/pet/getAllByLimit
      '/user/pet/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/apply/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/petDaily/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/diagnosis/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/notice/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/standard/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/medicine/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/user/medicineRecord/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // 2. 匹配用户主页相关API（/user/home/*）
      '/user/home/': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // 3. 匹配用户信息相关API（无子路径，直接是 /user/{action}）
      '^/user/(checkEmail|updatePassword|checkUserPassword|updateUser|getUserById|getCurrentUser|getMessage)$': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // 4. 匹配其他所有 /user/* 路径，但排除前端路由页面
      // 前端路由页面（精确匹配，不包含子路径）：
      // /user/pets, /user/apply, /user/apply-flow, /user/standards, /user/pet-daily, 
      // /user/notices, /user/diagnosis, /user/tj-apply, /user/tj-daily, 
      // /user/assess, /user/free-time, /user/profile, /user/change-password, /user/message, /user/more, /user/mine
      '^/user/(?!pets$|apply$|apply-flow$|standards$|pet-daily$|notices$|diagnosis$|tj-apply$|tj-daily$|assess$|free-time$|profile$|change-password$|message$|more$|mine$).+': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Doctor schedule APIs (explicit rule for schedule endpoints)
      '/doctor/schedule': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Doctor APIs（排除前端路由）
      // 排除的前端路由：apply, diagnosis, pet-daily, notices, standards, tj-apply, tj-daily, 
      // free-time, message, medicine, medicine-record, prescribe-medicine, schedule, 
      // service-type, more, chat, chat/request, chat/:id
      '^/doctor/(?!apply$|diagnosis$|pet-daily$|notices$|standards$|tj-apply$|tj-daily$|free-time$|message$|medicine$|medicine-record$|prescribe-medicine$|schedule$|service-type$|more$|chat$|chat/).*': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Chat APIs
      '/api/chat': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Notification APIs
      '/notification': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // WebSocket proxy for notifications (must be before other /ws rules)
      '/ws/notification': {
        target: PROXY_TARGET,
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path // 保持路径不变
      },
      // WebSocket proxy (fallback for other /ws paths)
      '/ws': {
        target: PROXY_TARGET,
        ws: true,
        changeOrigin: true
      },
      // Health APIs
      '/health': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Appointment Type APIs
      '/appointmentType': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // File upload
      '/upload': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // File access (for uploaded files)
      '/file': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      // Static resources from backend
      '/imgs': {
        target: PROXY_TARGET,
        changeOrigin: true,
        bypass(req, res, options) {
          if (req.url && (req.url.includes('?import') || req.url.includes('?url') || req.url.includes('?raw'))) {
            return req.url;
          }
          const accept = req.headers.accept || '';
          if (accept.includes('text/javascript') || accept.includes('application/javascript') || accept.includes('module')) {
            return req.url;
          }
        }
      },
      '/css': {
        target: PROXY_TARGET,
        changeOrigin: true
      },
      '/js': {
        target: PROXY_TARGET,
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
})