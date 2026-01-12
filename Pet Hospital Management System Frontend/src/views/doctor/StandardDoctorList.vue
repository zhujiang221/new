<template>
  <div class="standard-doctor-list">
    <div class="page-header">
      <h2>健康标准管理</h2>
    </div>

    <!-- Search bar -->
    <div class="search-bar">
      <label>类型：</label>
      <select v-model="searchType" @change="search">
        <option value="">全部</option>
        <option value="1">猫科</option>
        <option value="2">犬科</option>
      </select>
      <button class="btn btn-primary" @click="search">查询</button>
      <button class="btn btn-primary" @click="openAddModal">增加</button>
    </div>

    <!-- Data table -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>年龄区间</th>
          <th>体温范围</th>
          <th>体重范围</th>
          <th>身高范围</th>
          <th>饭量范围</th>
          <th>状态</th>
          <th>类型</th>
          <th width="100">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="9" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="9" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td>{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
          <td>{{ item.ageMin }} - {{ item.ageMax }}岁</td>
          <td>{{ item.tempMin }} - {{ item.tempMax }}℃</td>
          <td>{{ item.weightMin }} - {{ item.weightMax }}Kg</td>
          <td>{{ item.heightMin }} - {{ item.heightMax }}cm</td>
          <td>{{ item.appetiteMin }} - {{ item.appetiteMax }}g</td>
          <td>
            <span :class="getStatusClass(item.status)">{{ formatStatus(item.status) }}</span>
          </td>
          <td>{{ formatType(item.type) }}</td>
          <td>
            <button class="btn btn-danger btn-sm" @click="handleDelete(item)">删除</button>
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
      <select v-model="pagination.limit" @change="search">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- Add Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal large-modal">
        <div class="modal-header">
          <h3>添加健康标准</h3>
          <button class="modal-close" @click="showAddModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>类型：</label>
              <select v-model="standardForm.type" class="form-control">
                <option value="1">猫科</option>
                <option value="2">犬科</option>
              </select>
            </div>
            <div class="form-group">
              <label>状态：</label>
              <select v-model="standardForm.status" class="form-control">
                <option :value="1">正常</option>
                <option :value="2">异常</option>
                <option :value="3">生病</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>最小年龄：</label>
              <input type="number" v-model="standardForm.ageMin" class="form-control" />
            </div>
            <div class="form-group">
              <label>最大年龄：</label>
              <input type="number" v-model="standardForm.ageMax" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>最低体温：</label>
              <input type="number" step="0.1" v-model="standardForm.tempMin" class="form-control" />
            </div>
            <div class="form-group">
              <label>最高体温：</label>
              <input type="number" step="0.1" v-model="standardForm.tempMax" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>最低体重：</label>
              <input type="number" step="0.1" v-model="standardForm.weightMin" class="form-control" />
            </div>
            <div class="form-group">
              <label>最高体重：</label>
              <input type="number" step="0.1" v-model="standardForm.weightMax" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>最低身高：</label>
              <input type="number" step="0.1" v-model="standardForm.heightMin" class="form-control" />
            </div>
            <div class="form-group">
              <label>最高身高：</label>
              <input type="number" step="0.1" v-model="standardForm.heightMax" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>最低饭量：</label>
              <input type="number" v-model="standardForm.appetiteMin" class="form-control" />
            </div>
            <div class="form-group">
              <label>最高饭量：</label>
              <input type="number" v-model="standardForm.appetiteMax" class="form-control" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitStandard">保存</button>
          <button class="btn" @click="showAddModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface Standard {
  id: string;
  ageMin: number;
  ageMax: number;
  tempMin: number;
  tempMax: number;
  weightMin: number;
  weightMax: number;
  heightMin: number;
  heightMax: number;
  appetiteMin: number;
  appetiteMax: number;
  status: number;
  type: string;
}

const list = ref<Standard[]>([]);
const loading = ref(false);
const searchType = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showAddModal = ref(false);

const standardForm = reactive({
  type: '1',
  status: 1,
  ageMin: 0,
  ageMax: 0,
  tempMin: 0,
  tempMax: 0,
  weightMin: 0,
  weightMax: 0,
  heightMin: 0,
  heightMax: 0,
  appetiteMin: 0,
  appetiteMax: 0
});

function formatType(type: string) {
  return type === '1' ? '猫科' : type === '2' ? '犬科' : type;
}

function formatStatus(status: number) {
  switch (status) {
    case 1: return '正常';
    case 2: return '异常';
    case 3: return '生病';
    default: return '未知';
  }
}

function getStatusClass(status: number) {
  switch (status) {
    case 1: return 'status-badge status-approved';
    case 2: return 'status-badge status-pending';
    case 3: return 'status-badge status-rejected';
    default: return 'status-badge';
  }
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/user/standard/getAllByLimitDoctor', {
      params: {
        type: searchType.value,
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

function openAddModal() {
  Object.assign(standardForm, {
    type: '1',
    status: 1,
    ageMin: 0,
    ageMax: 10,
    tempMin: 37,
    tempMax: 40,
    weightMin: 1,
    weightMax: 20,
    heightMin: 10,
    heightMax: 50,
    appetiteMin: 50,
    appetiteMax: 200
  });
  showAddModal.value = true;
}

async function handleDelete(item: Standard) {
  const confirmed = await showConfirm('确认删除该标准吗？');
  if (!confirmed) return;
  
  try {
    const resp = await http.post('/user/standard/del', { id: item.id });
    if (resp.data === 'SUCCESS') {
      showMessage('删除成功', 'success');
      fetchData();
    } else if (resp.data === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage('删除失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

async function submitStandard() {
  // 基本非空与区间合理性校验
  if (standardForm.ageMin < 0 || standardForm.ageMax <= standardForm.ageMin) {
    showMessage('请填写正确的年龄区间', 'error');
    return;
  }
  if (!standardForm.tempMin || !standardForm.tempMax || standardForm.tempMax <= standardForm.tempMin) {
    showMessage('请填写正确的体温范围', 'error');
    return;
  }
  if (!standardForm.weightMin || !standardForm.weightMax || standardForm.weightMax <= standardForm.weightMin) {
    showMessage('请填写正确的体重范围', 'error');
    return;
  }
  if (!standardForm.heightMin || !standardForm.heightMax || standardForm.heightMax <= standardForm.heightMin) {
    showMessage('请填写正确的身高范围', 'error');
    return;
  }
  if (!standardForm.appetiteMin || !standardForm.appetiteMax || standardForm.appetiteMax <= standardForm.appetiteMin) {
    showMessage('请填写正确的饭量范围', 'error');
    return;
  }

  try {
    const resp = await http.post('/user/standard/doAdd', standardForm);
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('添加成功', 'success');
      showAddModal.value = false;
      fetchData();
    } else {
      showMessage('添加失败', 'error');
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
.standard-doctor-list {
  padding: 0;
}

.large-modal {
  min-width: 600px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .large-modal {
    min-width: 100% !important;
    max-width: 100% !important;
  }
  
  .form-row {
    flex-direction: column;
    gap: 15px;
    margin-bottom: 15px;
  }
  
  .form-row .form-group {
    width: 100%;
    margin-bottom: 0;
  }
  
  .modal-body .form-group label {
    display: block;
    margin-bottom: 8px;
    width: 100%;
  }
  
  .modal-body .form-group input,
  .modal-body .form-group select,
  .modal-body .form-group textarea {
    width: 100%;
    font-size: 16px;
    padding: 12px;
    box-sizing: border-box;
  }
  
  .modal-body .form-group textarea {
    min-height: 120px;
  }
}
</style>

