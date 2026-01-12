<template>
  <div class="appointment-type-manage">
    <div class="page-header">
      <h2>预约类型管理</h2>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openAddModal">添加类型</button>
    </div>

    <!-- 数据表格 -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>类型名称</th>
          <th>描述</th>
          <th>状态</th>
          <th width="200">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="5" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="5" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.description || '-' }}</td>
          <td>
            <span :class="item.status === 1 ? 'status-badge status-approved' : 'status-badge status-rejected'">
              {{ item.status === 1 ? '启用' : '禁用' }}
            </span>
          </td>
          <td>
            <button class="btn btn-primary btn-sm" @click="editType(item)">编辑</button>
            <button class="btn btn-danger btn-sm" @click="deleteType(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingType?.id ? '编辑预约类型' : '添加预约类型' }}</h3>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>类型名称：</label>
            <input type="text" v-model="typeForm.name" class="form-control" placeholder="如：看病、疫苗注射、洗澡、修毛" />
          </div>
          <div class="form-group">
            <label>描述：</label>
            <textarea v-model="typeForm.description" class="form-control" rows="3" placeholder="可选，描述该预约类型的详细信息"></textarea>
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model.number="typeForm.status" class="form-control">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitType">保存</button>
          <button class="btn" @click="closeModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface AppointmentType {
  id?: number;
  name: string;
  description?: string;
  status: number;
}

const loading = ref(false);
const list = ref<AppointmentType[]>([]);
const showModal = ref(false);
const editingType = ref<AppointmentType | null>(null);

const typeForm = reactive<AppointmentType>({
  name: '',
  description: '',
  status: 1
});

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/appointmentType/listAll');
    console.log('获取预约类型列表响应:', resp);
    
    if (Array.isArray(resp.data)) {
      list.value = resp.data;
      console.log('成功加载预约类型列表，共', resp.data.length, '条');
    } else if (resp.data === 'NO_PERMISSION' || (typeof resp.data === 'string' && resp.data.includes('NO_PERMISSION'))) {
      showMessage('无权限访问', 'error');
      list.value = [];
    } else if (typeof resp.data === 'string' && resp.data.includes('ERROR')) {
      showMessage('获取数据失败: ' + resp.data, 'error');
      list.value = [];
    } else {
      console.warn('意外的响应格式:', resp.data);
      list.value = [];
    }
  } catch (e: any) {
    console.error('获取数据失败:', e);
    const errorMsg = e.response?.data?.message || e.message || '获取数据失败';
    showMessage(errorMsg, 'error');
    list.value = [];
  } finally {
    loading.value = false;
  }
}

function openAddModal() {
  editingType.value = null;
  typeForm.name = '';
  typeForm.description = '';
  typeForm.status = 1;
  showModal.value = true;
}

function editType(item: AppointmentType) {
  editingType.value = item;
  typeForm.name = item.name;
  typeForm.description = item.description || '';
  typeForm.status = item.status;
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
  editingType.value = null;
}

async function submitType() {
  if (!typeForm.name || !typeForm.name.trim()) {
    showMessage('请输入类型名称', 'error');
    return;
  }

  try {
    const url = editingType.value?.id ? '/appointmentType/update' : '/appointmentType/add';
    const data = editingType.value?.id
      ? { ...typeForm, id: editingType.value.id }
      : typeForm;
    
    console.log('提交预约类型数据:', url, data);
    
    const resp = await http.post(url, data);
    console.log('提交预约类型响应:', resp);
    
    if (resp.data === 'SUCCESS') {
      showMessage(editingType.value?.id ? '更新成功' : '添加成功', 'success');
      closeModal();
      fetchData();
    } else if (resp.data === 'NO_PERMISSION' || (typeof resp.data === 'string' && resp.data.includes('NO_PERMISSION'))) {
      showMessage('无权限执行此操作', 'error');
    } else if (resp.data === 'noName') {
      showMessage('类型名称不能为空', 'error');
    } else {
      const errorMsg = typeof resp.data === 'string' ? resp.data : '操作失败';
      showMessage(errorMsg, 'error');
    }
  } catch (e: any) {
    console.error('提交预约类型失败:', e);
    const errorMsg = e.response?.data?.message || e.message || '操作失败';
    showMessage(errorMsg, 'error');
  }
}

async function deleteType(id: number) {
  const confirmed = await showConfirm('确认删除该预约类型吗？删除后相关预约记录不会受影响，但用户将无法再选择此类型。');
  if (!confirmed) return;

  try {
    const resp = await http.post('/appointmentType/delete', { id });
    console.log('删除预约类型响应:', resp);
    
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
    } else if (resp.data === 'NO_PERMISSION' || (typeof resp.data === 'string' && resp.data.includes('NO_PERMISSION'))) {
      showMessage('无权限执行此操作', 'error');
    } else {
      const errorMsg = typeof resp.data === 'string' ? resp.data : '删除失败';
      showMessage(errorMsg, 'error');
    }
  } catch (e: any) {
    console.error('删除预约类型失败:', e);
    const errorMsg = e.response?.data?.message || e.message || '操作失败';
    showMessage(errorMsg, 'error');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.appointment-type-manage {
  padding: 0;
}

textarea.form-control {
  resize: vertical;
}
</style>
