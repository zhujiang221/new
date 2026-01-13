/**
 * 认证功能测试
 * 测试登录、注册、密码重置等功能
 */

import { test, expect } from '@playwright/test';
import { login, logout, TEST_USERS } from '../utils/auth';
import { waitForPageLoad, elementExists } from '../utils/helpers';

test.describe('认证功能测试', () => {
  
  test.beforeEach(async ({ page }) => {
    // 清除所有存储
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  test('登录页面加载', async ({ page }) => {
    await page.goto('/');
    await waitForPageLoad(page);
    
    // 检查登录表单元素
    await expect(page.locator('input[placeholder*="用户名"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button:has-text("登录"), .btn-login')).toBeVisible();
  });

  test('管理员登录', async ({ page }) => {
    await login(page, TEST_USERS.admin);
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/admin\/users.*|.*\/home.*/);
    
    // 检查是否显示管理员相关元素
    const adminElements = await elementExists(page, 'text=用户管理, text=预约管理, text=宠物管理');
    expect(adminElements || page.url().includes('/admin')).toBeTruthy();
  });

  test('医生登录', async ({ page }) => {
    await login(page, TEST_USERS.doctor);
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/doctor.*/);
    
    // 检查是否显示医生相关元素
    expect(page.url().includes('/doctor')).toBeTruthy();
  });

  test('用户登录', async ({ page }) => {
    await login(page, TEST_USERS.user);
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/user.*/);
    
    // 检查是否显示用户相关元素
    expect(page.url().includes('/user')).toBeTruthy();
  });

  test('登录失败 - 错误用户名', async ({ page }) => {
    await page.goto('/');
    await waitForPageLoad(page);
    
    await page.fill('input[placeholder*="用户名"]', 'wrong_user');
    await page.fill('input[type="password"]', 'wrong_password');
    
    // 处理验证码（如果存在）
    const captchaInput = page.locator('input[placeholder*="验证码"]');
    if (await captchaInput.isVisible()) {
      await captchaInput.fill('1234');
    }
    
    await page.click('button:has-text("登录"), .btn-login');
    
    // 等待错误提示
    await page.waitForTimeout(2000);
    
    // 应该仍在登录页或显示错误信息
    const currentUrl = page.url();
    expect(currentUrl.includes('/login') || currentUrl === '/' || await elementExists(page, '.error, .error-tip')).toBeTruthy();
  });

  test('登录失败 - 错误密码', async ({ page }) => {
    await page.goto('/');
    await waitForPageLoad(page);
    
    await page.fill('input[placeholder*="用户名"]', TEST_USERS.user.username);
    await page.fill('input[type="password"]', 'wrong_password');
    
    // 处理验证码（如果存在）
    const captchaInput = page.locator('input[placeholder*="验证码"]');
    if (await captchaInput.isVisible()) {
      await captchaInput.fill('1234');
    }
    
    await page.click('button:has-text("登录"), .btn-login');
    
    // 等待错误提示
    await page.waitForTimeout(2000);
    
    // 应该仍在登录页或显示错误信息
    const currentUrl = page.url();
    expect(currentUrl.includes('/login') || currentUrl === '/' || await elementExists(page, '.error, .error-tip')).toBeTruthy();
  });

  test('登出功能', async ({ page }) => {
    // 先登录
    await login(page, TEST_USERS.user);
    await waitForPageLoad(page);
    
    // 登出
    await logout(page);
    
    // 验证已登出
    await expect(page).toHaveURL(/.*\/$/, { timeout: 5000 });
  });

  test('注册页面访问', async ({ page }) => {
    await page.goto('/register');
    await waitForPageLoad(page);
    
    // 检查注册表单元素
    const hasRegisterForm = await elementExists(page, 'input[placeholder*="用户名"], input[placeholder*="密码"], input[placeholder*="邮箱"]');
    expect(hasRegisterForm).toBeTruthy();
  });

  test('忘记密码页面访问', async ({ page }) => {
    await page.goto('/forget-password');
    await waitForPageLoad(page);
    
    // 检查忘记密码表单元素
    const hasPasswordForm = await elementExists(page, 'input[placeholder*="邮箱"], input[placeholder*="用户名"]');
    expect(hasPasswordForm).toBeTruthy();
  });

  test('未登录访问受保护页面 - 重定向到登录页', async ({ page }) => {
    // 清除所有存储
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    
    // 尝试访问用户页面
    await page.goto('/user/pets');
    
    // 应该重定向到登录页
    await expect(page).toHaveURL(/.*\/$/, { timeout: 10000 });
  });

  test('角色权限控制 - 用户无法访问管理员页面', async ({ page }) => {
    await login(page, TEST_USERS.user);
    await waitForPageLoad(page);
    
    // 尝试访问管理员页面
    await page.goto('/admin/users');
    await waitForPageLoad(page);
    
    // 应该被重定向到用户首页
    await expect(page).toHaveURL(/.*\/user.*/, { timeout: 10000 });
  });

  test('角色权限控制 - 用户无法访问医生页面', async ({ page }) => {
    await login(page, TEST_USERS.user);
    await waitForPageLoad(page);
    
    // 尝试访问医生页面
    await page.goto('/doctor/apply');
    await waitForPageLoad(page);
    
    // 应该被重定向到用户首页
    await expect(page).toHaveURL(/.*\/user.*/, { timeout: 10000 });
  });

  test('角色权限控制 - 医生无法访问管理员页面', async ({ page }) => {
    await login(page, TEST_USERS.doctor);
    await waitForPageLoad(page);
    
    // 尝试访问管理员页面
    await page.goto('/admin/users');
    await waitForPageLoad(page);
    
    // 应该被重定向到医生首页
    await expect(page).toHaveURL(/.*\/doctor.*/, { timeout: 10000 });
  });

  test('角色权限控制 - 管理员可以访问所有页面', async ({ page }) => {
    await login(page, TEST_USERS.admin);
    await waitForPageLoad(page);
    
    // 管理员应该可以访问用户页面
    await page.goto('/user/pets');
    await waitForPageLoad(page);
    expect(page.url().includes('/user') || page.url().includes('/admin')).toBeTruthy();
    
    // 管理员应该可以访问医生页面
    await page.goto('/doctor/apply');
    await waitForPageLoad(page);
    expect(page.url().includes('/doctor') || page.url().includes('/admin')).toBeTruthy();
  });
});
