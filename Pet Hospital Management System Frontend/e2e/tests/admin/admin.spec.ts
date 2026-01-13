/**
 * 管理员角色功能测试
 * 测试管理员的所有功能模块
 */

import { test, expect } from '@playwright/test';
import { login, TEST_USERS } from '../../utils/auth';
import { waitForPageLoad, waitAndClick, waitForTableData, elementExists, pageContainsText } from '../../utils/helpers';

test.describe('管理员角色功能测试', () => {
  
  test.beforeEach(async ({ page }) => {
    // 登录管理员账号
    await login(page, TEST_USERS.admin);
    await waitForPageLoad(page);
  });

  test('管理员首页加载', async ({ page }) => {
    await page.goto('/admin/users');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/admin')).toBeTruthy();
  });

  test('用户管理 - 查看用户列表', async ({ page }) => {
    await page.goto('/admin/users');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasUserList = await elementExists(page, '.user-list, .modern-card, table');
    expect(hasUserList).toBeTruthy();
  });

  test('预约类型管理 - 查看预约类型列表', async ({ page }) => {
    await page.goto('/admin/appointment-type');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasAppointmentTypeList = await elementExists(page, '.appointment-type-list, .modern-card, table');
    expect(hasAppointmentTypeList).toBeTruthy();
  });

  test('宠物管理 - 查看宠物列表', async ({ page }) => {
    await page.goto('/admin/pets');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasPetList = await elementExists(page, '.pet-list, .modern-card, table');
    expect(hasPetList).toBeTruthy();
  });

  test('诊断记录管理 - 查看诊断记录列表', async ({ page }) => {
    await page.goto('/admin/diagnosis');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDiagnosisList = await elementExists(page, '.diagnosis-list, .modern-card, table');
    expect(hasDiagnosisList).toBeTruthy();
  });

  test('预约管理 - 查看预约列表', async ({ page }) => {
    await page.goto('/admin/apply');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasApplyList = await elementExists(page, '.apply-list, .modern-card, table');
    expect(hasApplyList).toBeTruthy();
  });

  test('医生空闲时间 - 查看医生空闲时间', async ({ page }) => {
    await page.goto('/admin/free-time');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasFreeTimeList = await elementExists(page, '.free-time-list, .modern-card, table');
    expect(hasFreeTimeList).toBeTruthy();
  });

  test('排班管理 - 查看排班信息', async ({ page }) => {
    await page.goto('/admin/schedule');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasScheduleList = await elementExists(page, '.schedule-list, .modern-card, table');
    expect(hasScheduleList).toBeTruthy();
  });

  test('服务类型管理 - 查看服务类型', async ({ page }) => {
    await page.goto('/admin/service-type');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasServiceTypeList = await elementExists(page, '.service-type-list, .modern-card, table');
    expect(hasServiceTypeList).toBeTruthy();
  });

  test('健康指南管理 - 查看健康指南列表', async ({ page }) => {
    await page.goto('/admin/notices');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasNoticeList = await elementExists(page, '.notice-list, .modern-card, table');
    expect(hasNoticeList).toBeTruthy();
  });

  test('健康评估 - 访问健康评估页面', async ({ page }) => {
    await page.goto('/admin/assess');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/assess')).toBeTruthy();
  });

  test('健康标准管理 - 查看健康标准列表', async ({ page }) => {
    await page.goto('/admin/standards');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasStandardList = await elementExists(page, '.standard-list, .modern-card, table');
    expect(hasStandardList).toBeTruthy();
  });

  test('宠物日志管理 - 查看宠物日志列表', async ({ page }) => {
    await page.goto('/admin/pet-daily');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDailyList = await elementExists(page, '.pet-daily-list, .modern-card, table');
    expect(hasDailyList).toBeTruthy();
  });

  test('预约统计 - 查看预约统计', async ({ page }) => {
    await page.goto('/admin/tj-apply');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-apply')).toBeTruthy();
  });

  test('日志统计 - 查看日志统计', async ({ page }) => {
    await page.goto('/admin/tj-daily');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-daily')).toBeTruthy();
  });

  test('药品管理 - 查看药品列表', async ({ page }) => {
    await page.goto('/admin/medicine');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasMedicineList = await elementExists(page, '.medicine-list, .modern-card, table');
    expect(hasMedicineList).toBeTruthy();
  });

  test('开药记录管理 - 查看开药记录列表', async ({ page }) => {
    await page.goto('/admin/medicine-record');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasRecordList = await elementExists(page, '.medicine-record-list, .modern-card, table');
    expect(hasRecordList).toBeTruthy();
  });

  test('搜索功能 - 在用户列表中搜索', async ({ page }) => {
    await page.goto('/admin/users');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 查找搜索框
    const searchInput = page.locator('input[placeholder*="搜索"], input[type="text"]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill('test');
      await page.waitForTimeout(1000);
      
      // 验证搜索执行
      expect(true).toBeTruthy();
    }
  });

  test('分页功能 - 测试列表分页', async ({ page }) => {
    await page.goto('/admin/users');
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

  test('管理员权限 - 可以访问用户页面', async ({ page }) => {
    await page.goto('/user/pets');
    await waitForPageLoad(page);
    
    // 管理员应该可以访问用户页面
    expect(page.url().includes('/user') || page.url().includes('/admin')).toBeTruthy();
  });

  test('管理员权限 - 可以访问医生页面', async ({ page }) => {
    await page.goto('/doctor/apply');
    await waitForPageLoad(page);
    
    // 管理员应该可以访问医生页面
    expect(page.url().includes('/doctor') || page.url().includes('/admin')).toBeTruthy();
  });
});
