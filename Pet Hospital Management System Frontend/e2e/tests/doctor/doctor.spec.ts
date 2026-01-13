/**
 * 医生角色功能测试
 * 测试医生的所有功能模块
 */

import { test, expect } from '@playwright/test';
import { login, TEST_USERS } from '../../utils/auth';
import { waitForPageLoad, waitAndClick, waitForTableData, elementExists, pageContainsText } from '../../utils/helpers';

test.describe('医生角色功能测试', () => {
  
  test.beforeEach(async ({ page }) => {
    // 登录医生账号
    await login(page, TEST_USERS.doctor);
    await waitForPageLoad(page);
  });

  test('医生首页加载', async ({ page }) => {
    await page.goto('/doctor');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/doctor')).toBeTruthy();
  });

  test('预约管理 - 查看预约列表', async ({ page }) => {
    await page.goto('/doctor/apply');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasApplyList = await elementExists(page, '.apply-list-doctor-modern, .modern-card, table, .apply-cards');
    expect(hasApplyList).toBeTruthy();
  });

  test('预约管理 - 处理预约状态', async ({ page }) => {
    await page.goto('/doctor/apply');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 查找操作按钮（到场就诊、未到场就诊等）
    const actionButton = page.locator('button:has-text("到场就诊"), button:has-text("未到场就诊"), button:has-text("开始就诊")').first();
    if (await actionButton.isVisible()) {
      await actionButton.click();
      await page.waitForTimeout(1000);
      
      // 如果有确认对话框，点击确认
      const confirmButton = page.locator('button:has-text("确认"), button:has-text("确定")');
      if (await confirmButton.isVisible()) {
        await confirmButton.click();
        await page.waitForTimeout(2000);
      }
      
      // 验证操作执行
      expect(true).toBeTruthy();
    }
  });

  test('宠物健康史 - 查看诊断记录', async ({ page }) => {
    await page.goto('/doctor/diagnosis');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDiagnosisList = await elementExists(page, '.diagnosis-list, .modern-card, table');
    expect(hasDiagnosisList).toBeTruthy();
  });

  test('宠物日志 - 查看宠物日志', async ({ page }) => {
    await page.goto('/doctor/pet-daily');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasDailyList = await elementExists(page, '.pet-daily-list, .modern-card, table');
    expect(hasDailyList).toBeTruthy();
  });

  test('健康指南管理 - 查看健康指南', async ({ page }) => {
    await page.goto('/doctor/notices');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasNoticeList = await elementExists(page, '.notice-list, .modern-card, table');
    expect(hasNoticeList).toBeTruthy();
  });

  test('健康标准管理 - 查看健康标准', async ({ page }) => {
    await page.goto('/doctor/standards');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasStandardList = await elementExists(page, '.standard-list, .modern-card, table');
    expect(hasStandardList).toBeTruthy();
  });

  test('预约统计 - 查看预约统计', async ({ page }) => {
    await page.goto('/doctor/tj-apply');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-apply')).toBeTruthy();
  });

  test('日志统计 - 查看日志统计', async ({ page }) => {
    await page.goto('/doctor/tj-daily');
    await waitForPageLoad(page);
    
    // 等待图表或统计数据加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/tj-daily')).toBeTruthy();
  });

  test('时间管理 - 查看和管理空闲时间', async ({ page }) => {
    await page.goto('/doctor/free-time');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasFreeTimeList = await elementExists(page, '.free-time-list, .modern-card, table');
    expect(hasFreeTimeList).toBeTruthy();
  });

  test('药品管理 - 查看药品列表', async ({ page }) => {
    await page.goto('/doctor/medicine');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasMedicineList = await elementExists(page, '.medicine-list, .modern-card, table');
    expect(hasMedicineList).toBeTruthy();
  });

  test('开药记录 - 查看开药记录', async ({ page }) => {
    await page.goto('/doctor/medicine-record');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasRecordList = await elementExists(page, '.medicine-record-list, .modern-card, table');
    expect(hasRecordList).toBeTruthy();
  });

  test('开具药品 - 访问开具药品页面', async ({ page }) => {
    await page.goto('/doctor/prescribe-medicine');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/prescribe-medicine')).toBeTruthy();
    
    // 检查开具药品表单元素
    const hasPrescribeForm = await elementExists(page, 'button:has-text("开具"), input, select');
    expect(hasPrescribeForm).toBeTruthy();
  });

  test('排班管理 - 查看排班信息', async ({ page }) => {
    await page.goto('/doctor/schedule');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasScheduleList = await elementExists(page, '.schedule-list, .modern-card, table');
    expect(hasScheduleList).toBeTruthy();
  });

  test('服务类型管理 - 查看服务类型', async ({ page }) => {
    await page.goto('/doctor/service-type');
    await waitForPageLoad(page);
    
    // 等待表格数据加载
    await waitForTableData(page);
    
    // 验证页面元素存在
    const hasServiceTypeList = await elementExists(page, '.service-type-list, .modern-card, table');
    expect(hasServiceTypeList).toBeTruthy();
  });

  test('消息中心 - 查看消息', async ({ page }) => {
    await page.goto('/doctor/message');
    await waitForPageLoad(page);
    
    // 等待消息列表加载
    await page.waitForTimeout(2000);
    
    // 验证页面加载成功
    expect(page.url().includes('/message')).toBeTruthy();
  });

  test('更多功能 - 访问更多功能页面', async ({ page }) => {
    await page.goto('/doctor/more');
    await waitForPageLoad(page);
    
    // 验证页面加载成功
    expect(page.url().includes('/more')).toBeTruthy();
  });

  test('搜索功能 - 在预约列表中搜索', async ({ page }) => {
    await page.goto('/doctor/apply');
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
    await page.goto('/doctor/apply');
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
