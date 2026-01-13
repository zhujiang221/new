/**
 * 测试辅助函数
 */

import { Page, expect } from '@playwright/test';

/**
 * 等待元素可见并点击
 */
export async function waitAndClick(page: Page, selector: string, timeout: number = 10000): Promise<void> {
  await page.waitForSelector(selector, { state: 'visible', timeout });
  await page.click(selector);
}

/**
 * 等待元素可见并填写
 */
export async function waitAndFill(page: Page, selector: string, value: string, timeout: number = 10000): Promise<void> {
  await page.waitForSelector(selector, { state: 'visible', timeout });
  await page.fill(selector, value);
}

/**
 * 等待元素可见并获取文本
 */
export async function waitAndGetText(page: Page, selector: string, timeout: number = 10000): Promise<string> {
  await page.waitForSelector(selector, { state: 'visible', timeout });
  return await page.textContent(selector) || '';
}

/**
 * 检查元素是否存在
 */
export async function elementExists(page: Page, selector: string, timeout: number = 5000): Promise<boolean> {
  try {
    await page.waitForSelector(selector, { state: 'visible', timeout });
    return true;
  } catch {
    return false;
  }
}

/**
 * 等待API请求完成
 */
export async function waitForApiResponse(page: Page, urlPattern: string | RegExp, timeout: number = 10000): Promise<void> {
  await page.waitForResponse(
    (response) => {
      const url = response.url();
      if (typeof urlPattern === 'string') {
        return url.includes(urlPattern);
      }
      return urlPattern.test(url);
    },
    { timeout }
  );
}

/**
 * 截图并保存
 */
export async function takeScreenshot(page: Page, name: string): Promise<void> {
  await page.screenshot({ path: `test-results/screenshots/${name}-${Date.now()}.png`, fullPage: true });
}

/**
 * 检查页面是否包含文本
 */
export async function pageContainsText(page: Page, text: string, timeout: number = 5000): Promise<boolean> {
  try {
    await expect(page.locator(`text=${text}`)).toBeVisible({ timeout });
    return true;
  } catch {
    return false;
  }
}

/**
 * 等待表格数据加载
 */
export async function waitForTableData(page: Page, tableSelector: string = 'table, .modern-card', timeout: number = 10000): Promise<void> {
  // 等待表格或卡片容器出现
  await page.waitForSelector(tableSelector, { timeout });
  
  // 等待加载状态消失
  const loadingSelector = '.loading, .modern-loading, [class*="loading"]';
  try {
    await page.waitForSelector(loadingSelector, { state: 'hidden', timeout: 5000 });
  } catch {
    // 如果没有加载状态，继续
  }
  
  await page.waitForTimeout(1000); // 额外等待确保数据渲染完成
}

/**
 * 点击按钮并等待响应
 */
export async function clickAndWait(page: Page, buttonSelector: string, waitForSelector?: string, timeout: number = 10000): Promise<void> {
  await waitAndClick(page, buttonSelector, timeout);
  
  if (waitForSelector) {
    await page.waitForSelector(waitForSelector, { timeout });
  } else {
    await page.waitForTimeout(1000);
  }
}

/**
 * 填写表单
 */
export async function fillForm(page: Page, formData: Record<string, string>): Promise<void> {
  for (const [key, value] of Object.entries(formData)) {
    const selector = `input[name="${key}"], input[placeholder*="${key}"], textarea[name="${key}"]`;
    if (await elementExists(page, selector)) {
      await waitAndFill(page, selector, value);
    }
  }
}

/**
 * 选择下拉选项
 */
export async function selectOption(page: Page, selectSelector: string, optionText: string): Promise<void> {
  await page.waitForSelector(selectSelector, { timeout: 10000 });
  await page.selectOption(selectSelector, { label: optionText });
}

/**
 * 生成随机字符串
 */
export function generateRandomString(length: number = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 生成随机邮箱
 */
export function generateRandomEmail(): string {
  return `test_${generateRandomString(8)}@test.com`;
}

/**
 * 生成随机手机号
 */
export function generateRandomPhone(): string {
  return `1${Math.floor(Math.random() * 9) + 1}${String(Math.floor(Math.random() * 100000000)).padStart(9, '0')}`;
}

/**
 * 等待页面加载完成
 */
export async function waitForPageLoad(page: Page, timeout: number = 10000): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout });
  await page.waitForTimeout(500); // 额外等待500ms确保Vue组件渲染完成
}
