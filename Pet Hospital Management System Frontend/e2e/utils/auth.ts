/**
 * 认证工具类
 * 用于测试中的登录、登出等操作
 */

import { Page, expect } from '@playwright/test';

export interface TestUser {
  username: string;
  password: string;
  role: 'admin' | 'doctor' | 'user';
  name?: string;
}

// 测试用户账号（需要根据实际数据库中的账号调整）
export const TEST_USERS: Record<string, TestUser> = {
  admin: {
    username: 'admin',
    password: '123456',
    role: 'admin',
    name: '管理员'
  },
  doctor: {
    username: 'doctor',
    password: '123456',
    role: 'doctor',
    name: '医生'
  },
  user: {
    username: 'user',
    password: '123456',
    role: 'user',
    name: '用户'
  }
};

/**
 * 登录
 */
export async function login(page: Page, user: TestUser): Promise<void> {
  await page.goto('/');
  
  // 等待登录页面加载
  await page.waitForSelector('input[placeholder*="用户名"]', { timeout: 10000 });
  
  // 填写用户名
  await page.fill('input[placeholder*="用户名"]', user.username);
  
  // 填写密码
  await page.fill('input[type="password"]', user.password);
  
  // 获取验证码（如果存在）
  const captchaInput = page.locator('input[placeholder*="验证码"]');
  if (await captchaInput.isVisible()) {
    // 验证码通常需要手动输入，这里我们尝试点击刷新或等待
    // 在实际测试中，可能需要禁用验证码或使用测试账号
    await page.waitForTimeout(1000);
    // 尝试输入一个测试验证码（如果后端允许）
    await captchaInput.fill('1234');
  }
  
  // 点击登录按钮
  const loginButton = page.locator('button:has-text("登录"), .btn-login');
  await loginButton.click();
  
  // 等待登录完成（根据角色跳转到不同页面）
  await page.waitForTimeout(2000);
  
  // 验证登录成功（检查是否跳转到对应角色的首页）
  const currentUrl = page.url();
  if (user.role === 'admin') {
    await expect(page).toHaveURL(/.*\/admin\/users.*|.*\/home.*/, { timeout: 10000 });
  } else if (user.role === 'doctor') {
    await expect(page).toHaveURL(/.*\/doctor.*/, { timeout: 10000 });
  } else {
    await expect(page).toHaveURL(/.*\/user.*/, { timeout: 10000 });
  }
}

/**
 * 登出
 */
export async function logout(page: Page): Promise<void> {
  // 查找登出按钮（可能在菜单中）
  const logoutButton = page.locator('button:has-text("退出"), button:has-text("登出"), .logout-btn');
  
  if (await logoutButton.isVisible()) {
    await logoutButton.click();
    await page.waitForTimeout(1000);
  } else {
    // 如果没有找到登出按钮，清除localStorage和sessionStorage
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    await page.goto('/');
  }
  
  // 验证已登出（应该跳转到登录页）
  await expect(page).toHaveURL(/.*\/$/, { timeout: 5000 });
}

/**
 * 检查是否已登录
 */
export async function isLoggedIn(page: Page, expectedRole?: 'admin' | 'doctor' | 'user'): Promise<boolean> {
  const currentUrl = page.url();
  
  if (expectedRole === 'admin') {
    return currentUrl.includes('/admin') || currentUrl.includes('/home');
  } else if (expectedRole === 'doctor') {
    return currentUrl.includes('/doctor');
  } else if (expectedRole === 'user') {
    return currentUrl.includes('/user');
  }
  
  // 如果没有指定角色，检查是否不在登录页
  return !currentUrl.endsWith('/') && !currentUrl.includes('/login');
}

/**
 * 等待页面加载完成
 */
export async function waitForPageLoad(page: Page, timeout: number = 10000): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout });
  await page.waitForTimeout(500); // 额外等待500ms确保Vue组件渲染完成
}

/**
 * 获取当前用户角色
 */
export async function getCurrentUserRole(page: Page): Promise<'admin' | 'doctor' | 'user' | null> {
  const url = page.url();
  if (url.includes('/admin')) return 'admin';
  if (url.includes('/doctor')) return 'doctor';
  if (url.includes('/user')) return 'user';
  return null;
}
