<template>
  <div class="doctor-chat-request">
    <div class="page-header">
      <h1>💬 聊天申请管理</h1>
      <p>处理用户发起的聊天申请</p>
    </div>

    <div class="request-tabs">
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'pending' }"
        @click="currentTab = 'pending'"
      >
        待审核 ({{ pendingRequests.length }})
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'all' }"
        @click="currentTab = 'all'"
      >
        全部记录
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="displayRequests.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <div class="empty-text">暂无申请记录</div>
    </div>
    <div v-else class="request-list">
      <div v-for="request in displayRequests" :key="request.id" class="request-card">
        <div class="request-header">
          <div class="user-info">
            <div class="user-avatar">
              <img v-if="request.userImg" :src="request.userImg" :alt="request.userName" />
              <div v-else class="avatar-placeholder">{{ request.userName?.charAt(0) || 'U' }}</div>
            </div>
            <div class="user-details">
              <div class="user-name">{{ request.userName || '未知用户' }}</div>
              <div class="request-time">{{ formatTime(request.createTime) }}</div>
            </div>
          </div>
          <div class="request-status" :class="getStatusClass(request.status)">
            {{ getStatusText(request.status) }}
          </div>
        </div>
        <div v-if="request.requestMessage" class="request-message">
          <div class="message-label">申请留言：</div>
          <div class="message-content">{{ request.requestMessage }}</div>
        </div>
        <div v-if="request.status === 0" class="request-actions">
          <button class="btn btn-success" @click="approveRequest(request.id!)">
            同意
          </button>
          <button class="btn btn-danger" @click="rejectRequest(request.id!)">
            拒绝
          </button>
        </div>
        <div v-else-if="request.status === 1" class="request-actions">
          <button class="btn btn-primary" @click="openChat(request)">
            开始聊天
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getChatRequestList, approveChatRequest, rejectChatRequest, type ChatRequest } from '../../api/chat';
import { showMessage, showConfirm } from '../../utils/message';

const router = useRouter();

const loading = ref(false);
const currentTab = ref<'pending' | 'all'>('pending');
const requestList = ref<ChatRequest[]>([]);

const pendingRequests = computed(() => {
  return requestList.value.filter(r => r.status === 0);
});

const displayRequests = computed(() => {
  if (currentTab.value === 'pending') {
    return pendingRequests.value;
  }
  return requestList.value;
});

onMounted(() => {
  fetchRequests();
});

async function fetchRequests() {
  loading.value = true;
  try {
    console.log('开始获取聊天申请列表...');
    const list = await getChatRequestList();
    console.log('获取到的申请列表:', list);
    requestList.value = list || [];
    console.log('申请列表数量:', requestList.value.length);
  } catch (e) {
    console.error('获取申请列表失败:', e);
    showMessage('获取申请列表失败', 'error');
  } finally {
    loading.value = false;
  }
}

async function approveRequest(id: number) {
  const confirmed = await showConfirm('确认同意该聊天申请吗？');
  if (!confirmed) return;

  try {
    const result = await approveChatRequest(id);
    if (result === 'SUCCESS') {
      showMessage('已同意申请，可以开始聊天了', 'success');
      fetchRequests();
    } else {
      showMessage(result.includes('ERROR') ? result : '操作失败', 'error');
    }
  } catch (e: any) {
    console.error('同意申请失败:', e);
    showMessage(e.message || '操作失败', 'error');
  }
}

async function rejectRequest(id: number) {
  const confirmed = await showConfirm('确认拒绝该聊天申请吗？');
  if (!confirmed) return;

  try {
    const result = await rejectChatRequest(id);
    if (result === 'SUCCESS') {
      showMessage('已拒绝申请', 'success');
      fetchRequests();
    } else {
      showMessage(result.includes('ERROR') ? result : '操作失败', 'error');
    }
  } catch (e: any) {
    console.error('拒绝申请失败:', e);
    showMessage(e.message || '操作失败', 'error');
  }
}

function openChat(request: ChatRequest) {
  // 跳转到聊天窗口，需要先找到对应的会话
  router.push('/doctor/chat');
}

function getStatusText(status: number): string {
  switch (status) {
    case 0: return '待审核';
    case 1: return '已同意';
    case 2: return '已拒绝';
    default: return '未知';
  }
}

function getStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending';
    case 1: return 'status-approved';
    case 2: return 'status-rejected';
    default: return '';
  }
}

function formatTime(timeStr?: string): string {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}
</script>

<style scoped>
.doctor-chat-request {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  font-size: 14px;
}

.request-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: -2px;
}

.tab-btn.active {
  color: #72C1BB;
  border-bottom-color: #72C1BB;
  font-weight: 600;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  color: #666;
  font-size: 16px;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.request-time {
  font-size: 12px;
  color: #999;
}

.request-status {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.request-message {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.message-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.message-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.request-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-success {
  background: #52c41a;
  color: white;
}

.btn-success:hover {
  background: #73d13d;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
}

.btn-danger:hover {
  background: #ff7875;
}

.btn-primary {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
}

@media (max-width: 767px) {
  .doctor-chat-request {
    padding: 15px;
  }

  .request-card {
    padding: 16px;
  }

  .request-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
