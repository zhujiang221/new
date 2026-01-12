<template>
  <div class="tj-apply-doctor">
    <div class="page-header">
      <h2>预约统计</h2>
    </div>
    
    <div class="chart-container">
      <div v-if="loading" class="loading">加载中...</div>
      <div ref="chartRef" class="chart"></div>
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
    const resp = await http.get('/user/apply/getAllByLimitDoctor', {
      params: { page: 1, limit: 1000 }
    });
    const rows = resp.data.rows || [];
    
    let a1 = 0, a2 = 0, a3 = 0, a4 = 0;
    rows.forEach((item: any) => {
      switch (item.status) {
        case 1: a1++; break;
        case 2: a2++; break;
        case 3: a3++; break;
        case 4: a4++; break;
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
      text: '医生预约状态统计',
      subtext: '根据预约状态数量进行统计',
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
.tj-apply-doctor {
  padding: 0;
}

.chart-container {
  position: relative;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart {
  width: 100%;
  height: 500px;
}

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #666;
}
</style>

