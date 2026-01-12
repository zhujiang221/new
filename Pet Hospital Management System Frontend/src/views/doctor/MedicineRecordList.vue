<template>
  <div class="medicine-record-list-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📜</span>
        开药记录
      </h1>
      <p class="modern-page-subtitle">查看历史开药记录，便于追踪用药情况</p>
    </div>

    <div class="modern-search-bar">
      <input
        type="number"
        v-model="searchPetId"
        placeholder="按宠物ID搜索"
        class="modern-input"
        @keyup.enter="search"
      />
      <input
        type="number"
        v-model="searchDiagnosisId"
        placeholder="按诊断ID搜索"
        class="modern-input"
        @keyup.enter="search"
      />
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
    </div>

    <div class="modern-card">
      <div v-if="loading" class="modern-loading">加载中...</div>
      <div v-else>
    <table class="data-table">
      <thead>
        <tr>
          <th width="50">#</th>
          <th>宠物名</th>
          <th>用户名</th>
          <th>医生名</th>
          <th>药品名</th>
          <th>数量</th>
          <th>用量</th>
          <th>用法</th>
          <th>价格</th>
          <th>创建时间</th>
          <th width="100">操作</th>
        </tr>
      </thead>
      <tbody>
            <tr v-if="list.length === 0">
          <td colspan="11" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td>{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
          <td>{{ item.petName || '-' }}</td>
          <td>{{ item.userName || '-' }}</td>
          <td>{{ item.doctorName || '-' }}</td>
          <td>{{ item.medicineName || '-' }}</td>
          <td>{{ item.quantity || 0 }}</td>
          <td>{{ item.dosage || '-' }}</td>
          <td>{{ item.usage || '-' }}</td>
          <td>¥{{ item.medicinePrice?.toFixed(2) || '0.00' }}</td>
          <td>{{ item.createTime || '-' }}</td>
          <td>
                <button class="modern-btn modern-btn-danger modern-btn-sm" @click="handleDelete(item)">🗑️ 删除</button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { getMedicineRecordListDoctor, deleteMedicineRecord, type MedicineRecord } from '../../api/medicineRecord';
import { showMessage, showConfirm } from '../../utils/message';

const list = ref<MedicineRecord[]>([]);
const loading = ref(false);
const searchPetId = ref<number | ''>('');
const searchDiagnosisId = ref<number | ''>('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

async function fetchData() {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      limit: pagination.limit
    };
    if (searchPetId.value) {
      params.petId = searchPetId.value;
    }
    if (searchDiagnosisId.value) {
      params.diagnosisId = searchDiagnosisId.value;
    }
    const data = await getMedicineRecordListDoctor(params);
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

async function handleDelete(item: MedicineRecord) {
  const confirmed = await showConfirm('确认删除该开药记录吗？');
  if (!confirmed) return;
  
  try {
    const result = await deleteMedicineRecord(item.id!);
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

.medicine-record-list-modern {
  padding: 0;
}
</style>

