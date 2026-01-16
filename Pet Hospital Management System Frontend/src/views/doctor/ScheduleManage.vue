<template>
  <div class="schedule-manage">
    <div class="page-header">
      <h2>排班管理</h2>
    </div>

    <!-- 管理员可以选择医生 -->
    <div class="search-bar-with-actions">
      <div class="search-bar" v-if="isAdmin">
        <label>选择医生：</label>
        <select v-model="selectedDoctorId" @change="loadSchedule">
          <option value="">请选择医生</option>
          <option v-for="doctor in doctorList" :key="doctor.id" :value="doctor.id">{{ doctor.name }}</option>
        </select>
      </div>
      <div class="action-bar">
        <button class="btn btn-primary" @click="openAddModal">添加排班</button>
        <button class="btn btn-primary" @click="openBatchModal">批量设置</button>
      </div>
    </div>

    <!-- 周视图排班展示 -->
    <div class="weekly-board" v-if="!loading && scheduleList.length > 0">
      <div class="week-column" v-for="day in 7" :key="day">
        <div class="week-header">
          <div class="week-title">{{ formatWeekDay(day) }}</div>
          <button class="modern-btn modern-btn-outline modern-btn-sm" @click="openAddForDay(day)">➕ 添加</button>
        </div>
        <div v-if="groupedSchedules[day] && groupedSchedules[day].length" class="slot-list">
          <div class="slot-card" v-for="item in groupedSchedules[day]" :key="item.id">
            <div class="slot-time">
              <span class="time-text">{{ item.timeSlot }}</span>
              <span :class="item.status === 1 ? 'status-badge status-approved' : 'status-badge status-rejected'">
                {{ item.status === 1 ? '启用' : '停用' }}
              </span>
            </div>
            <div class="slot-actions">
              <button class="modern-btn modern-btn-primary modern-btn-sm" @click="editSchedule(item)">编辑</button>
              <button class="modern-btn modern-btn-danger modern-btn-sm" @click="deleteSchedule(item.id)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="slot-empty">暂无排班</div>
      </div>
    </div>
    <div v-else-if="loading" class="modern-loading">加载中...</div>
    <div v-else class="modern-empty-state">暂无排班数据</div>

    <!-- 排班模板区域 -->
    <div class="template-section">
      <div class="template-header">
        <h3>快速应用模板</h3>
        <p>选择模板后一键设置工作时间，将替换现有排班</p>
      </div>
      <div class="template-grid">
        <div 
          v-for="template in scheduleTemplates" 
          :key="template.id"
          class="template-card"
          :class="{ active: selectedTemplate?.id === template.id }"
          @click="selectTemplate(template)"
        >
          <div class="template-name">{{ template.name }}</div>
          <div class="template-desc">{{ template.description }}</div>
          <div class="template-info">
            <span>工作日: {{ formatWorkDays(template.workDays) }}</span>
            <span>时间段数: {{ getTemplateSlotCount(template) }}个</span>
          </div>
        </div>
      </div>
      <div class="template-actions">
        <button 
          class="btn btn-primary" 
          @click="applyTemplate"
          :disabled="!selectedTemplate"
        >
          应用模板（将删除现有排班）
        </button>
      </div>
    </div>

    <!-- 添加/编辑排班弹窗 -->
    <div v-if="showScheduleModal" class="modal-overlay" @click.self="closeScheduleModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingSchedule?.id ? '编辑排班' : '添加排班' }}</h3>
          <button class="modal-close" @click="closeScheduleModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>星期：</label>
            <select v-model="scheduleForm.weekDay" class="form-control">
              <option :value="1">周一</option>
              <option :value="2">周二</option>
              <option :value="3">周三</option>
              <option :value="4">周四</option>
              <option :value="5">周五</option>
              <option :value="6">周六</option>
              <option :value="7">周日</option>
            </select>
          </div>
          <div class="form-group">
            <label>时间段：</label>
            <select v-model="scheduleForm.timeSlot" class="form-control">
              <option value="">请选择时间段</option>
              <option v-for="slot in timeSlotPresets" :key="slot" :value="slot">
                {{ slot }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>最大预约数：</label>
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model.number="scheduleForm.status" class="form-control">
              <option :value="1">启用</option>
              <option :value="0">停用</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitSchedule">保存</button>
          <button class="btn" @click="closeScheduleModal">取消</button>
        </div>
      </div>
    </div>

    <!-- 批量设置弹窗 -->
    <div v-if="showBatchModal" class="modal-overlay" @click.self="showBatchModal = false">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>批量设置排班</h3>
          <button class="modal-close" @click="showBatchModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="batch-schedule-grid">
            <div v-for="weekDay in 7" :key="weekDay" class="week-day-section">
              <h4>{{ formatWeekDay(weekDay) }}</h4>
              <div v-for="(slot, index) in batchSchedules[weekDay]" :key="index" class="slot-item">
                <select 
                  v-model="slot.timeSlot" 
                  class="form-control"
                >
                  <option value="">请选择时间段</option>
                  <option v-for="preset in timeSlotPresets" :key="preset" :value="preset">
                    {{ preset }}
                  </option>
                </select>
                <button class="btn btn-danger btn-sm" @click="removeSlot(weekDay, index)">删除</button>
              </div>
              <button class="btn btn-primary btn-sm" @click="addSlot(weekDay)">添加时间段</button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitBatchSchedule">保存</button>
          <button class="btn" @click="showBatchModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import axios from 'axios';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';
import { getUserInfo, ROLE_ADMIN } from '../../utils/user';

interface DoctorSchedule {
  id?: number;
  doctorId: number;
  weekDay: number;
  timeSlot: string;
  status: number;
}

interface Doctor {
  id: string;
  name: string;
}

const loading = ref(false);
const scheduleList = ref<DoctorSchedule[]>([]);
const doctorList = ref<Doctor[]>([]);
const selectedDoctorId = ref('');
const showScheduleModal = ref(false);
const showBatchModal = ref(false);
const editingSchedule = ref<DoctorSchedule | null>(null);

const userInfo = getUserInfo();
const isAdmin = userInfo?.role === ROLE_ADMIN;

const scheduleForm = reactive<DoctorSchedule>({
  doctorId: 0,
  weekDay: 1,
  timeSlot: '',
  status: 1
});

const batchSchedules = reactive<Record<number, DoctorSchedule[]>>({
  1: [], 2: [], 3: [], 4: [], 5: [], 6: [], 7: []
});

const selectedTemplate = ref<ScheduleTemplate | null>(null);

// 常用时间段预设
const timeSlotPresets = [
  '08:00-09:00',
  '09:00-10:00',
  '10:00-11:00',
  '11:00-12:00',
  '14:00-15:00',
  '15:00-16:00',
  '16:00-17:00',
  '17:00-18:00',
  '18:00-19:00',
  '19:00-20:00'
];

// 排班模板接口
interface ScheduleTemplate {
  id: number;
  name: string;
  description: string;
  workDays: number[]; // 1-7 表示周一到周日
  timeSlots: string[]; // 时间段数组
}

// 排班模板数据（至少10个）
const scheduleTemplates: ScheduleTemplate[] = [
  {
    id: 1,
    name: '标准工作日',
    description: '周一到周五，上午+下午',
    workDays: [1, 2, 3, 4, 5],
    timeSlots: ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'],
  },
  {
    id: 2,
    name: '周末班',
    description: '周六周日全天',
    workDays: [6, 7],
    timeSlots: ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '17:00-18:00'],
  },
  {
    id: 3,
    name: '全天班',
    description: '每天8小时工作时间',
    workDays: [1, 2, 3, 4, 5, 6, 7],
    timeSlots: ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '17:00-18:00'],
  },
  {
    id: 4,
    name: '上午班',
    description: '每天上午工作',
    workDays: [1, 2, 3, 4, 5, 6, 7],
    timeSlots: ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00'],
  },
  {
    id: 5,
    name: '下午班',
    description: '每天下午工作',
    workDays: [1, 2, 3, 4, 5, 6, 7],
    timeSlots: ['14:00-15:00', '15:00-16:00', '16:00-17:00', '17:00-18:00'],
  },
  {
    id: 6,
    name: '夜班',
    description: '每天晚上工作',
    workDays: [1, 2, 3, 4, 5, 6, 7],
    timeSlots: ['18:00-19:00', '19:00-20:00'],
  },
  {
    id: 7,
    name: '弹性工作制',
    description: '周一到周五，灵活时间段',
    workDays: [1, 2, 3, 4, 5],
    timeSlots: ['10:00-11:00', '11:00-12:00', '15:00-16:00', '16:00-17:00'],
  },
  {
    id: 8,
    name: '双休日班',
    description: '仅周六周日，全天',
    workDays: [6, 7],
    timeSlots: ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '17:00-18:00'],
  },
  {
    id: 9,
    name: '工作日早班',
    description: '周一到周五，早上',
    workDays: [1, 2, 3, 4, 5],
    timeSlots: ['08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00'],
  },
  {
    id: 10,
    name: '工作日晚班',
    description: '周一到周五，晚上',
    workDays: [1, 2, 3, 4, 5],
    timeSlots: ['17:00-18:00', '18:00-19:00', '19:00-20:00'],
  },
  {
    id: 11,
    name: '每周三天班',
    description: '周一、周三、周五',
    workDays: [1, 3, 5],
    timeSlots: ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'],
  },
  {
    id: 12,
    name: '每周四天班',
    description: '周一到周四',
    workDays: [1, 2, 3, 4],
    timeSlots: ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'],
  }
];

function formatWeekDay(weekDay: number) {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return days[weekDay] || '';
}

function formatWorkDays(workDays: number[]) {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return workDays.map(d => days[d] || '').join('、');
}

function selectTemplate(template: ScheduleTemplate) {
  selectedTemplate.value = template;
}

function getTemplateSlotCount(template: ScheduleTemplate) {
  return template.workDays.length * template.timeSlots.length;
}

const groupedSchedules = computed<Record<number, DoctorSchedule[]>>(() => {
  const result: Record<number, DoctorSchedule[]> = { 1: [], 2: [], 3: [], 4: [], 5: [], 6: [], 7: [] };
  scheduleList.value.forEach(item => {
    const day = item.weekDay;
    if (!result[day]) result[day] = [];
    result[day].push(item);
  });
  // 按时间段排序（前半部分小时:分钟）
  const parseTime = (timeSlot?: string) => {
    if (!timeSlot) return 0;
    const start = timeSlot.split('-')[0] || '';
    const [h, m] = start.split(':').map(Number);
    return (h || 0) * 60 + (m || 0);
  };
  Object.keys(result).forEach(key => {
    const k = Number(key);
    result[k].sort((a, b) => parseTime(a.timeSlot) - parseTime(b.timeSlot));
  });
  return result;
});

async function applyTemplate() {
  if (!selectedTemplate.value) {
    showMessage('请先选择模板', 'error');
    return;
  }

  if (isAdmin) {
    if (!selectedDoctorId.value) {
      showMessage('请先选择医生', 'error');
      return;
    }
  } else {
    if (!selectedDoctorId.value) {
      selectedDoctorId.value = userInfo?.id || '';
    }
  }
  if (!selectedDoctorId.value) {
    showMessage('请选择医生', 'error');
    return;
  }

  const confirmed = await showConfirm(
    `确认应用模板"${selectedTemplate.value.name}"吗？\n这将删除所有现有排班并应用新模板。`
  );
  if (!confirmed) return;

  loading.value = true;
  try {
    const doctorId = Number(selectedDoctorId.value);
    
    // 根据模板生成新的排班数据
    const newSchedules: DoctorSchedule[] = [];
    const template = selectedTemplate.value;
    
    for (const weekDay of template.workDays) {
      for (const timeSlot of template.timeSlots) {
        newSchedules.push({
          doctorId: doctorId,
          weekDay: weekDay,
          timeSlot: timeSlot,
          status: 1
        });
      }
    }

    // 使用新的 applyTemplate 接口，后端会自动删除旧排班并添加新排班
    if (newSchedules.length > 0) {
      // 使用 http 实例发送 JSON 数据，确保自动添加 Authorization 头
      try {
        const resp = await http.post(`/doctor/schedule/applyTemplate?doctorId=${doctorId}`, newSchedules, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        if (resp.data === 'SUCCESS') {
          showMessage(`模板"${template.name}"应用成功`, 'success');
          selectedTemplate.value = null;
          loadSchedule();
        } else {
          const errorMsg = resp.data === 'EMPTY' ? '排班数据为空' :
                          resp.data === 'noDoctorId' ? '医生ID不能为空' :
                          resp.data === 'NOT_LOGIN' ? '用户未登录，请重新登录' :
                          resp.data === 'ERROR' ? '应用模板失败，请稍后重试' :
                          resp.data || '应用模板失败';
          showMessage(errorMsg, 'error');
        }
      } catch (axiosError: any) {
        console.error('应用模板失败:', axiosError);
        if (axiosError.response) {
          const errorMsg = axiosError.response.data === 'EMPTY' ? '排班数据为空' :
                          axiosError.response.data === 'noDoctorId' ? '医生ID不能为空' :
                          axiosError.response.data === 'NOT_LOGIN' ? '用户未登录，请重新登录' :
                          axiosError.response.data === 'ERROR' ? '应用模板失败，请稍后重试' :
                          axiosError.response.data || `请求失败: ${axiosError.response.status}`;
          showMessage(errorMsg, 'error');
        } else if (axiosError.request) {
          showMessage('网络请求失败，请检查网络连接', 'error');
        } else {
          showMessage('应用模板失败: ' + axiosError.message, 'error');
        }
      }
    } else {
      showMessage('模板数据为空', 'error');
    }
  } catch (e: any) {
    console.error('应用模板失败:', e);
    showMessage('应用模板失败: ' + (e.message || '未知错误'), 'error');
  } finally {
    loading.value = false;
  }
}

async function loadSchedule() {
  if (!selectedDoctorId.value && !isAdmin) {
    // 医生查看自己的排班
    selectedDoctorId.value = userInfo?.id || '';
  }
  if (!selectedDoctorId.value) return;

  loading.value = true;
  try {
    const resp = await http.get('/doctor/schedule/list', {
      params: { 
        doctorId: selectedDoctorId.value
      }
    });
    
    // 响应为数组
    if (Array.isArray(resp.data)) {
      scheduleList.value = resp.data;
    } else if (resp.data && Array.isArray((resp.data as any).rows)) {
      // 兼容可能的分页格式
      scheduleList.value = (resp.data as any).rows;
    } else {
      scheduleList.value = [];
    }
  } catch (e) {
    console.error('加载排班失败:', e);
    showMessage('加载排班失败', 'error');
    scheduleList.value = [];
  } finally {
    loading.value = false;
  }
}

async function fetchDoctors() {
  if (!isAdmin) return;
  try {
    const resp = await http.get('/admin/getAllUserByRoleId', {
      params: { roleId: 2, page: 1, limit: 100 } // roleId=2 表示医生
    });
    const data = resp.data;
    if (Array.isArray(data)) {
      doctorList.value = data.map((d: any) => ({ id: String(d.id), name: d.name || d.username }));
    } else if (data?.rows) {
      doctorList.value = data.rows.map((d: any) => ({ id: String(d.id), name: d.name || d.username }));
    }
    if (doctorList.value.length > 0 && !selectedDoctorId.value) {
      selectedDoctorId.value = doctorList.value[0].id;
    }
  } catch (e) {
    console.error('获取医生列表失败:', e);
  }
}

function openAddModal() {
  if (isAdmin) {
    // 管理员必须选择医生
    if (!selectedDoctorId.value) {
      showMessage('请先选择医生', 'error');
      return;
    }
  } else {
    // 医生使用自己的ID
    if (!selectedDoctorId.value) {
      selectedDoctorId.value = userInfo?.id || '';
    }
  }
  if (!selectedDoctorId.value) {
    showMessage('请选择医生', 'error');
    return;
  }
  editingSchedule.value = null;
  scheduleForm.doctorId = Number(selectedDoctorId.value);
  scheduleForm.weekDay = 1;
  scheduleForm.timeSlot = '';
  scheduleForm.status = 1;
  showScheduleModal.value = true;
}

function editSchedule(item: DoctorSchedule) {
  editingSchedule.value = item;
  scheduleForm.doctorId = item.doctorId;
  scheduleForm.weekDay = item.weekDay;
  scheduleForm.timeSlot = item.timeSlot;
  scheduleForm.status = item.status;
  showScheduleModal.value = true;
}

function closeScheduleModal() {
  showScheduleModal.value = false;
  editingSchedule.value = null;
}

function openAddForDay(day: number) {
  if (!selectedDoctorId.value) {
    showMessage('请先选择医生', 'error');
    return;
  }
  editingSchedule.value = null;
  scheduleForm.doctorId = Number(selectedDoctorId.value);
  scheduleForm.weekDay = day;
  scheduleForm.timeSlot = '';
  scheduleForm.status = 1;
  showScheduleModal.value = true;
}

async function submitSchedule() {
  if (!scheduleForm.timeSlot) {
    showMessage('请输入时间段', 'error');
    return;
  }

  try {
    const url = editingSchedule.value?.id ? '/doctor/schedule/update' : '/doctor/schedule/add';
    const data = editingSchedule.value?.id
      ? { ...scheduleForm, id: editingSchedule.value.id }
      : scheduleForm;
    
    const resp = await http.post(url, data);
    if (resp.data === 'SUCCESS') {
      showMessage(editingSchedule.value?.id ? '更新成功' : '添加成功', 'success');
      closeScheduleModal();
      loadSchedule();
    } else {
      showMessage(resp.data || '操作失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

async function deleteSchedule(id: number) {
  const confirmed = await showConfirm('确认删除该排班吗？');
  if (!confirmed) return;

  try {
    const resp = await http.post('/doctor/schedule/delete', { id });
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      loadSchedule();
    } else {
      showMessage('删除失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

function openBatchModal() {
  if (isAdmin) {
    // 管理员必须选择医生
    if (!selectedDoctorId.value) {
      showMessage('请先选择医生', 'error');
      return;
    }
  } else {
    // 医生使用自己的ID
    if (!selectedDoctorId.value) {
      selectedDoctorId.value = userInfo?.id || '';
    }
  }
  if (!selectedDoctorId.value) {
    showMessage('请选择医生', 'error');
    return;
  }
  // 初始化批量设置数据
  for (let i = 1; i <= 7; i++) {
    batchSchedules[i] = scheduleList.value
      .filter(s => s.weekDay === i)
      .map(s => ({ ...s }));
  }
  showBatchModal.value = true;
}

function addSlot(weekDay: number) {
  batchSchedules[weekDay].push({
    doctorId: Number(selectedDoctorId.value),
    weekDay: weekDay,
    timeSlot: '',
    status: 1
  });
}

function removeSlot(weekDay: number, index: number) {
  batchSchedules[weekDay].splice(index, 1);
}

async function submitBatchSchedule() {
  const allSchedules: DoctorSchedule[] = [];
  for (let i = 1; i <= 7; i++) {
    for (const slot of batchSchedules[i]) {
      if (slot.timeSlot) {
        allSchedules.push({
          ...slot,
          doctorId: Number(selectedDoctorId.value)
        });
      }
    }
  }

  if (allSchedules.length === 0) {
    showMessage('请至少添加一个有效的排班', 'error');
    return;
  }

  try {
    const resp = await http.post('/doctor/schedule/batchSave', allSchedules);
    if (resp.data === 'SUCCESS') {
      showMessage('批量保存成功', 'success');
      showBatchModal.value = false;
      loadSchedule();
    } else {
      showMessage(resp.data || '操作失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  if (isAdmin) {
    fetchDoctors().then(() => {
      if (selectedDoctorId.value) {
        loadSchedule();
      }
    });
  } else {
    selectedDoctorId.value = userInfo?.id || '';
    loadSchedule();
  }
});
</script>

<style scoped>
.schedule-manage {
  padding: 0;
}

.search-bar-with-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-bar-with-actions .search-bar {
  flex: 1;
  min-width: 200px;
  margin: 0;
}

.search-bar-with-actions .action-bar {
  display: flex;
  gap: 10px;
  margin: 0;
}

.schedule-table-container {
  margin: 20px 0;
}

.weekly-board {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
  margin: 20px 0;
}

.week-column {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

.week-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.week-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.slot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.slot-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  background: #fafbfc;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.slot-time {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #111827;
}

.time-text {
  font-size: 15px;
}

.slot-meta {
  color: #6b7280;
  font-size: 13px;
}

.slot-actions {
  display: flex;
  gap: 8px;
}

.slot-empty {
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
  padding: 20px 0;
  border: 1px dashed #e5e7eb;
  border-radius: 10px;
  background: #fafbfc;
}

.modal-large {
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.batch-schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.week-day-section {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 8px;
  background: #f9f9f9;
}

.week-day-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.slot-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.slot-item .form-control {
  flex: 1;
}

.template-section {
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.template-header {
  margin-bottom: 20px;
}

.template-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
}

.template-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.template-card {
  padding: 15px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.template-card.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.template-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.template-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.template-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.template-actions {
  text-align: center;
}

.template-actions .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 767px) {
  .batch-schedule-grid {
    grid-template-columns: 1fr;
  }
  
  .slot-item {
    flex-direction: column;
  }
  
  .slot-item .form-control {
    width: 100%;
  }
  
  .template-grid {
    grid-template-columns: 1fr;
  }
}
</style>
