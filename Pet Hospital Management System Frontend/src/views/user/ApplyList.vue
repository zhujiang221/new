<template>
  <div class="apply-list-modern">
    <!-- 页面标题 -->
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📅</span>
        我的预约
      </h1>
      <p class="modern-page-subtitle">查看和管理您自己的预约记录（保存30天）</p>
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
      <button class="modern-btn modern-btn-primary" @click="openAddModal">
        <span>➕</span> 新建预约
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="modern-loading">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">📋</div>
      <div class="modern-empty-state-text">暂无预约记录</div>
      <button class="modern-btn modern-btn-primary" @click="openAddModal" style="margin-top: 16px;">
        创建第一个预约
      </button>
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
            <div v-if="item.doctorName" class="apply-detail-item">
              <span class="detail-icon">👨‍⚕️</span>
              <span class="detail-text">医生: {{ item.doctorName }}</span>
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
        
        <div class="apply-card-actions">
          <button 
            v-if="item.status === 1" 
            class="modern-btn modern-btn-danger modern-btn-sm"
            @click="cancelApply(item)"
          >
            取消预约
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

    <!-- Add Apply Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal">
        <div class="modal-header">
          <h3>新建预约</h3>
          <button class="modal-close" @click="closeAddModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>选择宠物：</label>
            <select v-model="applyForm.petId" class="form-control">
              <option value="">请选择宠物</option>
              <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>预约类型：</label>
            <select v-model="applyForm.appointmentTypeId" class="form-control">
              <option value="">请选择预约类型</option>
              <option v-for="t in appointmentTypeList" :key="t.id" :value="t.id">
                {{ t.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>选择医生：</label>
            <select v-model="applyForm.doctorId" class="form-control">
              <option value="">请选择医生</option>
              <option v-for="d in doctorList" :key="d.id" :value="d.id">
                {{ d.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>预约日期：</label>
            <input type="date" v-model="applyForm.appDate" class="form-control" />
          </div>
          <div class="form-group">
            <label>预约时间段：</label>
            <select v-model="applyForm.timeSlot" class="form-control" :disabled="!availableTimeSlots.length">
              <option value="">请选择时间段</option>
              <option 
                v-for="slot in availableTimeSlots" 
                :key="slot.timeSlot" 
                :value="slot.timeSlot"
                :disabled="!slot.canBook"
              >
                {{ slot.timeSlot }} 
                <span v-if="slot.canBook">(可预约)</span>
                <span v-else style="color: red;">(已满)</span>
              </option>
            </select>
            <small v-if="applyForm.doctorId && applyForm.appDate && !availableTimeSlots.length" class="text-muted">
              该医生在该日期暂无可用时间段，请选择其他日期或医生
            </small>
          </div>
          <div class="form-group">
            <label>预约内容：</label>
            <textarea v-model="applyForm.info" class="form-control" rows="3" placeholder="请描述您的预约需求，如看病、疫苗、洗澡等"></textarea>
          </div>
          <div class="form-group">
            <label>联系电话：</label>
            <input type="text" v-model="applyForm.phone" class="form-control" placeholder="请输入联系电话" />
          </div>
          <div class="form-group">
            <label>地址：</label>
            <input type="text" v-model="applyForm.address" class="form-control" placeholder="请输入地址" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitApply">提交</button>
          <button class="btn" @click="closeAddModal">取消</button>
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

interface Apply {
  id: string;
  info: string;
  phone: string;
  address: string;
  appTime: string;
  timeSlot?: string;
  createTime?: string;
  doctorName?: string;
  status: number;
}

interface Pet {
  id: string;
  name: string;
}

interface Doctor {
  id: string;
  name: string;
}

interface AppointmentType {
  id: string;
  name: string;
}

const list = ref<Apply[]>([]);
const petList = ref<Pet[]>([]);
const doctorList = ref<Doctor[]>([]);
const appointmentTypeList = ref<AppointmentType[]>([]);
const loading = ref(false);
const searchInfo = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showAddModal = ref(false);

const applyForm = reactive({
  petId: '',
  appointmentTypeId: '',
  doctorId: '',
  appDate: '',
  timeSlot: '',
  info: '',
  phone: '',
  address: ''
});

// 可用时间段列表（从后端动态加载）
interface TimeSlot {
  timeSlot: string;
  used: number;
  canBook: boolean;
}
const availableTimeSlots = ref<TimeSlot[]>([]);

function formatStatus(status: number) {
  switch (status) {
    case 1: return '申请中';
    case 2: return '申请通过';
    case 3: return '不通过';
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
    const resp = await http.get('/user/apply/getAllByLimit', {
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

async function fetchPets() {
  try {
    const resp = await http.get('/user/pet/getAllByLimit', {
      params: { page: 1, limit: 100 }
    });
    const data = resp.data;
    if (data && data.rows) {
      petList.value = data.rows.map((pet: any) => ({
        id: String(pet.id),
        name: pet.name || '未知'
      }));
    } else if (Array.isArray(data)) {
      petList.value = data.map((pet: any) => ({
        id: String(pet.id),
        name: pet.name || '未知'
      }));
    } else {
      petList.value = [];
    }
  } catch (e) {
    console.error('获取宠物列表失败:', e);
    petList.value = [];
  }
}

async function fetchAppointmentTypes() {
  try {
    const resp = await http.get('/appointmentType/list');
    const data = resp.data;
    if (Array.isArray(data)) {
      appointmentTypeList.value = data.map((t: any) => ({
        id: String(t.id),
        name: t.name || '未知'
      }));
    } else {
      appointmentTypeList.value = [];
    }
  } catch (e) {
    console.error('获取预约类型失败:', e);
    appointmentTypeList.value = [];
  }
}

async function fetchDoctors() {
  try {
    const resp = await http.get('/admin/getAllUserByRoleId', {
      params: {
        // 只获取医生角色的用户：roleId = 2
        roleId: 2,
        page: 1,
        limit: 100
      }
    });
    const data = resp.data;
    let doctors: any[] = [];
    if (Array.isArray(data)) {
      doctors = data;
    } else if (data && data.rows) {
      doctors = data.rows;
    }
    doctorList.value = doctors.map((d: any) => ({
      id: String(d.id || d.userId || ''),
      name: d.name || d.username || d.phone || '未知'
    }));
  } catch (e) {
    console.error('获取医生列表失败:', e);
    doctorList.value = [];
  }
}

// 取消预约（仅限“申请中”的记录）
async function cancelApply(item: Apply) {
  const confirmed = await showConfirm('确认取消该预约吗？');
  if (!confirmed) return;
  try {
    const resp = await http.post('/user/apply/del', { id: item.id });
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('取消预约成功', 'success');
      fetchData();
    } else {
      showMessage('取消预约失败', 'error');
    }
  } catch (e) {
    console.error('取消预约失败:', e);
    showMessage('操作失败', 'error');
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

// 加载可用时间段
async function loadAvailableTimeSlots() {
  if (!applyForm.doctorId || !applyForm.appDate) {
    availableTimeSlots.value = [];
    applyForm.timeSlot = '';
    return;
  }

  try {
    const resp = await http.get('/user/apply/getAvailableSlots', {
      params: {
        doctorId: applyForm.doctorId,
        appDate: applyForm.appDate
      }
    });
    if (Array.isArray(resp.data)) {
      availableTimeSlots.value = resp.data;
      // 如果当前选择的时间段已不可用，清空选择
      if (applyForm.timeSlot) {
        const currentSlot = availableTimeSlots.value.find(s => s.timeSlot === applyForm.timeSlot);
        if (!currentSlot || !currentSlot.canBook) {
          applyForm.timeSlot = '';
        }
      }
    } else {
      availableTimeSlots.value = [];
    }
  } catch (e) {
    console.error('获取可用时间段失败:', e);
    availableTimeSlots.value = [];
  }
}

// 监听医生和日期的变化，自动加载可用时间段
watch([() => applyForm.doctorId, () => applyForm.appDate], () => {
  if (showAddModal.value) {
    loadAvailableTimeSlots();
  }
});

function closeAddModal() {
  showAddModal.value = false;
  applyForm.petId = '';
  applyForm.appointmentTypeId = '';
  applyForm.doctorId = '';
  applyForm.appDate = '';
  applyForm.timeSlot = '';
  applyForm.info = '';
  applyForm.phone = '';
  applyForm.address = '';
  availableTimeSlots.value = [];
}

// 当打开添加预约弹窗时，跳转到新的预约流程页面
function openAddModal() {
  router.push('/user/apply-flow');
}

async function submitApply() {
  if (!applyForm.petId) {
    showMessage('请选择宠物', 'error');
    return;
  }
  if (!applyForm.appointmentTypeId) {
    showMessage('请选择预约类型', 'error');
    return;
  }
  if (!applyForm.doctorId) {
    showMessage('请选择医生', 'error');
    return;
  }
  if (!applyForm.appDate) {
    showMessage('请选择预约日期', 'error');
    return;
  }
  if (!applyForm.timeSlot) {
    showMessage('请选择预约时间段', 'error');
    return;
  }
  if (!applyForm.info) {
    showMessage('请输入预约内容', 'error');
    return;
  }
  if (!applyForm.phone) {
    showMessage('请输入联系电话', 'error');
    return;
  }
  if (!applyForm.address) {
    showMessage('请输入地址', 'error');
    return;
  }
  
  try {
    // 后端接口：/user/apply/doAdd
    const payload = {
      petId: applyForm.petId,
      appointmentTypeId: applyForm.appointmentTypeId,
      doctorId: applyForm.doctorId,
      timeSlot: applyForm.timeSlot,
      // appTime 只需要日期部分，时间段由 timeSlot 表示
      appTime: applyForm.appDate + ' 00:00:00',
      info: applyForm.info,
      phone: applyForm.phone,
      address: applyForm.address
    };
    const resp = await http.post('/user/apply/doAdd', payload);
    const respData = resp.data;
    if (respData === 'SUCCESS' || respData?.status === 'SUCCESS') {
      showMessage('预约成功', 'success');
      closeAddModal();
      fetchData();
    } else if (respData === 'FULL') {
      showMessage('该时间段已约满，请选择其他时间段', 'error');
    } else if (respData === 'noDoctorOrTypeOrSlot') {
      showMessage('请选择医生、预约类型和预约时间段', 'error');
    } else if (respData === 'noPetId') {
      showMessage('请选择宠物', 'error');
    } else {
      showMessage('预约失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  fetchData();
  fetchPets();
  fetchAppointmentTypes();
  fetchDoctors();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.apply-list-modern {
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
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  display: flex;
  justify-content: flex-end;
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

.text-muted {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
    display: block;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .apply-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
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

