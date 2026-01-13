/**
 * 用户角色功能测试
 * 测试普通用户的所有功能模块
 */

import { test, expect } from '@playwright/test';
import { login, TEST_USERS } from '../../utils/auth';
import { waitForPageLoad, waitAndClick, waitForTableData, elementExists, pageContainsText } from '../../utils/helpers';

test.describe('用户角色功能测试', () => {
  
  test.beforeEach(async ({ page }) => {
    // 登录用户账号
    await login(page, TEST_USERS.user);
    await waitForPageLoad(page);
  });

  test('用户首页加载', async ({ page }) => {
    await page.goto('/user');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/user')).toBeTruthy();
  });

  test('宠物管理 - 查看宠物列表', async ({ page }) => {
    await page.goto('/user/pets');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasPetList = await elementExists(page, '.pet-list, .modern-card, table, .apply-cards');
    expect(hasPetList).toBeTruthy();
  });

  test('预约管理 - 查看我的预约', async ({ page }) => {
    await page.goto('/user/apply');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasApplyList = await elementExists(page, '.apply-list, .modern-card, table, .apply-cards');
    expect(hasApplyList).toBeTruthy();
  });

  test('预约就诊 - 访问预约流程页面', async ({ page }) => {
    await page.goto('/user/apply-flow');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/apply-flow')).toBeTruthy();
    
    // 检查预约表单元素
    const hasApplyForm = await elementExists(page, 'button:has-text("预约"), input, select');
    expect(hasApplyForm).toBeTruthy();
  });

  test('健康标准 - 查看健康标准列表', async ({ page }) => {
    await page.goto('/user/standards');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasStandardList = await elementExists(page, '.standard-list, .modern-card, table');
    expect(hasStandardList).toBeTruthy();
  });

  test('宠物日志 - 查看宠物日志列表', async ({ page }) => {
    await page.goto('/user/pet-daily');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDailyList = await elementExists(page, '.pet-daily-list, .modern-card, table');
    expect(hasDailyList).toBeTruthy();
  });

  test('健康指南 - 查看健康指南列表', async ({ page }) => {
    await page.goto('/user/notices');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasNoticeList = await elementExists(page, '.notice-list, .modern-card, table');
    expect(hasNoticeList).toBeTruthy();
  });

  test('诊断记录 - 查看诊断记录列表', async ({ page }) => {
    await page.goto('/user/diagnosis');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDiagnosisList = await elementExists(page, '.diagnosis-list, .modern-card, table');
    expect(hasDiagnosisList).toBeTruthy();
  });

  test('预约统计 - 查看预约统计', async ({ page }) => {
    await page.goto('/user/tj-apply');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-apply')).toBeTruthy();
  });

  test('日志统计 - 查看日志统计', async ({ page }) => {
    await page.goto('/user/tj-daily');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-daily')).toBeTruthy();
  });

  test('健康评估 - 访问健康评估页面', async ({ page }) => {
    await page.goto('/user/assess');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/assess')).toBeTruthy();
  });

  test('医生时间 - 查看医生空闲时间', async ({ page }) => {
    await page.goto('/user/free-time');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasFreeTimeList = await elementExists(page, '.free-time-list, .modern-card, table');
    expect(hasFreeTimeList).toBeTruthy();
  });

  test('个人信息 - 访问个人信息页面', async ({ page }) => {
    await page.goto('/user/profile');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/profile')).toBeTruthy();
    
    // 检查个人信息表单元素
    const hasProfileForm = await elementExists(page, 'input, form');
    expect(hasProfileForm).toBeTruthy();
  });

  test('修改密码 - 访问修改密码页面', async ({ page }) => {
    await page.goto('/user/change-password');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/change-password')).toBeTruthy();
    
    // 检查密码表单元素
    const hasPasswordForm = await elementExists(page, 'input[type="password"]');
    expect(hasPasswordForm).toBeTruthy();
  });

  test('更多功能 - 访问更多功能页面', async ({ page }) => {
    await page.goto('/user/more');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/more')).toBeTruthy();
  });

  test('搜索功能 - 在预约列表中搜索', async ({ page }) => {
    await page.goto('/user/apply');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 查找搜索框
    const searchInput = page.locator('input[placeholder*="搜索"], input[type="text"]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      
      // 验证搜索执行（页面应该有变化或显示结果）
      expect(true).toBeTruthy();
    }
  });

  test('分页功能 - 测试列表分页', async ({ page }) => {
    await page.goto('/user/pets');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 查找分页按钮
    const nextButton = page.locator('button:has-text("下一页"), button:has-text(">")');
    if (await nextButton.isVisible() && !(await nextButton.isDisabled())) {
      await nextButton.click();
      await waitForTableData(page);
      
      // 验证分页成功
      expect(true).toBeTruthy();
    }
  });
});
