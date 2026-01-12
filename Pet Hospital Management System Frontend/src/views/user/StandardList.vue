<template>
  <div class="standard-list-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>🧭</span>
        健康标准
      </h1>
      <p class="modern-page-subtitle">查看不同年龄与类型的健康标准区间</p>
    </div>

    <div class="modern-search-bar">
      <select v-model="searchType" @change="search" class="modern-input" style="max-width: 160px;">
        <option value="">全部类型</option>
        <option value="1">猫科</option>
        <option value="2">犬科</option>
      </select>
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
    </div>

    <div v-if="loading" class="modern-loading">加载中...</div>
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">📊</div>
      <div class="modern-empty-state-text">暂无健康标准</div>
    </div>

    <div v-else class="standard-cards">
      <div v-for="item in list" :key="item.id" class="modern-card standard-card">
        <div class="standard-card-header">
          <div class="standard-type">{{ formatType(item.type) }}</div>
          <span :class="getStatusClass(item.status) + ' modern-badge'">{{ formatStatus(item.status) }}</span>
        </div>

        <div class="standard-ages">
          <div class="age-range">年龄：{{ item.ageMin }} - {{ item.ageMax }} 岁</div>
        </div>

        <div class="standard-grid">
          <div class="standard-item">
            <div class="label">体温</div>
            <div class="value">{{ item.tempMin }} ~ {{ item.tempMax }} ℃</div>
          </div>
          <div class="standard-item">
            <div class="label">体重</div>
            <div class="value">{{ item.weightMin }} ~ {{ item.weightMax }} kg</div>
          </div>
          <div class="standard-item">
            <div class="label">身高</div>
            <div class="value">{{ item.heightMin }} ~ {{ item.heightMax }} cm</div>
          </div>
          <div class="standard-item">
            <div class="label">饭量</div>
            <div class="value">{{ item.appetiteMin }} ~ {{ item.appetiteMax }} g</div>
          </div>
        </div>
      </div>
    </div>

    <div class="modern-pagination">
      <span class="modern-pagination-info">共 {{ pagination.total }} 条</span>
      <button :disabled="pagination.page <= 1" @click="changePage(pagination.page - 1)">上一页</button>
      <span class="modern-pagination-info">第 {{ pagination.page }} / {{ totalPages }} 页</span>
      <button :disabled="pagination.page >= totalPages" @click="changePage(pagination.page + 1)">下一页</button>
      <select v-model="pagination.limit" @change="search" class="modern-input" style="width: auto; padding: 8px 12px;">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';

interface Standard {
  id: string;
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
  status: number;
  type: string;
}

const list = ref<Standard[]>([]);
const loading = ref(false);
const searchType = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

function formatType(type: string) {
  return type === '1' ? '猫科' : type === '2' ? '犬科' : type;
}

function formatStatus(status: number) {
  switch (status) {
    case 1: return '正常';
    case 2: return '异常';
    case 3: return '生病';
    default: return '未知';
  }
}

function getStatusClass(status: number) {
  switch (status) {
    case 1: return 'status-badge status-approved';
    case 2: return 'status-badge status-pending';
    case 3: return 'status-badge status-rejected';
    default: return 'status-badge';
  }
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/user/standard/getAllByLimit', {
      params: {
        type: searchType.value,
        page: pagination.page,
        limit: pagination.limit
      }
    });
    const data = resp.data;
    list.value = data.rows || [];
    pagination.total = data.total || 0;
  } catch (e) {
    console.error('获取数据失败:', e);
  } finally {
    loading.value = false;
  }
}

function search() {
  pagination.page = 1;
  fetchData();
}

function changePage(page: number) {
  pagination.page = page;
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.standard-list-modern {
  padding: 0;
}

.standard-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.standard-card {
  position: relative;
  overflow: hidden;
}

.standard-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
}

.standard-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f9f8;
  margin-bottom: 10px;
}

.standard-type {
  font-weight: 600;
  color: #111827;
}

.standard-ages {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 10px;
}

.standard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.standard-item {
  background: #f9fafb;
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 10px;
}

.label {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 6px;
}

.value {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.status-badge.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

@media (max-width: 768px) {
  .modern-search-bar {
    flex-direction: column;
  }

  .modern-search-bar .modern-input,
  .modern-search-bar .modern-btn {
    width: 100%;
  }
}
</style>

