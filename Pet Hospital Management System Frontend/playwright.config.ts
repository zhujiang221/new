import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 配置文件
 * 宠物医院管理系统 E2E 测试配置
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e/tests',

  // 并行运行测试
  fullyParallel: true,

  // 在 CI 环境中禁止 test.only
  forbidOnly: !!process.env.CI,

  // 重试次数
  retries: process.env.CI ? 2 : 0,

  // 并行 worker 数量
  workers: process.env.CI ? 1 : undefined,

  // 测试报告
  reporter: [
    ['html', { 
      outputFolder: 'playwright-report',
      open: 'never' // 不自动打开报告
    }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ['list']
    // 如果需要Allure报告，取消下面的注释并安装 allure-playwright
    // ['allure-playwright', { outputFolder: 'allure-results' }]
  ],

  // 全局设置
  use: {
    // 基础 URL
    baseURL: 'http://localhost:5173',

    // 收集失败测试的追踪信息
    trace: 'on-first-retry',

    // 截图设置
    screenshot: {
      mode: 'only-on-failure',
      fullPage: true
    },

    // 视频录制
    video: {
      mode: 'on-first-retry',
      size: { width: 1280, height: 720 }
    },

    // 超时设置
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  // 全局超时
  timeout: 60000,

  // 预期超时
  expect: {
    timeout: 10000
  },

  // 浏览器配置
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // 可选：Firefox 测试
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // 可选：WebKit 测试
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  // 开发服务器配置
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
});

