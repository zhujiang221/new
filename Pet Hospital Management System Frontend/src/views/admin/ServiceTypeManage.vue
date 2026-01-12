<template>
  <div class="service-type-manage">
    <div class="page-header">
      <h2>服务类型管理</h2>
    </div>

    <!-- 管理员可以选择医生 -->
    <div class="search-bar">
      <label>选择医生：</label>
      <select v-model="selectedDoctorId" @change="loadServiceTypes">
        <option value="">请选择医生</option>
        <option v-for="doctor in doctorList" :key="doctor.id" :value="doctor.id">{{ doctor.name }}</option>
      </select>
    </div>

    <!-- 当前医生的服务类型列表 -->
    <div class="service-types-container">
      <h3>当前可提供的服务类型</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="currentServiceTypes.length === 0" class="empty-state">暂无服务类型</div>
      <div v-else class="service-type-list">
        <div v-for="type in currentServiceTypes" :key="type.id" class="service-type-item">
          <span>{{ getTypeName(type.typeId) }}</span>
        </div>
      </div>
    </div>

    <!-- 设置服务类型 -->
    <div class="action-section">
      <h3>设置可提供的服务类型</h3>
      <div class="type-checkbox-list">
        <div v-if="allAppointmentTypes.length === 0" class="empty-state">暂无预约类型数据</div>
        <label v-for="type in allAppointmentTypes" :key="type.id" class="checkbox-item">
          <input
            type="checkbox"
            :value="type.id"
            v-model="selectedTypeIds"
          />
          <span>{{ type.name }}</span>
          <span class="type-desc" v-if="type.description">（{{ type.description }}）</span>
        </label>
      </div>
      <div class="action-bar">
        <button class="btn btn-primary" @click="saveServiceTypes">保存设置</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage } from '../../utils/message';

interface AppointmentType {
  id: number;
  name: string;
  description?: string;
  status: number;
}

interface DoctorServiceType {
  id: number;
  doctorId: number;
  typeId: number;
}

interface Doctor {
  id: string;
  name: string;
}

const loading = ref(false);
const allAppointmentTypes = ref<AppointmentType[]>([]);
const currentServiceTypes = ref<DoctorServiceType[]>([]);
const selectedTypeIds = ref<number[]>([]);
const doctorList = ref<Doctor[]>([]);
const selectedDoctorId = ref('');

function getTypeName(typeId: number) {
  const type = allAppointmentTypes.value.find(t => t.id === typeId);
  return type ? type.name : '未知类型';
}

async function loadAppointmentTypes() {
  try {
    const resp = await http.get('/appointmentType/list');
    if (resp && resp.data) {
      allAppointmentTypes.value = Array.isArray(resp.data) ? resp.data : [];
      console.log('加载预约类型成功:', allAppointmentTypes.value.length, '个类型');
    } else {
      allAppointmentTypes.value = [];
      console.warn('预约类型数据为空');
    }
  } catch (e: any) {
    console.error('加载预约类型失败:', e);
    showMessage('加载预约类型失败: ' + (e.message || '未知错误'), 'error');
    allAppointmentTypes.value = [];
  }
}

async function loadServiceTypes() {
  if (!selectedDoctorId.value) {
    currentServiceTypes.value = [];
    selectedTypeIds.value = [];
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    const resp = await http.get('/doctor/serviceType/list', {
      params: { doctorId: selectedDoctorId.value }
    });
    if (resp && resp.data) {
      currentServiceTypes.value = Array.isArray(resp.data) ? resp.data : [];
      selectedTypeIds.value = currentServiceTypes.value.map((st: any) => st.typeId);
    } else {
      currentServiceTypes.value = [];
      selectedTypeIds.value = [];
    }
  } catch (e: any) {
    console.error('加载服务类型失败:', e);
    showMessage('加载服务类型失败: ' + (e.message || '未知错误'), 'error');
    currentServiceTypes.value = [];
    selectedTypeIds.value = [];
  } finally {
    loading.value = false;
  }
}

async function fetchDoctors() {
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

async function saveServiceTypes() {
  if (!selectedDoctorId.value) {
    showMessage('请先选择医生', 'error');
    return;
  }

  try {
    const resp = await http.post('/doctor/serviceType/set', {
      doctorId: selectedDoctorId.value,
      typeIds: selectedTypeIds.value
    });
    if (resp.data === 'SUCCESS') {
      showMessage('保存成功', 'success');
      loadServiceTypes();
    } else {
      showMessage(resp.data || '保存失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  loadAppointmentTypes();
  fetchDoctors().then(() => {
    if (selectedDoctorId.value) {
      loadServiceTypes();
    }
  });
});
</script>

<style scoped>

.service-type-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar label {
  font-weight: 500;
  color: #555;
}

.search-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.service-types-container {
  margin: 20px 0;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.service-types-container h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.service-type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 15px;
}

.service-type-item {
  padding: 8px 15px;
  background: #e3f2fd;
  border-radius: 4px;
  color: #1976d2;
}

.action-section {
  margin: 20px 0;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.type-checkbox-list {
  margin: 15px 0;
}

.checkbox-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin: 5px 0;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.checkbox-item:hover {
  background: #f5f5f5;
}

.checkbox-item input[type="checkbox"] {
  margin-right: 10px;
  cursor: pointer;
}

.type-desc {
  color: #666;
  font-size: 0.9em;
  margin-left: 5px;
}

.action-bar {
  margin-top: 20px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #72C1BB;
  color: white;
}

.btn-primary:hover {
  background: #5aa9a3;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
