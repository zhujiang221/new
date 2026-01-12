<template>
  <div class="pet-daily-list-doctor-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📓</span>
        宠物日志
      </h1>
      <p class="modern-page-subtitle">查看宠物体征记录，便于后续诊疗</p>
    </div>

    <div class="modern-search-bar">
      <input
        type="text"
        v-model="searchName"
        placeholder="🔍 按名称搜索宠物..."
        class="modern-input"
        @keyup.enter="search"
      />
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
    </div>

    <div v-if="loading" class="modern-loading">加载中...</div>
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">🐾</div>
      <div class="modern-empty-state-text">暂无日志记录</div>
    </div>

    <div v-else class="daily-cards">
      <div v-for="item in list" :key="item.id" class="modern-card daily-card">
        <div class="daily-card-header">
          <div class="pet-info">
            <div class="pet-avatar">🐶</div>
            <div class="pet-meta">
              <h3 class="pet-name">{{ item.name }}</h3>
              <span :class="getStatusClass(item.status) + ' modern-badge'">{{ formatStatus(item.status) }}</span>
            </div>
          </div>
          <div class="daily-date">{{ item.createTime || '—' }}</div>
        </div>

        <div class="daily-metrics">
          <div class="metric">
            <div class="metric-label">⚖️ 体重</div>
            <div class="metric-value">{{ item.weight }} kg</div>
          </div>
          <div class="metric">
            <div class="metric-label">📏 身高</div>
            <div class="metric-value">{{ item.height }} cm</div>
          </div>
          <div class="metric">
            <div class="metric-label">🌡️ 体温</div>
            <div class="metric-value">{{ item.temperature }} ℃</div>
          </div>
          <div class="metric">
            <div class="metric-label">🍚 饭量</div>
            <div class="metric-value">{{ item.appetite }} g</div>
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

interface PetDaily {
  id: string;
  name: string;
  weight: number;
  height: number;
  temperature: number;
  appetite: number;
  status: number;
  createTime: string;
}

const list = ref<PetDaily[]>([]);
const loading = ref(false);
const searchName = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

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
    const resp = await http.get('/user/petDaily/getAllByLimitDoctor', {
      params: {
        name: searchName.value,
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

.pet-daily-list-doctor-modern {
  padding: 0;
}

.daily-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.daily-card {
  position: relative;
  overflow: hidden;
}

.daily-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
}

.daily-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f9f8;
  margin-bottom: 12px;
}

.pet-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pet-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.2);
}

.pet-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pet-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.daily-date {
  color: #6b7280;
  font-size: 13px;
}

.daily-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 8px;
}

.metric {
  background: #f9fafb;
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 12px;
}

.metric-label {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 16px;
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
  .daily-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .modern-search-bar {
    flex-direction: column;
  }

  .modern-search-bar .modern-input,
  .modern-search-bar .modern-btn {
    width: 100%;
  }
}
</style>

