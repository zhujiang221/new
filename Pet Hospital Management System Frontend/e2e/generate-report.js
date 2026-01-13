/**
 * 测试报告生成脚本
 * 生成HTML、JSON和Allure格式的测试报告
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const reportDir = path.join(__dirname, '../playwright-report');
const resultsDir = path.join(__dirname, '../test-results');
const allureResultsDir = path.join(__dirname, '../allure-results');
const allureReportDir = path.join(__dirname, '../allure-report');

// 确保目录存在
[reportDir, resultsDir, allureResultsDir, allureReportDir].forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

console.log('📊 开始生成测试报告...\n');

// 1. 生成Playwright HTML报告
console.log('1. 生成Playwright HTML报告...');
try {
  execSync('npx playwright show-report', { stdio: 'inherit' });
  console.log('✅ Playwright HTML报告已生成: playwright-report/index.html\n');
} catch (error) {
  console.error('❌ 生成Playwright HTML报告失败:', error.message);
}

// 2. 生成Allure报告（如果安装了allure）
console.log('2. 生成Allure报告...');
try {
  execSync('npx allure generate allure-results --clean -o allure-report', { stdio: 'inherit' });
  console.log('✅ Allure报告已生成: allure-report/index.html\n');
} catch (error) {
  console.log('⚠️  Allure报告生成跳过（需要安装allure命令行工具）\n');
}

// 3. 读取测试结果并生成摘要报告
console.log('3. 生成测试摘要...');
try {
  const resultsFile = path.join(resultsDir, 'results.json');
  if (fs.existsSync(resultsFile)) {
    const results = JSON.parse(fs.readFileSync(resultsFile, 'utf-8'));
    
    const summary = {
      total: results.stats?.total || 0,
      passed: results.stats?.passed || 0,
      failed: results.stats?.failed || 0,
      skipped: results.stats?.skipped || 0,
      duration: results.stats?.duration || 0,
      timestamp: new Date().toISOString()
    };
    
    const summaryFile = path.join(resultsDir, 'summary.json');
    fs.writeFileSync(summaryFile, JSON.stringify(summary, null, 2));
    
    console.log('📋 测试摘要:');
    console.log(`   总测试数: ${summary.total}`);
    console.log(`   通过: ${summary.passed} ✅`);
    console.log(`   失败: ${summary.failed} ❌`);
    console.log(`   跳过: ${summary.skipped} ⏭️`);
    console.log(`   耗时: ${(summary.duration / 1000).toFixed(2)}秒`);
    console.log(`\n✅ 测试摘要已保存: ${summaryFile}\n`);
  }
} catch (error) {
  console.error('❌ 生成测试摘要失败:', error.message);
}

// 4. 生成Markdown格式的报告
console.log('4. 生成Markdown报告...');
try {
  const resultsFile = path.join(resultsDir, 'results.json');
  if (fs.existsSync(resultsFile)) {
    const results = JSON.parse(fs.readFileSync(resultsFile, 'utf-8'));
    
    let markdown = '# 宠物医院管理系统 - 自动化测试报告\n\n';
    markdown += `**生成时间**: ${new Date().toLocaleString('zh-CN')}\n\n`;
    markdown += `## 测试概览\n\n`;
    markdown += `| 指标 | 数量 |\n`;
    markdown += `|------|------|\n`;
    markdown += `| 总测试数 | ${results.stats?.total || 0} |\n`;
    markdown += `| ✅ 通过 | ${results.stats?.passed || 0} |\n`;
    markdown += `| ❌ 失败 | ${results.stats?.failed || 0} |\n`;
    markdown += `| ⏭️ 跳过 | ${results.stats?.skipped || 0} |\n`;
    markdown += `| ⏱️ 耗时 | ${((results.stats?.duration || 0) / 1000).toFixed(2)}秒 |\n\n`;
    
    if (results.stats?.failed > 0 && results.suites) {
      markdown += `## 失败的测试\n\n`;
      markdown += `| 测试名称 | 文件 | 错误信息 |\n`;
      markdown += `|---------|------|----------|\n`;
      
      // 遍历suites查找失败的测试
      function findFailedTests(suite, prefix = '') {
        let failedTests = [];
        if (suite.specs) {
          suite.specs.forEach(spec => {
            spec.tests.forEach(test => {
              if (test.results && test.results.some(r => r.status === 'failed')) {
                const error = test.results.find(r => r.status === 'failed')?.error?.message || '未知错误';
                failedTests.push({
                  title: `${prefix}${spec.title}`,
                  file: spec.file || '',
                  error: error.substring(0, 100) + (error.length > 100 ? '...' : '')
                });
              }
            });
          });
        }
        if (suite.suites) {
          suite.suites.forEach(subSuite => {
            failedTests = failedTests.concat(findFailedTests(subSuite, `${prefix}${subSuite.title} > `));
          });
        }
        return failedTests;
      }
      
      const failedTests = findFailedTests(results.suites[0] || {});
      failedTests.forEach(test => {
        markdown += `| ${test.title} | ${test.file} | ${test.error} |\n`;
      });
    }
    
    const markdownFile = path.join(resultsDir, 'report.md');
    fs.writeFileSync(markdownFile, markdown);
    console.log(`✅ Markdown报告已生成: ${markdownFile}\n`);
  }
} catch (error) {
  console.error('❌ 生成Markdown报告失败:', error.message);
}

console.log('🎉 测试报告生成完成！');
console.log('\n📁 报告文件位置:');
console.log(`   - HTML报告: ${reportDir}/index.html`);
console.log(`   - JSON结果: ${resultsDir}/results.json`);
console.log(`   - Markdown报告: ${resultsDir}/report.md`);
if (fs.existsSync(allureReportDir)) {
  console.log(`   - Allure报告: ${allureReportDir}/index.html`);
}
