<template>
  <div class="assess-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>🩺</span>
        健康评估
      </h1>
      <p class="modern-page-subtitle">基于最新日志与标准，查看宠物雷达评估</p>
    </div>

    <div class="modern-search-bar">
      <select v-model="selectedPetId" @change="fetchData" class="modern-input" style="max-width: 300px;">
        <option value="">查看所有宠物</option>
        <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }} ({{ pet.type }})</option>
      </select>
    </div>
    
    <div class="chart-card modern-card">
      <div class="chart-card-header">
        <div class="chart-title">雷达图评估</div>
        <div class="chart-tip">{{ selectedPetId ? '查看选中宠物的健康评估' : '同时展示多只宠物最新体征' }}</div>
      </div>
      <div class="chart-container">
        <div v-if="loading" class="modern-loading">加载中...</div>
        <div class="chart-scroll">
          <div ref="chartRef" class="chart"></div>
        </div>
      </div>
    </div>
    
    <div class="assessment-details" v-if="assessmentData.length > 0">
      <div class="details-header">
        <h3>评估详情</h3>
        <p>与健康标准对比的结果说明</p>
      </div>
      <div class="detail-grid">
        <div v-for="item in assessmentData" :key="item.name" class="detail-card modern-card">
          <h4>{{ item.name }}</h4>
          <div class="metrics">
            <p><strong>体温：</strong> {{ item.tempStatus }}</p>
            <p><strong>体重：</strong> {{ item.weightStatus }}</p>
            <p><strong>身高：</strong> {{ item.heightStatus }}</p>
            <p><strong>饭量：</strong> {{ item.appetiteStatus }}</p>
          </div>
        </div>
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
  type: string;
  birthday: string;
}

interface PetDaily {
  petId: string;
  weight: number;
  height: number;
  temperature: number;
  appetite: number;
}

interface Standard {
  type: string;
  ageMin: number;
  ageMax: number;
  tempMin: number;
  tempMax: number;
  weightMin: number;
  weightMax: number;
  heightMin: number;
  heightMax: number;
  appetiteMin: number;
  appetiteMax: number;
}

interface AssessmentItem {
  name: string;
  tempStatus: string;
  weightStatus: string;
  heightStatus: string;
  appetiteStatus: string;
}

const chartRef = ref<HTMLElement | null>(null);
const loading = ref(true);
const assessmentData = ref<AssessmentItem[]>([]);
const petList = ref<Pet[]>([]);
const selectedPetId = ref('');
let chartInstance: echarts.ECharts | null = null;

async function fetchPets() {
  try {
    // Admin can see all pets using doctor API
    const petsResp = await http.get('/user/pet/getAllByLimitDoctor', {
      params: { page: 1, limit: 1000 }
    });
    petList.value = petsResp.data.rows || [];
  } catch (e) {
    console.error('获取宠物列表失败:', e);
  }
}

async function fetchData() {
  loading.value = true;
  try {
    // Fetch all pets (admin can see all users' pets)
    const petsResp = await http.get('/user/pet/getAllByLimitDoctor', {
      params: { page: 1, limit: 1000 }
    });
    let pets: Pet[] = petsResp.data.rows || [];
    
    // Filter by selected pet if one is selected
    if (selectedPetId.value) {
      pets = pets.filter(p => p.id === selectedPetId.value);
    }
    
    if (pets.length === 0) {
      loading.value = false;
      return;
    }
    
    // Fetch all pet daily logs (admin can see all)
    const dailyResp = await http.get('/user/petDaily/getAllByLimitDoctor', {
      params: { page: 1, limit: 10000 }
    });
    const dailys: any[] = dailyResp.data.rows || [];
    
    // Fetch health standards
    const standardsResp = await http.get('/user/standard/getAllByLimit', {
      params: { page: 1, limit: 100 }
    });
    const standards: Standard[] = standardsResp.data.rows || [];
    
    // Process data for each pet
    const names: string[] = [];
    const radarData: any[] = [];
    const seriesData: any[] = [];
    const assessments: AssessmentItem[] = [];

    const petValues: { name: string; value: number[]; standard: Standard | {
      tempMin: number;
      tempMax: number;
      weightMin: number;
      weightMax: number;
      heightMin: number;
      heightMax: number;
      appetiteMin: number;
      appetiteMax: number;
    } }[] = [];

    let dataTempMax = 0;
    let dataWeightMax = 0;
    let dataHeightMax = 0;
    let dataAppetiteMax = 0;

    let stdTempMax = 0;
    let stdWeightMax = 0;
    let stdHeightMax = 0;
    let stdAppetiteMax = 0;

    pets.forEach((pet) => {
      names.push(pet.name);

      const petDailys = dailys
        .filter((d: any) => d.petId === pet.id || d.name === pet.name)
        .sort((a: any, b: any) => {
          const ta = new Date(a.createTime || a.dateTime || 0).getTime();
          const tb = new Date(b.createTime || b.dateTime || 0).getTime();
          return tb - ta;
        });
      const latestDaily = petDailys[0] || { temperature: 0, weight: 0, height: 0, appetite: 0 };

      const petAge = calculateAge(pet.birthday);
      const standard: Standard | {
        tempMin: number;
        tempMax: number;
        weightMin: number;
        weightMax: number;
        heightMin: number;
        heightMax: number;
        appetiteMin: number;
        appetiteMax: number;
      } = standards.find(s => 
        s.type === pet.type && 
        petAge >= s.ageMin && 
        petAge <= s.ageMax
      ) || standards.find(s => s.type === pet.type) || {
        tempMin: 37, tempMax: 40,
        weightMin: 0, weightMax: 50,
        heightMin: 0, heightMax: 100,
        appetiteMin: 0, appetiteMax: 500
      };

      const t = latestDaily.temperature || 0;
      const w = latestDaily.weight || 0;
      const h = latestDaily.height || 0;
      const a = latestDaily.appetite || 0;

      dataTempMax = Math.max(dataTempMax, t);
      dataWeightMax = Math.max(dataWeightMax, w);
      dataHeightMax = Math.max(dataHeightMax, h);
      dataAppetiteMax = Math.max(dataAppetiteMax, a);

      stdTempMax = Math.max(stdTempMax, standard.tempMax || 0);
      stdWeightMax = Math.max(stdWeightMax, standard.weightMax || 0);
      stdHeightMax = Math.max(stdHeightMax, standard.heightMax || 0);
      stdAppetiteMax = Math.max(stdAppetiteMax, standard.appetiteMax || 0);

      petValues.push({
        name: pet.name,
        value: [t, w, h, a],
        standard
      });
    });

    const clamp = (value: number, min: number, max: number) => {
      if (!value || isNaN(value)) return min;
      return Math.min(Math.max(value, min), max);
    };

    const GLOBAL_MAX_TEMP = 45;
    const GLOBAL_MAX_WEIGHT = 200;
    const GLOBAL_MAX_HEIGHT = 250;
    const GLOBAL_MAX_APPETITE = 1000;

    const indicator = [
      { text: '体温', max: clamp(Math.max(dataTempMax, stdTempMax) * 1.1, 30, GLOBAL_MAX_TEMP) },
      { text: '体重', max: clamp(Math.max(dataWeightMax, stdWeightMax) * 1.1, 1, GLOBAL_MAX_WEIGHT) },
      { text: '身高', max: clamp(Math.max(dataHeightMax, stdHeightMax) * 1.1, 10, GLOBAL_MAX_HEIGHT) },
      { text: '饭量', max: clamp(Math.max(dataAppetiteMax, stdAppetiteMax) * 1.1, 10, GLOBAL_MAX_APPETITE) }
    ];

    const petCount = pets.length;
    const step = 100 / Math.max(petCount, 1);

    petValues.forEach((p, index) => {
      const [t, w, h, a] = p.value;
      const standard = p.standard;

      radarData.push({
        indicator,
        center: [`${step / 2 + index * step}%`, '50%'],
        radius: 80
      });

      seriesData.push({
        type: 'radar',
        radarIndex: index,
        tooltip: { trigger: 'item' },
        areaStyle: {},
        data: [{
          name: p.name,
          value: p.value
        }]
      });

      assessments.push({
        name: p.name,
        tempStatus: getStatus(t, standard.tempMin, standard.tempMax, '体温'),
        weightStatus: getStatus(w, standard.weightMin, standard.weightMax, '体重'),
        heightStatus: getStatus(h, standard.heightMin, standard.heightMax, '身高'),
        appetiteStatus: getStatus(a, standard.appetiteMin, standard.appetiteMax, '饭量')
      });
    });
    
    assessmentData.value = assessments;
    renderChart(names, radarData, seriesData, pets.length);
  } catch (e) {
    console.error('获取数据失败:', e);
  } finally {
    loading.value = false;
  }
}

function calculateAge(birthday: string): number {
  if (!birthday) return 0;
  const birth = new Date(birthday);
  const now = new Date();
  const years = (now.getTime() - birth.getTime()) / (365.25 * 24 * 60 * 60 * 1000);
  return Math.floor(years);
}

function getStatus(value: number, min: number, max: number, label: string): string {
  if (!value) return `${label}数据不足`;
  if (value < min) return `${label}偏低 (${value})`;
  if (value > max) return `${label}偏高 (${value})`;
  return `${label}正常 (${value})`;
}

function renderChart(names: string[], radarData: any[], seriesData: any[], petCount: number) {
  if (!chartRef.value) return;
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  const baseWidth = 900;
  const widthPerPet = 350;
  const totalWidth = Math.max(baseWidth, petCount * widthPerPet);
  (chartRef.value as HTMLElement).style.width = `${totalWidth}px`;
  
  const isMobile = window.innerWidth <= 768;

  const option: echarts.EChartsOption = {
    title: {
      // 移动端隐藏标题文字，桌面端保留
      text: isMobile ? '' : '宠物健康分析'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      left: 'center',
      data: names
    },
    radar: radarData,
    series: seriesData
  };
  
  chartInstance.setOption(option);
}

function handleResize() {
  chartInstance?.resize();
}

onMounted(() => {
  fetchPets();
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

.assess-modern {
  padding: 20px;
}

.chart-card {
  padding: 16px;
  margin-bottom: 20px;
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

.chart-scroll {
  width: 100%;
  overflow-x: auto;
}

.chart {
  height: 400px;
}

.assessment-details {
  margin-top: 20px;
}

.details-header h3 {
  margin: 0 0 6px 0;
}

.details-header p {
  margin: 0 0 12px 0;
  color: #6b7280;
  font-size: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.detail-card h4 {
  margin: 0 0 10px;
  color: #111827;
}

.metrics p {
  margin: 5px 0;
  font-size: 14px;
  color: #444;
}

@media (max-width: 768px) {
  .chart {
    height: 360px;
  }
}
</style>
