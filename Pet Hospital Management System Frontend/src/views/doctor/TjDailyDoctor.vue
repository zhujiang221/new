<template>
  <div class="tj-daily-doctor">
    <div class="page-header">
      <h2>宠物日志统计</h2>
    </div>
    
    <div class="search-bar">
      <label>选择宠物：</label>
      <select v-model="selectedPetId" @change="fetchDailyData">
        <option value="">请选择</option>
        <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }}</option>
      </select>
      <button class="btn btn-primary" @click="fetchDailyData">查询</button>
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
    // For doctor, we might need to fetch all pets
    const resp = await http.get('/user/petDaily/getAllByLimitDoctor', {
      params: { page: 1, limit: 100 }
    });
    const dailys = resp.data.rows || [];
    // Extract unique pets from daily records
    const petMap = new Map();
    dailys.forEach((d: any) => {
      if (d.petId && d.name) {
        petMap.set(d.petId, { id: d.petId, name: d.name });
      }
    });
    petList.value = Array.from(petMap.values());
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
    const resp = await http.post('/health/tjDailyDataDoctor', { id: selectedPetId.value });
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
.tj-daily-doctor {
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

@media (max-width: 768px) {
  .chart {
    height: 420px;
  }
}
</style>

