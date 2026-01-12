<template>
  <div class="tj-daily-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📊</span>
        宠物日志统计
      </h1>
      <p class="modern-page-subtitle">按宠物查看体温/体重/身高/饭量趋势</p>
    </div>
    
    <div class="modern-search-bar">
      <select v-model="selectedPetId" @change="fetchDailyData" class="modern-input" style="max-width: 200px;">
        <option value="">请选择宠物</option>
        <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }}</option>
      </select>
      <button class="modern-btn modern-btn-primary" @click="fetchDailyData">查询</button>
    </div>
    
    <div class="modern-card chart-card">
      <div class="chart-card-header">
        <div class="chart-title">宠物日志趋势</div>
        <div class="chart-tip">最近记录的体温、体重、身高、饭量变化</div>
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

interface Pet {
  id: string;
  name: string;
}

interface DailyData {
  dateTime: string;
  weight: number;
  height: number;
  temperature: number;
  appetite: number;
}

const chartRef = ref<HTMLElement | null>(null);
const loading = ref(false);
const petList = ref<Pet[]>([]);
const selectedPetId = ref('');
let chartInstance: echarts.ECharts | null = null;

async function fetchPets() {
  try {
    const resp = await http.get('/user/pet/getAllByLimit', {
      params: { page: 1, limit: 100 }
    });
    petList.value = resp.data.rows || [];
    if (petList.value.length > 0) {
      selectedPetId.value = petList.value[0].id;
      fetchDailyData();
    }
  } catch (e) {
    console.error('获取宠物列表失败:', e);
  }
}

async function fetchDailyData() {
  if (!selectedPetId.value) {
    renderChart([]);
    return;
  }
  
  loading.value = true;
  try {
    const resp = await http.post('/health/tjDailyData', { id: selectedPetId.value });
    const dailys: DailyData[] = resp.data || [];
    renderChart(dailys);
  } catch (e) {
    console.error('获取日志数据失败:', e);
    renderChart([]);
  } finally {
    loading.value = false;
  }
}

function renderChart(dailys: DailyData[]) {
  if (!chartRef.value) return;
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  
  const dateTime: string[] = [];
  const wData: number[] = [];
  const hData: number[] = [];
  const tData: number[] = [];
  const aData: number[] = [];
  
  dailys.forEach(item => {
    dateTime.push(item.dateTime);
    wData.push(item.weight);
    hData.push(item.height);
    tData.push(item.temperature);
    aData.push(item.appetite);
  });
  
  // Hide title on mobile devices
  const isMobile = window.innerWidth <= 768;
  
  const option: echarts.EChartsOption = {
    title: isMobile ? undefined : {
      text: '宠物日志趋势'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['体温', '体重', '身高', '饭量']
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: dateTime
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '体温',
        type: 'line',
        areaStyle: {},
        data: tData
      },
      {
        name: '体重',
        type: 'line',
        areaStyle: {},
        data: wData
      },
      {
        name: '身高',
        type: 'line',
        areaStyle: {},
        data: hData
      },
      {
        name: '饭量',
        type: 'line',
        areaStyle: {},
        data: aData
      }
    ]
  };
  
  chartInstance.setOption(option);
}

function handleResize() {
  chartInstance?.resize();
  // Update chart title visibility on resize
  if (chartInstance && chartRef.value) {
    const isMobile = window.innerWidth <= 768;
    const currentOption = chartInstance.getOption() as any;
    if (currentOption && currentOption.title) {
      if (isMobile && currentOption.title[0]?.text) {
        chartInstance.setOption({ title: undefined });
      } else if (!isMobile && !currentOption.title[0]?.text) {
        chartInstance.setOption({ title: { text: '宠物日志趋势' } });
      }
    }
  }
}

onMounted(() => {
  fetchPets();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.tj-daily-modern {
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
  .modern-search-bar {
    flex-direction: column;
  }
  .modern-search-bar .modern-input,
  .modern-search-bar .modern-btn {
    width: 100%;
  }
  .chart {
    height: 420px;
  }
  .chart-title {
    display: none;
  }
}
</style>
