<template>
  <div class="apply-list-admin">
    <div class="page-header">
      <h2>预约管理</h2>
    </div>

    <!-- Search bar -->
    <div class="search-bar">
      <label>预约内容：</label>
      <input type="text" v-model="searchInfo" placeholder="请输入预约内容" @keyup.enter="search" />
      <button class="btn btn-primary" @click="search">查询</button>
    </div>

    <!-- Data table -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>预约ID</th>
          <th>宠物名称</th>
          <th>用户</th>
          <th>医生</th>
          <th>预约类型</th>
          <th>预约时间</th>
          <th>时间段</th>
          <th>创建时间</th>
          <th>状态</th>
          <th>联系电话</th>
          <th>地址</th>
          <th>预约内容</th>
          <th width="120">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="14" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="14" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td data-label="序号">{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
          <td data-label="预约ID">{{ item.id }}</td>
          <td data-label="宠物名称">{{ item.petName || item.name || '-' }}</td>
          <td data-label="用户">{{ item.userName || '-' }}</td>
          <td data-label="医生">{{ item.doctorName || '-' }}</td>
          <td data-label="预约类型">{{ item.appointmentTypeName || '-' }}</td>
          <td data-label="预约时间">{{ formatDateTime(item.appTime) || '-' }}</td>
          <td data-label="时间段">{{ item.timeSlot || '-' }}</td>
          <td data-label="创建时间">{{ item.createTime ? formatDateTime(item.createTime) : '-' }}</td>
          <td data-label="状态">
            <span class="status-badge" :class="getStatusClass(item.status)">
              {{ formatStatus(item.status) }}
            </span>
          </td>
          <td data-label="联系电话">{{ item.phone || '-' }}</td>
          <td data-label="地址" class="info-cell">{{ item.address || '-' }}</td>
          <td data-label="预约内容" class="info-cell">{{ item.info || '-' }}</td>
          <td data-label="操作">
            <button class="btn btn-sm btn-edit" @click="editItem(item)" title="编辑">编辑</button>
            <button class="btn btn-sm btn-delete" @click="deleteItem(item.id)" title="删除">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Pagination -->
    <div class="pagination">
      <span>共 {{ pagination.total }} 条</span>
      <button :disabled="pagination.page <= 1" @click="changePage(pagination.page - 1)">上一页</button>
      <span>第 {{ pagination.page }} / {{ totalPages }} 页</span>
      <button :disabled="pagination.page >= totalPages" @click="changePage(pagination.page + 1)">下一页</button>
      <select v-model="pagination.limit" @change="search" class="form-control" style="width: auto; padding: 8px 12px;">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- Edit Dialog -->
    <div v-if="showEditDialog" class="modal-overlay" @click="closeEditDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑预约</h3>
          <button class="modal-close" @click="closeEditDialog">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>预约内容：</label>
            <input type="text" v-model="editForm.info" placeholder="请输入预约内容" />
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model="editForm.status" class="form-control">
              <option :value="1">申请中</option>
              <option :value="2">申请通过</option>
              <option :value="3">不通过</option>
              <option :value="4">已完成</option>
              <option :value="5">已取消</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEditDialog">取消</button>
          <button class="btn btn-primary" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface Apply {
  id: string;
  name?: string;
  petName?: string;
  userName?: string;
  doctorName?: string;
  appointmentTypeName?: string;
  info: string;
  phone: string;
  address: string;
  appTime: string;
  timeSlot?: string;
  createTime?: string;
  status: number;
}

const list = ref<Apply[]>([]);
const loading = ref(false);
const searchInfo = ref('');
const chartLoading = ref(false);
const chartRef = ref<HTMLElement | null>(null);
const showEditDialog = ref(false);
const editForm = reactive({
  id: '',
  info: '',
  status: 1
});
let chartInstance: echarts.ECharts | null = null;

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

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
    case 1: return 'status-pending';
    case 2: return 'status-approved';
    case 3: return 'status-rejected';
    case 4: return 'status-completed';
    case 5: return 'status-cancelled';
    default: return '';
  }
}

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
    const resp = await http.get('/user/apply/getAllByLimitAdmin', {
      params: {
        info: searchInfo.value,
        page: pagination.page,
        limit: pagination.limit
      }
    });
    const data = resp.data;
    const rows = (data.rows || []) as Apply[];
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

async function fetchChartData() {
  chartLoading.value = true;
  try {
    const resp = await http.get('/user/apply/getAllByLimitAdmin', {
      params: { page: 1, limit: 10000 }
    });
    const rows = resp.data.rows || [];
    
    let a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0;
    rows.forEach((item: any) => {
      switch (item.status) {
        case 1: a1++; break;
        case 2: a2++; break;
        case 3: a3++; break;
        case 4: a4++; break;
        case 5: a5++; break;
      }
    });
    
    renderChart(a1, a2, a3, a4, a5);
  } catch (e) {
    console.error('获取统计数据失败:', e);
  } finally {
    chartLoading.value = false;
  }
}

function renderChart(a1: number, a2: number, a3: number, a4: number, a5: number) {
  if (!chartRef.value) return;
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  const option: echarts.EChartsOption = {
    title: {
      text: '预约状态数量统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['申请中', '申请通过', '不通过', '已完成', '已取消']
    },
    series: [
      {
        name: '预约状态',
        type: 'pie',
        radius: '55%',
        center: ['50%', '60%'],
        data: [
          { value: a1, name: '申请中' },
          { value: a2, name: '申请通过' },
          { value: a3, name: '不通过' },
          { value: a4, name: '已完成' },
          { value: a5, name: '已取消' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  chartInstance.setOption(option);
}

function editItem(item: Apply) {
  editForm.id = item.id;
  editForm.info = item.info;
  editForm.status = item.status;
  showEditDialog.value = true;
}

function closeEditDialog() {
  showEditDialog.value = false;
}

async function saveEdit() {
  try {
    const resp = await http.post('/user/apply/chStatus', editForm);
    if (resp.data === 'SUCCESS' || resp.data === 'jz') {
      showMessage('保存成功', 'success');
      closeEditDialog();
      fetchData();
      fetchChartData();
    } else {
      showMessage('保存失败：' + resp.data, 'error');
    }
  } catch (e) {
    console.error('保存失败:', e);
    showMessage('保存失败', 'error');
  }
}

async function deleteItem(id: string) {
  const confirmed = await showConfirm('确定要删除这个预约吗？');
  if (!confirmed) return;
  
  try {
    const resp = await http.get('/user/apply/del', {
      params: { id }
    });
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
      fetchChartData();
    } else {
      showMessage('删除失败：' + resp.data, 'error');
    }
  } catch (e) {
    console.error('删除失败:', e);
    showMessage('删除失败', 'error');
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

function handleResize() {
  chartInstance?.resize();
}

onMounted(() => {
  fetchData();
  fetchChartData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped>
@import '../../assets/responsive.css';

.apply-list-admin {
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
  flex-wrap: wrap;
}

.search-bar label {
  font-weight: 500;
  color: #555;
}

.search-bar input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.chart-card {
  padding: 16px;
  margin-bottom: 20px;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f9f8;
  margin-bottom: 12px;
}

.chart-title {
  font-weight: 600;
  color: #111827;
}

.chart-tip {
  color: #6b7280;
  font-size: 13px;
}

.chart-container {
  position: relative;
}

.chart {
  width: 100%;
  height: 400px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.data-table thead {
  background: #f5f5f5;
}

.data-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #ddd;
}

.data-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.data-table tbody tr:hover {
  background: #f9f9f9;
}

.data-table .loading,
.data-table .empty-state {
  text-align: center;
  color: #999;
  padding: 40px;
}

.info-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.status-completed {
  background: #dbeafe;
  color: #1e40af;
}

.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
}

.pagination button:hover:not(:disabled) {
  background: #f5f5f5;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
  margin-right: 5px;
}

.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
}

.form-control {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .data-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 8px;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar input {
    width: 100%;
  }

  .info-cell {
    max-width: 150px;
  }

  .chart {
    height: 300px;
  }
}
</style>
