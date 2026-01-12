<template>
  <div class="pet-list-admin">
    <div class="page-header">
      <h2>宠物管理</h2>
    </div>

    <!-- Search bar -->
    <div class="search-bar">
      <label>宠物名称：</label>
      <input type="text" v-model="searchName" placeholder="请输入宠物名称" @keyup.enter="search" />
      <button class="btn btn-primary" @click="search">查询</button>
    </div>

    <!-- Data table -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>宠物ID</th>
          <th>宠物名称</th>
          <th>类型</th>
          <th>身高(cm)</th>
          <th>体重(kg)</th>
          <th>年龄</th>
          <th>性别</th>
          <th>所属用户</th>
          <th width="200">操作</th>
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
          <td data-label="宠物ID">{{ item.id }}</td>
          <td data-label="宠物名称">{{ item.name || '-' }}</td>
          <td data-label="类型">
            <span class="type-badge" :class="getTypeClass(item.type)">
              {{ formatType(item.type) }}
            </span>
          </td>
          <td data-label="身高">{{ item.height || '-' }}</td>
          <td data-label="体重">{{ item.weight || '-' }}</td>
          <td data-label="年龄">{{ formatAge(item.birthday) }}</td>
          <td data-label="性别">{{ formatSex(item.sex) }}</td>
          <td data-label="所属用户">{{ item.userName || '-' }}</td>
          <td data-label="操作">
            <div class="btn-group">
              <button class="btn btn-primary btn-sm" @click="showDetail(item)">详情</button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(item)">删除</button>
            </div>
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

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal detail-modal">
        <div class="modal-header">
          <h3>宠物详情</h3>
          <button class="modal-close" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body" v-if="detailData">
          <div class="detail-grid">
            <div class="detail-item">
              <label>宠物ID：</label>
              <span>{{ detailData.id }}</span>
            </div>
            <div class="detail-item">
              <label>宠物名称：</label>
              <span>{{ detailData.name || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>宠物类型：</label>
              <span class="type-badge" :class="getTypeClass(detailData.type)">
                {{ formatType(detailData.type) }}
              </span>
            </div>
            <div class="detail-item">
              <label>身高：</label>
              <span>{{ detailData.height ? detailData.height + ' cm' : '-' }}</span>
            </div>
            <div class="detail-item">
              <label>体重：</label>
              <span>{{ detailData.weight ? detailData.weight + ' kg' : '-' }}</span>
            </div>
            <div class="detail-item">
              <label>年龄：</label>
              <span>{{ formatAge(detailData.birthday) }}</span>
            </div>
            <div class="detail-item">
              <label>性别：</label>
              <span>{{ formatSex(detailData.sex) }}</span>
            </div>
            <div class="detail-item">
              <label>生日：</label>
              <span>{{ detailData.birthday ? formatDate(detailData.birthday) : '-' }}</span>
            </div>
            <div class="detail-item">
              <label>所属用户：</label>
              <span>{{ detailData.userName || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ detailData.createTime ? formatDate(detailData.createTime) : '-' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface Pet {
  id: string;
  name: string;
  weight: number;
  height: number;
  birthday: string;
  type: string;
  sex?: string;
  userId?: string;
  userName?: string;
}

const list = ref<Pet[]>([]);
const loading = ref(false);
const searchName = ref('');
const showDetailModal = ref(false);
const detailData = ref<Pet | null>(null);

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

function formatType(type: string) {
  if (type === '1') return '猫科';
  if (type === '2') return '犬科';
  if (type === '3') return '其他';
  return type || '-';
}

function getTypeClass(type: string) {
  if (type === '1') return 'type-cat';
  if (type === '2') return 'type-dog';
  return 'type-other';
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

function formatDate(dateStr: string | Date) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function showDetail(item: Pet) {
  detailData.value = { ...item };
  showDetailModal.value = true;
}

async function fetchData() {
  loading.value = true;
  try {
    // 使用getAllByLimitDoctor接口获取所有宠物（管理员可以看到所有数据）
    const resp = await http.get('/user/pet/getAllByLimitDoctor', {
      params: {
        name: searchName.value,
        page: pagination.page,
        limit: pagination.limit
      }
    });
    const data = resp.data;
    list.value = (data.rows || []) as Pet[];
    pagination.total = data.total || 0;
  } catch (e) {
    console.error('获取数据失败:', e);
    showMessage('获取数据失败', 'error');
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
    console.error('删除失败:', e);
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
@import '../../assets/responsive.css';

.pet-list-admin {
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

.type-badge {
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

.type-other {
  background: #e5e7eb;
  color: #374151;
}

.btn-group {
  display: flex;
  gap: 8px;
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

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
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
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  padding: 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.detail-modal {
  max-width: 700px;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item label {
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.detail-item span {
  color: #333;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .modal {
    width: 95%;
    max-width: 95%;
  }
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
}
</style>
