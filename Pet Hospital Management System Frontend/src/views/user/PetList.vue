<template>
  <div class="pet-list-modern">
    <!-- 页面标题 -->
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>🐾</span>
        我的宠物
      </h1>
      <p class="modern-page-subtitle">管理您的宠物信息，预约就诊服务</p>
    </div>

    <!-- 搜索栏 -->
    <div class="modern-search-bar">
      <input 
        type="text" 
        v-model="searchName" 
        placeholder="🔍 搜索宠物名称..." 
        class="modern-input"
        @keyup.enter="search" 
      />
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
      <button class="modern-btn modern-btn-primary" @click="showAddModal = true">
        <span>➕</span> 添加宠物
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="modern-loading">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">🐱</div>
      <div class="modern-empty-state-text">暂无宠物信息</div>
      <button class="modern-btn modern-btn-primary" @click="showAddModal = true" style="margin-top: 16px;">
        添加第一个宠物
      </button>
    </div>

    <!-- 宠物卡片网格 -->
    <div v-else class="modern-grid">
      <div v-for="item in list" :key="item.id" class="modern-card pet-card">
        <div class="pet-card-header">
          <div class="pet-avatar">
            {{ item.type === '1' ? '🐱' : '🐶' }}
          </div>
          <div class="pet-info">
            <h3 class="pet-name">{{ item.name }}</h3>
            <span class="pet-type-badge" :class="item.type === '1' ? 'type-cat' : 'type-dog'">
              {{ formatType(item.type) }}
            </span>
          </div>
        </div>
        
        <div class="pet-card-body">
          <div class="pet-detail-item">
            <span class="detail-label">📏 身高</span>
            <span class="detail-value">{{ item.height }} cm</span>
          </div>
          <div class="pet-detail-item">
            <span class="detail-label">⚖️ 体重</span>
            <span class="detail-value">{{ item.weight }} kg</span>
          </div>
          <div class="pet-detail-item">
            <span class="detail-label">🎂 年龄</span>
            <span class="detail-value">{{ formatAge(item.birthday) }}</span>
          </div>
          <div class="pet-detail-item">
            <span class="detail-label">⚧️ 性别</span>
            <span class="detail-value">{{ formatSex(item.sex) }}</span>
          </div>
            </div>
        
        <div class="pet-card-actions">
          <button class="modern-btn modern-btn-primary modern-btn-sm" @click="handleApply(item)">
            📅 预约就诊
          </button>
          <button class="modern-btn modern-btn-outline modern-btn-sm" @click="handleViewHistory(item)">
            📋 查看病例
          </button>
          <button class="modern-btn modern-btn-danger modern-btn-sm" @click="handleDelete(item)">
            🗑️ 删除
          </button>
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

    <!-- Add Pet Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>添加宠物</h3>
          <button class="modal-close" @click="showAddModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>名称：</label>
            <input type="text" v-model="petForm.name" class="form-control" />
          </div>
          <div class="form-group">
            <label>体重(Kg)：</label>
            <input type="number" v-model="petForm.weight" class="form-control" />
          </div>
          <div class="form-group">
            <label>身高(cm)：</label>
            <input type="number" v-model="petForm.height" class="form-control" />
          </div>
          <div class="form-group">
            <label>出生日期：</label>
            <input type="date" v-model="petForm.birthday" class="form-control" />
          </div>
          <div class="form-group">
            <label>类型：</label>
            <select v-model="petForm.type" class="form-control">
              <option value="1">猫科</option>
              <option value="2">犬科</option>
              <option value="3">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>性别：</label>
            <select v-model="petForm.sex" class="form-control">
              <option value="1">公</option>
              <option value="2">母</option>
              <option value="3">未知</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitPet">保存</button>
          <button class="btn" @click="showAddModal = false">取消</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

const router = useRouter();

interface Pet {
  id: string;
  name: string;
  weight: number;
  height: number;
  birthday: string;
  type: string;
  sex?: string;
}


const list = ref<Pet[]>([]);
const loading = ref(false);
const searchName = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showAddModal = ref(false);

const petForm = reactive({
  name: '',
  weight: 0,
  height: 0,
  birthday: '',
  type: '1',
  sex: '3'
});


function formatType(type: string) {
  if (type === '1') return '猫科';
  if (type === '2') return '犬科';
  if (type === '3') return '其他';
  return type;
}

function formatSex(sex: string) {
  if (sex === '1') return '公';
  if (sex === '2') return '母';
  if (sex === '3') return '未知';
  return sex || '未知';
}

function formatAge(birthday: string) {
  if (!birthday) return '-';
  const birth = new Date(birthday);
  if (isNaN(birth.getTime())) return '-';

  const now = new Date();
  let years = now.getFullYear() - birth.getFullYear();
  let months = now.getMonth() - birth.getMonth();

  if (now.getDate() < birth.getDate()) {
    months -= 1;
  }

  if (months < 0) {
    years -= 1;
    months += 12;
  }

  // 刚出生不显示 0 岁，按月显示
  if (years <= 0) {
    const totalMonths =
      (now.getFullYear() - birth.getFullYear()) * 12 +
      (now.getMonth() - birth.getMonth());
    const safeMonths = totalMonths < 0 ? 0 : totalMonths || 1;
    return `${safeMonths}个月`;
  }

  if (months === 0) {
    return `${years}岁`;
  }

  return `${years}岁${months}个月`;
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/user/pet/getAllByLimit', {
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

async function handleDelete(item: Pet) {
  const confirmed = await showConfirm('确认删除该宠物吗？');
  if (!confirmed) return;
  
  try {
    const resp = await http.post('/user/pet/del', { id: item.id });
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
    } else {
      showMessage('删除失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}


function handleApply(item: Pet) {
  // 跳转到新的预约流程页面，传递宠物ID
  router.push({
    path: '/user/apply-flow',
    query: { petId: item.id }
  });
}

function handleViewHistory(item: Pet) {
  router.push(`/user/diagnosis?petId=${item.id}`);
}

async function submitPet() {
  // 简单的前端校验，防止空信息被提交
  if (!petForm.name || !petForm.name.trim()) {
    showMessage('请输入宠物名称', 'error');
    return;
  }
  if (!petForm.weight || petForm.weight <= 0) {
    showMessage('请输入正确的体重（大于0）', 'error');
    return;
  }
  if (!petForm.height || petForm.height <= 0) {
    showMessage('请输入正确的身高（大于0）', 'error');
    return;
  }
  if (!petForm.birthday) {
    showMessage('请选择宠物出生日期', 'error');
    return;
  }
  
  try {
    // 后端接口：/user/pet/doAdd
    // 参考原系统实现，直接发送对象，让axios拦截器自动转换为表单格式
    const param = {
      name: petForm.name.trim(),
      weight: petForm.weight,
      height: petForm.height,
      // 后端 Pet.birthday 使用 @DateTimeFormat(\"yyyy-MM-dd HH:mm:ss\")
      // 这里统一将日期补全为 \"yyyy-MM-dd 00:00:00\"，保证前后端日期格式一致
      birthday: petForm.birthday ? `${petForm.birthday} 00:00:00` : '',
      type: petForm.type || '1',
      sex: petForm.sex || '3'
    };
    
    const resp = await http.post('/user/pet/doAdd', param);
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('添加成功', 'success');
      showAddModal.value = false;
      petForm.name = '';
      petForm.weight = 0;
      petForm.height = 0;
      petForm.birthday = '';
      petForm.type = '1';
      petForm.sex = '3';
      fetchData();
    } else {
      showMessage('添加失败: ' + (resp.data?.message || resp.data || ''), 'error');
    }
  } catch (e: any) {
    console.error('添加宠物失败:', e);
    showMessage('操作失败: ' + (e.response?.data?.message || e.message || '未知错误'), 'error');
  }
}


onMounted(() => {
  fetchData();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.pet-list-modern {
  padding: 0;
}

/* 宠物卡片样式 */
.pet-card {
  position: relative;
  overflow: hidden;
}

.pet-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
  }
  
.pet-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f9f8;
}

.pet-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.2);
  }
  
.pet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pet-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.pet-type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-cat {
  background: #fef3c7;
  color: #92400e;
}

.type-dog {
  background: #dbeafe;
  color: #1e40af;
}

.pet-card-body {
  margin-bottom: 16px;
  }
  
.pet-detail-item {
    display: flex;
    justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.pet-detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #6b7280;
  font-size: 14px;
}

.detail-value {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
}

.pet-card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 2px solid #f0f9f8;
}

.pet-card-actions .modern-btn {
  flex: 1;
  min-width: 100px;
}

/* 模态框样式优化 */
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
  min-height: 80px;
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
  .pet-card-actions {
    flex-direction: column;
  }
  
  .pet-card-actions .modern-btn {
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

