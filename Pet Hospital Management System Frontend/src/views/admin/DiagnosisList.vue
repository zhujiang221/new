<template>
  <div class="diagnosis-list-admin">
    <div class="page-header">
      <h2>诊断记录管理</h2>
    </div>

    <!-- Search bar -->
    <div class="search-bar">
      <label>诊疗建议：</label>
      <input type="text" v-model="searchInfo" placeholder="请输入诊疗建议" @keyup.enter="search" />
      <button class="btn btn-primary" @click="search">查询</button>
    </div>

    <!-- Data table -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>诊断ID</th>
          <th>宠物名称</th>
          <th>所属用户</th>
          <th>医生</th>
          <th>类型</th>
          <th>性别</th>
          <th>状态</th>
          <th>诊疗建议</th>
          <th>诊断时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="10" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="10" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td data-label="序号">{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
          <td data-label="诊断ID">{{ item.id }}</td>
          <td data-label="宠物名称">{{ item.name || '-' }}</td>
          <td data-label="所属用户">{{ item.userName || '-' }}</td>
          <td data-label="医生">{{ item.doctorName || '-' }}</td>
          <td data-label="类型">
            <span class="type-badge" :class="getTypeClass(item.type)">
              {{ formatType(item.type) }}
            </span>
          </td>
          <td data-label="性别">{{ formatSex(item.sex) }}</td>
          <td data-label="状态">
            <span class="status-badge" :class="getStatusClass(item.status)">
              {{ formatStatus(item.status) }}
            </span>
          </td>
          <td data-label="诊疗建议" class="info-cell">{{ item.info || '-' }}</td>
          <td data-label="诊断时间">{{ item.createTime || '-' }}</td>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';

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
    case 1: return 'status-approved';
    case 2: return 'status-rejected';
    case 3: return 'status-pending';
    case 4: return 'status-rejected';
    default: return '';
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
    // 使用getAllByLimitDoctor接口获取所有诊断记录（管理员可以看到所有数据）
    const resp = await http.get('/user/diagnosis/getAllByLimitDoctor', {
      params: {
        info: searchInfo.value,
        page: pagination.page,
        limit: pagination.limit
      }
    });
    const data = resp.data;
    list.value = (data.rows || []) as Diagnosis[];
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
@import '../../assets/responsive.css';

.diagnosis-list-admin {
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
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-badge,
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-normal {
  background: #dbeafe;
  color: #1e40af;
}

.type-vaccine {
  background: #fef3c7;
  color: #92400e;
}

.type-deworm {
  background: #e0e7ff;
  color: #3730a3;
}

.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-rejected {
  background: #fee2e2;
  color: #991b1b;
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

.form-control {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
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
}
</style>
