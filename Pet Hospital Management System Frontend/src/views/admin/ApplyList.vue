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
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="13" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="13" class="empty-state">暂无数据</td>
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
    // 使用getAllByLimitDoctor接口获取所有预约记录（管理员可以看到所有数据）
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

onMounted(() => {
  fetchData();
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
