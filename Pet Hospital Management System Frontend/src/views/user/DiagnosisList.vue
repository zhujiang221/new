<template>
  <div class="diagnosis-list-modern">
    <!-- 页面标题 -->
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>🏥</span>
        诊断记录
      </h1>
      <p class="modern-page-subtitle">查看您的宠物诊断历史记录</p>
    </div>

    <!-- 搜索栏 -->
    <div class="modern-search-bar">
      <input 
        type="text" 
        v-model="searchInfo" 
        placeholder="🔍 搜索诊疗建议..." 
        class="modern-input"
        @keyup.enter="search" 
      />
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="modern-loading">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">📋</div>
      <div class="modern-empty-state-text">暂无诊断记录</div>
    </div>

    <!-- 诊断记录卡片列表 -->
    <div v-else class="diagnosis-cards">
      <div v-for="item in list" :key="item.id" class="modern-card diagnosis-card">
        <div class="diagnosis-card-header">
          <div class="diagnosis-pet-info">
            <div class="pet-icon">🐾</div>
            <div class="pet-details">
              <h3 class="pet-name">{{ item.name }}</h3>
              <span class="pet-owner">主人：{{ item.userName }}</span>
            </div>
          </div>
          <div class="diagnosis-status">
            <span :class="getStatusClass(item.status) + ' modern-badge'">{{ formatStatus(item.status) }}</span>
          </div>
        </div>
        
        <div class="diagnosis-card-body">
          <div class="diagnosis-info-section">
            <h4 class="section-title">📝 诊疗建议</h4>
            <p class="diagnosis-info-text">{{ item.info }}</p>
          </div>
          
          <div class="diagnosis-meta">
            <div class="meta-item">
              <span class="meta-label">👨‍⚕️ 医生</span>
              <span class="meta-value">{{ item.doctorName }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">🏷️ 类型</span>
              <span class="meta-value type-badge" :class="getTypeClass(item.type)">{{ formatType(item.type) }}</span>
            </div>
            <div class="meta-item" v-if="item.sex">
              <span class="meta-label">⚧️ 性别</span>
              <span class="meta-value">{{ formatSex(item.sex) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">🕐 时间</span>
              <span class="meta-value">{{ item.createTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
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
import { useRoute } from 'vue-router';
import http from '../../api/http';

const route = useRoute();

interface Diagnosis {
  id: string;
  name: string;
  userName: string;
  doctorName: string;
  info: string;
  type: number;
  status: number;
  createTime: string;
  sex?: string | number;
}

const list = ref<Diagnosis[]>([]);
const loading = ref(false);
const searchInfo = ref('');
const petIdParam = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

function formatType(type: number) {
  switch (type) {
    case 1: return '正常就医';
    case 2: return '注射疫苗';
    case 3: return '驱虫';
    default: return '未知';
  }
}

function formatStatus(status: number) {
  switch (status) {
    case 1: return '正常';
    case 2: return '严重';
    case 3: return '较重';
    case 4: return '死亡';
    default: return '未知';
  }
}

function getStatusClass(status: number) {
  switch (status) {
    case 1: return 'status-badge status-approved';
    case 2: return 'status-badge status-rejected';
    case 3: return 'status-badge status-pending';
    case 4: return 'status-badge status-rejected';
    default: return 'status-badge';
  }
}

function getTypeClass(type: number) {
  switch (type) {
    case 1: return 'type-normal';
    case 2: return 'type-vaccine';
    case 3: return 'type-deworm';
    default: return '';
  }
}

function formatSex(sex: string | number) {
  const sexStr = String(sex);
  if (sexStr === '1') return '公';
  if (sexStr === '2') return '母';
  if (sexStr === '3') return '未知';
  return sexStr || '未知';
}

async function fetchData() {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      info: searchInfo.value,
      page: pagination.page,
      limit: pagination.limit
    };
    if (petIdParam.value) {
      params.petId = petIdParam.value;
    }
    const resp = await http.get('/user/diagnosis/getAllByLimit', { params });
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
  // Check for petId query parameter
  petIdParam.value = (route.query.petId as string) || '';
  fetchData();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.diagnosis-list-modern {
  padding: 0;
}

.diagnosis-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.diagnosis-card {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.diagnosis-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
}

.diagnosis-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f9f8;
}

.diagnosis-pet-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.pet-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.2);
}

.pet-details {
  flex: 1;
}

.pet-name {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.pet-owner {
  font-size: 13px;
  color: #6b7280;
}

.diagnosis-status {
  flex-shrink: 0;
}

.diagnosis-card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.diagnosis-info-section {
  flex: 1;
}

.section-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.diagnosis-info-text {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
  font-size: 14px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #72C1BB;
}

.diagnosis-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-size: 13px;
  color: #6b7280;
  flex-shrink: 0;
}

.meta-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-normal {
  background: #dbeafe;
  color: #1e40af;
}

.type-vaccine {
  background: #d1fae5;
  color: #065f46;
}

.type-deworm {
  background: #fef3c7;
  color: #92400e;
}

/* 状态标签样式 */
.status-badge.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .diagnosis-card-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .diagnosis-meta {
    grid-template-columns: 1fr;
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

