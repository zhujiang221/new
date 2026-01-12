<template>
  <div class="pet-daily-list-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📓</span>
        宠物日志管理
      </h1>
      <p class="modern-page-subtitle">查看和管理所有宠物的日志记录</p>
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
      <button class="modern-btn modern-btn-primary" @click="showAddModal = true">
        ➕ 添加日志
      </button>
    </div>

    <div v-if="loading" class="modern-loading">加载中...</div>
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">🐾</div>
      <div class="modern-empty-state-text">暂无日志记录</div>
      <button class="modern-btn modern-btn-primary" @click="showAddModal = true" style="margin-top: 12px;">
        添加第一条日志
      </button>
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
          <div class="daily-date">{{ item.createTime || item.id || '--' }}</div>
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

        <div class="daily-actions">
          <button class="modern-btn modern-btn-danger modern-btn-sm" @click="handleDelete(item)">
            🗑️ 删除
          </button>
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

    <div v-if="showAddModal" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal">
        <div class="modal-header">
          <h3>添加宠物日志</h3>
          <button class="modal-close" @click="closeAddModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>选择宠物：</label>
            <select v-model="dailyForm.petId" class="form-control">
              <option value="">请选择宠物</option>
              <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>体重(Kg)：</label>
            <input type="number" step="0.1" v-model="dailyForm.weight" class="form-control" />
          </div>
          <div class="form-group">
            <label>身高(cm)：</label>
            <input type="number" step="0.1" v-model="dailyForm.height" class="form-control" />
          </div>
          <div class="form-group">
            <label>体温(℃)：</label>
            <input type="number" step="0.1" v-model="dailyForm.temperature" class="form-control" />
          </div>
          <div class="form-group">
            <label>饭量(g)：</label>
            <input type="number" v-model="dailyForm.appetite" class="form-control" />
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model="dailyForm.status" class="form-control">
              <option :value="1">正常</option>
              <option :value="2">异常</option>
              <option :value="3">生病</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitDaily">保存</button>
          <button class="btn" @click="closeAddModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface PetDaily {
  id: string;
  name: string;
  weight: number;
  height: number;
  temperature: number;
  appetite: number;
  status: number;
  createTime?: string;
}

interface Pet {
  id: string;
  name: string;
}

const list = ref<PetDaily[]>([]);
const petList = ref<Pet[]>([]);
const loading = ref(false);
const searchName = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showAddModal = ref(false);

const dailyForm = reactive({
  petId: '',
  weight: 0,
  height: 0,
  temperature: 0,
  appetite: 0,
  status: 1
});

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
    const params: Record<string, any> = {
      name: searchName.value,
      page: pagination.page,
      limit: pagination.limit
    };
    // Admin can view all pet daily logs using doctor API
    const resp = await http.get('/user/petDaily/getAllByLimitDoctor', { params });
    const data = resp.data;
    list.value = data.rows || [];
    pagination.total = data.total || 0;
  } catch (e) {
    console.error('获取数据失败:', e);
  } finally {
    loading.value = false;
  }
}

async function fetchPets() {
  try {
    // Admin can see all pets using doctor API
    const resp = await http.get('/user/pet/getAllByLimitDoctor', {
      params: { page: 1, limit: 1000 }
    });
    petList.value = resp.data.rows || [];
  } catch (e) {
    console.error('获取宠物列表失败:', e);
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

function closeAddModal() {
  showAddModal.value = false;
  dailyForm.petId = '';
  dailyForm.weight = 0;
  dailyForm.height = 0;
  dailyForm.temperature = 0;
  dailyForm.appetite = 0;
  dailyForm.status = 1;
}

async function handleDelete(item: PetDaily) {
  const confirmed = await showConfirm('确认删除该日志吗？');
  if (!confirmed) return;
  
  try {
    const resp = await http.post('/user/petDaily/del', { id: item.id });
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
    } else if (resp.data === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage('删除失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

async function submitDaily() {
  if (!dailyForm.petId) {
    showMessage('请选择宠物', 'error');
    return;
  }
  if (!dailyForm.weight || dailyForm.weight <= 0) {
    showMessage('请输入正确的体重（大于0）', 'error');
    return;
  }
  if (!dailyForm.height || dailyForm.height <= 0) {
    showMessage('请输入正确的身高（大于0）', 'error');
    return;
  }
  if (!dailyForm.temperature || dailyForm.temperature <= 0) {
    showMessage('请输入正确的体温（大于0）', 'error');
    return;
  }
  if (!dailyForm.appetite || dailyForm.appetite <= 0) {
    showMessage('请输入正确的饭量（大于0）', 'error');
    return;
  }
  
  try {
    const resp = await http.post('/user/petDaily/doAdd', dailyForm);
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('添加成功', 'success');
      closeAddModal();
      fetchData();
    } else {
      showMessage('添加失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  fetchData();
  fetchPets();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.pet-daily-list-modern {
  padding: 20px;
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
  margin-bottom: 12px;
}

.metric {
  background: #f9fafb;
  border-radius: 10px;
  padding: 12px;
  border: 1px solid #edf2f7;
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

.daily-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  border-top: 2px solid #f0f9f8;
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

.modal-overlay {
  backdrop-filter: blur(4px);
}

.modal {
  border-radius: 14px;
  overflow: hidden;
}

.modal-header {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.form-control {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: #72C1BB;
  box-shadow: 0 0 0 3px rgba(114, 193, 187, 0.1);
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.modal-footer .btn {
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 500;
}

.modal-footer .btn-primary {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  border: none;
}

.modal-footer .btn:not(.btn-primary) {
  background: #f3f4f6;
  color: #374151;
  border: none;
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

  .daily-actions {
    justify-content: center;
  }

  .daily-actions .modern-btn {
    width: 100%;
  }
}
</style>
