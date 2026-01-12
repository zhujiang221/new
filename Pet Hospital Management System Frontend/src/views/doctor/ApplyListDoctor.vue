<template>
  <div class="apply-list-doctor-modern">
    <!-- 页面标题 -->
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>👨‍⚕️</span>
        预约管理
      </h1>
      <p class="modern-page-subtitle">管理患者预约，处理就诊申请</p>
    </div>

    <!-- 搜索栏 -->
    <div class="modern-search-bar">
      <input 
        type="text" 
        v-model="searchInfo" 
        placeholder="🔍 搜索预约内容..." 
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
      <div class="modern-empty-state-text">暂无预约记录</div>
    </div>

    <!-- 预约卡片列表 -->
    <div v-else class="apply-cards">
      <div v-for="item in list" :key="item.id" class="modern-card apply-card">
        <div class="apply-card-header">
          <div class="apply-status">
            <span :class="getStatusClass(item.status) + ' modern-badge'">{{ formatStatus(item.status) }}</span>
          </div>
          <div class="apply-time">
            <span class="time-icon">🕐</span>
            <div class="time-info">
              <div class="appointment-time">
                预约时间: {{ formatDateTime(item.appTime) }}{{ item.timeSlot ? ' ' + item.timeSlot : '' }}
              </div>
              <div v-if="item.createTime" class="create-time">
                创建时间: {{ formatDateTime(item.createTime) }}
              </div>
            </div>
          </div>
        </div>
        
        <div class="apply-card-body">
          <div class="apply-content">
            <h3 class="apply-info-title">预约内容</h3>
            <p class="apply-info-text">{{ item.info }}</p>
          </div>
          
          <div class="apply-details">
            <div class="apply-detail-item" v-if="item.userName">
              <span class="detail-icon">👤</span>
              <span class="detail-text">用户姓名: {{ item.userName }}</span>
            </div>
            <div class="apply-detail-item" v-if="item.petName">
              <span class="detail-icon">🐾</span>
              <span class="detail-text">宠物: {{ item.petName }}</span>
            </div>
            <div class="apply-detail-item">
              <span class="detail-icon">📞</span>
              <span class="detail-text">{{ item.phone }}</span>
            </div>
            <div class="apply-detail-item">
              <span class="detail-icon">📍</span>
              <span class="detail-text">{{ item.address }}</span>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="apply-card-actions" v-if="item.status === 1">
          <button class="modern-btn modern-btn-success modern-btn-sm" @click="changeStatus(item, 2)">
            ✓ 到场就诊
          </button>
          <button class="modern-btn modern-btn-danger modern-btn-sm" @click="changeStatus(item, 3)">
            ✗ 未到场就诊
          </button>
        </div>
        <div class="apply-card-actions" v-else-if="item.status === 2">
          <button class="modern-btn modern-btn-primary modern-btn-sm" @click="handleDiagnosis(item)">
            🏥 开始就诊
          </button>
        </div>
        <div class="apply-card-actions" v-else>
          <span class="modern-badge modern-badge-info">已处理</span>
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

    <!-- Diagnosis Modal -->
    <div v-if="showDiagnosisModal" class="modal-overlay" @click.self="showDiagnosisModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>添加诊断记录</h3>
          <button class="modal-close" @click="showDiagnosisModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>就诊信息：</label>
            <textarea v-model="diagnosisForm.info" class="form-control" rows="4" placeholder="请输入就诊信息"></textarea>
          </div>
          <div class="form-group">
            <label>类型：</label>
            <select v-model="diagnosisForm.type" class="form-control">
              <option :value="1">正常就医</option>
              <option :value="2">注射疫苗</option>
              <option :value="3">驱虫</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model="diagnosisForm.status" class="form-control">
              <option :value="1">正常</option>
              <option :value="2">严重</option>
              <option :value="3">较重</option>
              <option :value="4">死亡</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitDiagnosis">保存</button>
          <button class="btn" @click="showDiagnosisModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface Apply {
  id: string;
  info: string;
  phone: string;
  address: string;
  appTime: string;
  timeSlot?: string;
  createTime?: string;
  status: number;
  userId?: string;
  petId?: string;
  userName?: string;
  petName?: string;
}

const list = ref<Apply[]>([]);
const loading = ref(false);
const searchInfo = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showDiagnosisModal = ref(false);
const currentApply = ref<Apply | null>(null);

const diagnosisForm = reactive({
  info: '',
  type: 1,
  status: 1
});

function formatStatus(status: number) {
  switch (status) {
    case 1: return '申请中';
    case 2: return '已到场就诊';
    case 3: return '未到场就诊';
    case 4: return '已完成';
    case 5: return '已取消';
    default: return '未知';
  }
}

function getStatusClass(status: number) {
  switch (status) {
    case 1: return 'status-badge status-pending';
    case 2: return 'status-badge status-approved';
    case 3: return 'status-badge status-rejected';
    case 4: return 'status-badge status-completed';
    case 5: return 'status-badge status-cancelled';
    default: return 'status-badge';
  }
}

// 格式化日期时间
function formatDateTime(dateStr: string) {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    // 如果是创建时间，显示完整时间；如果是预约时间，只显示日期和时间段
    if (dateStr.includes('00:00:00') || dateStr.split(' ').length === 1) {
      return `${year}-${month}-${day}`;
    }
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (e) {
    return dateStr;
  }
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/user/apply/getAllByLimitDoctor', {
      params: {
        info: searchInfo.value,
        page: pagination.page,
        limit: pagination.limit
      }
    });
    const data = resp.data;
    const rows = (data.rows || []) as Apply[];
    // 按申请时间从新到旧排序（最新的在前面）
    rows.sort((a, b) => {
      const ta = new Date(a.appTime || 0).getTime();
      const tb = new Date(b.appTime || 0).getTime();
      return tb - ta;
    });
    list.value = rows;
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

async function changeStatus(item: Apply, status: number) {
  let msg = '';
  if (status === 2) {
    msg = '确认患者已到场就诊？';
  } else if (status === 3) {
    msg = '确认患者未到场就诊？';
  } else if (status === 4) {
    msg = '确认前去就诊？';
  } else {
    msg = '确认保存吗？';
  }
  const confirmed = await showConfirm(msg);
  if (!confirmed) return;
  
  try {
    const resp = await http.post('/user/apply/chStatus', {
      id: item.id,
      status: status
    });
    
    if (resp.data === 'SUCCESS') {
      showMessage('操作成功', 'success');
      fetchData();
    } else if (resp.data === 'jz') {
      // Open diagnosis modal
      handleDiagnosis(item);
    } else {
      showMessage('操作失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

function handleDiagnosis(item: Apply) {
  currentApply.value = item;
  diagnosisForm.info = '';
  diagnosisForm.type = 1;
  diagnosisForm.status = 1;
  
  // First change status to completed
  changeStatusToDiagnosis(item);
}

async function changeStatusToDiagnosis(item: Apply) {
  try {
    const resp = await http.post('/user/apply/chStatus', {
      id: item.id,
      status: 4
    });
    
    if (resp.data === 'jz' || resp.data === 'SUCCESS') {
      showDiagnosisModal.value = true;
    }
  } catch (e) {
    // Still show the modal
    showDiagnosisModal.value = true;
  }
}

async function submitDiagnosis() {
  if (!diagnosisForm.info) {
    showMessage('请输入就诊信息', 'error');
    return;
  }
  if (!currentApply.value) return;
  
  try {
    const resp = await http.post('/user/diagnosis/doAdd', {
      userId: currentApply.value.userId,
      petId: currentApply.value.petId,
      info: diagnosisForm.info,
      type: diagnosisForm.type,
      status: diagnosisForm.status
    });
    
    if (resp.data === 'SUCCESS') {
      showMessage('保存成功', 'success');
      showDiagnosisModal.value = false;
      fetchData();
    } else if (resp.data === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage('保存失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.apply-list-doctor-modern {
  padding: 0;
}

.apply-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.apply-card {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.apply-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
}

.apply-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f9f8;
}

.apply-status {
  display: flex;
  align-items: center;
}

.apply-time {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.time-icon {
  font-size: 16px;
  margin-top: 2px;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.appointment-time {
  font-weight: 500;
  color: #1f2937;
}

.create-time {
  font-size: 12px;
  color: #9ca3af;
}

.apply-card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.apply-content {
  flex: 1;
}

.apply-info-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.apply-info-text {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
  font-size: 14px;
}

.apply-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.apply-detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 14px;
}

.detail-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.detail-text {
  flex: 1;
  word-break: break-all;
}

.apply-card-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 2px solid #f0f9f8;
  flex-wrap: wrap;
}

.apply-card-actions .modern-btn {
  flex: 1;
  min-width: 120px;
}

/* 状态标签样式 */
.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.status-completed {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

/* 模态框样式 */
.modal-overlay {
  backdrop-filter: blur(4px);
}

.modal {
  border-radius: 16px;
  overflow: hidden;
}

.modal-header {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
}

.modal-body .form-group {
  margin-bottom: 20px;
}

.modal-body .form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
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

textarea.form-control {
  resize: vertical;
  min-height: 100px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal-footer .btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-footer .btn-primary {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

.modal-footer .btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(114, 193, 187, 0.3);
}

.modal-footer .btn:not(.btn-primary) {
  background: #f3f4f6;
  color: #374151;
}

.modal-footer .btn:not(.btn-primary):hover {
  background: #e5e7eb;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .apply-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .apply-card-actions {
    flex-direction: column;
  }
  
  .apply-card-actions .modern-btn {
    width: 100%;
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

