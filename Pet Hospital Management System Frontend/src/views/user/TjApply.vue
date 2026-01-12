<template>
  <div class="tj-apply-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📈</span>
        预约统计
      </h1>
      <p class="modern-page-subtitle">按状态查看预约分布，理解办事效率</p>
    </div>
    
    <div class="modern-card chart-card">
      <div class="chart-card-header">
        <div class="chart-title">预约状态数量统计</div>
        <div class="chart-tip">来源：预约列表（最近全部数据）</div>
      </div>
      <div class="chart-container">
        <div v-if="loading" class="modern-loading">加载中...</div>
        <div ref="chartRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import http from '../../api/http';

const chartRef = ref<HTMLElement | null>(null);
const loading = ref(true);
let chartInstance: echarts.ECharts | null = null;

async function fetchData() {
  loading.value = true;
  try {
    // Get appointment statistics
    const resp = await http.get('/user/apply/getAllByLimit', {
      params: { page: 1, limit: 1000 }
    });
    const rows = resp.data.rows || [];
    
    // Count by status
    let a1 = 0, a2 = 0, a3 = 0, a4 = 0;
    rows.forEach((item: any) => {
      switch (item.status) {
        case 1: a1++; break; // 申请中
        case 2: a2++; break; // 申请通过
        case 3: a3++; break; // 不通过
        case 4: a4++; break; // 已完成
      }
    });
    
    renderChart(a1, a2, a3, a4);
  } catch (e) {
    console.error('获取数据失败:', e);
  } finally {
    loading.value = false;
  }
}

function renderChart(a1: number, a2: number, a3: number, a4: number) {
  if (!chartRef.value) return;
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  
  const option: echarts.EChartsOption = {
    title: {
      text: '预约状态数量统计',
      subtext: '根据预约状态数量进行统计,体现医院整体办事效率',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['申请中', '申请通过', '不通过', '已完成']
    },
    series: [
      {
        name: '预约状态',
        type: 'pie',
        radius: '55%',
        center: ['50%', '60%'],
        data: [
          { value: a1, name: '申请中' },
          { value: a2, name: '申请通过' },
          { value: a3, name: '不通过' },
          { value: a4, name: '已完成' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  chartInstance.setOption(option);
}

function handleResize() {
  chartInstance?.resize();
}

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.tj-apply-modern {
  padding: 0;
}

.chart-card {
  padding: 16px;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f9f8;
  margin-bottom: 12px;
}

.chart-title {
  font-weight: 600;
  color: #111827;
}

.chart-tip {
  color: #6b7280;
  font-size: 13px;
}

.chart-container {
  position: relative;
}

.chart {
  width: 100%;
  height: 500px;
}

@media (max-width: 768px) {
  .chart {
    height: 420px;
  }
}
</style>
