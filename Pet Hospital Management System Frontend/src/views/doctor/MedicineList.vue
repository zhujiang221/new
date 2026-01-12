<template>
  <div class="medicine-list-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>💊</span>
        药品管理
      </h1>
      <p class="modern-page-subtitle">维护药品信息，支持开药与库存管理</p>
    </div>

    <div class="modern-search-bar">
      <input
        type="text"
        v-model="searchName"
        placeholder="🔍 按名称搜索药品..."
        class="modern-input"
        @keyup.enter="search"
      />
      <input
        type="text"
        v-model="searchType"
        placeholder="按类型搜索..."
        class="modern-input"
        @keyup.enter="search"
      />
      <select v-model="searchStatus" @change="search" class="modern-input" style="max-width: 140px;">
        <option value="">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
      <button class="modern-btn modern-btn-primary" @click="openAddModal">➕ 添加药品</button>
    </div>

    <div class="modern-card">
      <div v-if="loading" class="modern-loading">加载中...</div>
      <div v-else>
        <table class="data-table">
          <thead>
            <tr>
              <th width="50">#</th>
              <th>药品名称</th>
              <th>类型</th>
              <th>描述</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>创建时间</th>
              <th width="150">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="list.length === 0">
              <td colspan="9" class="empty-state">暂无数据</td>
            </tr>
            <tr v-for="(item, index) in list" :key="item.id">
              <td>{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.type || '-' }}</td>
              <td>{{ item.description || '-' }}</td>
              <td>¥{{ item.price?.toFixed(2) || '0.00' }}</td>
              <td>{{ item.stock || 0 }}</td>
              <td>
                <span :class="getStatusClass(item.status) + ' modern-badge'">{{ formatStatus(item.status) }}</span>
              </td>
              <td>{{ item.createTime || '-' }}</td>
              <td>
                <button class="modern-btn modern-btn-primary modern-btn-sm" @click="handleEdit(item)">编辑</button>
                <button class="modern-btn modern-btn-danger modern-btn-sm" @click="handleDelete(item)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

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

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingId ? '编辑药品' : '添加药品' }}</h3>
          <button class="modal-close" @click="showModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>药品名称：<span class="required">*</span></label>
            <input type="text" v-model="medicineForm.name" class="form-control" placeholder="请输入药品名称" />
          </div>
          <div class="form-group">
            <label>类型：</label>
            <input type="text" v-model="medicineForm.type" class="form-control" placeholder="请输入类型" />
          </div>
          <div class="form-group">
            <label>描述：</label>
            <textarea v-model="medicineForm.description" class="form-control" rows="3" placeholder="请输入描述"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>价格：<span class="required">*</span></label>
              <input type="number" step="0.01" v-model="medicineForm.price" class="form-control" placeholder="0.00" />
            </div>
            <div class="form-group">
              <label>库存：<span class="required">*</span></label>
              <input type="number" v-model="medicineForm.stock" class="form-control" placeholder="0" />
            </div>
          </div>
          <div class="form-group">
            <label>状态：</label>
            <select v-model="medicineForm.status" class="form-control">
              <option :value="1">上架</option>
              <option :value="0">下架</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitMedicine">保存</button>
          <button class="btn" @click="showModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { getMedicineList, addMedicine, updateMedicine, deleteMedicine, type Medicine } from '../../api/medicine';
import { showMessage, showConfirm } from '../../utils/message';

const list = ref<Medicine[]>([]);
const loading = ref(false);
const searchName = ref('');
const searchType = ref('');
const searchStatus = ref<number | ''>('');
const showModal = ref(false);
const editingId = ref<number | null>(null);

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const medicineForm = reactive<Medicine>({
  name: '',
  type: '',
  description: '',
  price: 0,
  stock: 0,
  status: 1
});

function formatStatus(status: number | undefined) {
  if (status === 1) return '上架';
  if (status === 0) return '下架';
  return '未知';
}

function getStatusClass(status: number | undefined) {
  if (status === 1) return 'status-badge status-approved';
  if (status === 0) return 'status-badge status-rejected';
  return 'status-badge';
}

async function fetchData() {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      limit: pagination.limit
    };
    if (searchName.value) {
      params.name = searchName.value;
    }
    if (searchType.value) {
      params.type = searchType.value;
    }
    if (searchStatus.value !== '') {
      params.status = searchStatus.value;
    }
    const data = await getMedicineList(params);
    list.value = data.rows || [];
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

function openAddModal() {
  editingId.value = null;
  Object.assign(medicineForm, {
    name: '',
    type: '',
    description: '',
    price: 0,
    stock: 0,
    status: 1
  });
  showModal.value = true;
}

function handleEdit(item: Medicine) {
  editingId.value = item.id || null;
  Object.assign(medicineForm, {
    id: item.id,
    name: item.name,
    type: item.type || '',
    description: item.description || '',
    price: item.price || 0,
    stock: item.stock || 0,
    status: item.status ?? 1
  });
  showModal.value = true;
}

async function submitMedicine() {
  if (!medicineForm.name) {
    showMessage('请输入药品名称', 'error');
    return;
  }
  if (!medicineForm.price || medicineForm.price <= 0) {
    showMessage('请输入有效的价格', 'error');
    return;
  }
  if (medicineForm.stock === undefined || medicineForm.stock < 0) {
    showMessage('请输入有效的库存', 'error');
    return;
  }

  try {
    let result: string;
    if (editingId.value) {
      result = await updateMedicine(medicineForm);
    } else {
      result = await addMedicine(medicineForm);
    }
    
    if (result === 'SUCCESS') {
      showMessage(editingId.value ? '更新成功' : '添加成功', 'success');
      showModal.value = false;
      fetchData();
    } else if (result === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage(result || '操作失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

async function handleDelete(item: Medicine) {
  const confirmed = await showConfirm('确认删除该药品吗？');
  if (!confirmed) return;
  
  try {
    const result = await deleteMedicine(item.id!);
    if (result === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
    } else if (result === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage(result || '删除失败', 'error');
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

.medicine-list-modern {
  padding: 0;
}

.required {
  color: red;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

@media (max-width: 767px) {
  .form-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .form-row .form-group {
    width: 100%;
  }
}
</style>

