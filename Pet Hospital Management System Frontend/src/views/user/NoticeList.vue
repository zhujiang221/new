<template>
  <div class="notice-list-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📚</span>
        健康指南
      </h1>
      <p class="modern-page-subtitle">阅读健康知识，提升宠物护理能力</p>
    </div>

    <div class="modern-search-bar">
      <input
        type="text"
        v-model="searchTitle"
        placeholder="🔍 按标题搜索..."
        class="modern-input"
        @keyup.enter="search"
      />
      <button class="modern-btn modern-btn-primary" @click="search">查询</button>
    </div>

    <div v-if="loading" class="modern-loading">加载中...</div>
    <div v-else-if="list.length === 0" class="modern-empty-state">
      <div class="modern-empty-state-icon">📄</div>
      <div class="modern-empty-state-text">暂无健康指南</div>
    </div>

    <div v-else class="notice-cards">
      <div v-for="item in list" :key="item.id" class="modern-card notice-card">
        <div class="notice-card-header">
          <h3 class="notice-title">{{ item.title }}</h3>
          <div class="notice-meta">
            <span class="badge view">👁️ {{ item.viewCount }}</span>
            <span class="badge time">🕒 {{ item.createTime }}</span>
          </div>
        </div>
        <div class="notice-card-body">
          <p class="notice-brief">点击查看详情</p>
        </div>
        <div class="notice-card-actions">
          <button class="modern-btn modern-btn-primary modern-btn-sm" @click="showDetail(item)">
            📖 阅读
          </button>
        </div>
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

    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal detail-modal">
        <div class="modal-header">
          <h3>{{ detailData.title }}</h3>
          <button class="modal-close" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-meta">
            <span>发布时间：{{ detailData.createTime }}</span>
            <span>浏览次数：{{ detailData.viewCount }}</span>
          </div>
          <div class="detail-content" v-html="detailData.content"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage } from '../../utils/message';

interface Notice {
  id: string;
  title: string;
  viewCount: number;
  createTime: string;
  content?: string;
}

const list = ref<Notice[]>([]);
const loading = ref(false);
const searchTitle = ref('');

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const showDetailModal = ref(false);
const detailData = reactive<Notice>({
  id: '',
  title: '',
  viewCount: 0,
  createTime: '',
  content: ''
});

async function fetchData() {
  loading.value = true;
  try {
    const resp = await http.get('/user/notice/getAllWordByLimit', {
      params: {
        title: searchTitle.value,
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

async function showDetail(item: Notice) {
  try {
    // Vue 前端使用 JSON 详情接口（/xq 是返回 HTML 页面）
    const resp = await http.get('/user/notice/getById', {
      params: { id: item.id }
    });
    const data = resp.data;
    detailData.id = item.id;
    detailData.title = data.title || item.title;
    detailData.viewCount = data.viewCount || item.viewCount;
    detailData.createTime = data.createTime || item.createTime;
    detailData.content = data.content || data;
    showDetailModal.value = true;
  } catch (e) {
    console.error('获取详情失败:', e);
    showMessage('获取详情失败', 'error');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.notice-list-modern {
  padding: 0;
}

.notice-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.notice-card {
  position: relative;
  overflow: hidden;
}

.notice-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #72C1BB 0%, #5aa9a3 100%);
}

.notice-card-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f9f8;
}

.notice-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
  font-size: 12px;
  color: #6b7280;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.badge.view {
  background: #dbeafe;
  color: #1e40af;
}

.badge.time {
  background: #e5e7eb;
  color: #374151;
}

.notice-card-body {
  padding: 10px 0;
}

.notice-brief {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.notice-card-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 2px solid #f0f9f8;
}

.detail-modal {
  min-width: 700px;
  max-width: 800px;
}

.detail-meta {
  display: flex;
  gap: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
  color: #666;
  font-size: 13px;
  flex-wrap: wrap;
}

.detail-content {
  line-height: 1.8;
  color: #333;
}

.detail-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.modal-overlay {
  backdrop-filter: blur(4px);
}

.modal {
  border-radius: 14px;
  overflow: hidden;
}

.modal-header {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
}

@media (max-width: 768px) {
  .modern-search-bar {
    flex-direction: column;
  }

  .modern-search-bar .modern-input,
  .modern-search-bar .modern-btn {
    width: 100%;
  }

  .notice-cards {
    grid-template-columns: 1fr;
  }

  .detail-modal {
    min-width: 100%;
    max-width: 100%;
  }
}
</style>
