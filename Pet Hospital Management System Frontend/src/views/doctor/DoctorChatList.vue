<template>
  <div class="doctor-chat-list">
    <div class="page-header">
      <h1>💬 我的聊天</h1>
    </div>

    <!-- 标签切换 -->
    <div class="chat-tabs">
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'sessions' }"
        @click="currentTab = 'sessions'"
      >
        我的聊天
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'requests' }"
        @click="currentTab = 'requests'"
      >
        聊天申请
        <span v-if="pendingRequests.length > 0" class="badge">{{ pendingRequests.length }}</span>
      </button>
    </div>

    <!-- 聊天会话列表 -->
    <div v-if="currentTab === 'sessions'">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="sessionList.length === 0" class="empty-state">
      <div class="empty-icon">💬</div>
      <div class="empty-text">暂无聊天会话</div>
    </div>
    <div v-else class="session-list">
      <div
        v-for="session in sessionList"
        :key="session.id"
        class="session-item"
        :class="{ active: currentSessionId === session.id }"
        @click="openChat(session)"
      >
        <div class="session-avatar">
          <img v-if="session.userImg" :src="session.userImg" :alt="session.userName" />
          <div v-else class="avatar-placeholder">{{ session.userName?.charAt(0) || 'U' }}</div>
        </div>
        <div class="session-info">
          <div class="session-header">
            <div class="session-name">{{ session.userName || '未知用户' }}</div>
            <div class="session-time">{{ formatTime(session.lastMessageTime || session.createTime) }}</div>
          </div>
          <div class="session-preview">
            <span v-if="session.lastMessageType === 'image'" class="preview-icon">📷</span>
            <span v-else-if="session.lastMessageType === 'emoji'" class="preview-icon">😊</span>
            <span class="preview-text">{{ getPreviewText(session) }}</span>
          </div>
        </div>
        <div v-if="session.unreadCount && session.unreadCount > 0" class="unread-badge">
          {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天申请列表 -->
    <div v-if="currentTab === 'requests'">
      <div class="request-tabs">
        <button
          class="sub-tab-btn"
          :class="{ active: requestTab === 'pending' }"
          @click="requestTab = 'pending'"
        >
          待审核 ({{ pendingRequests.length }})
        </button>
        <button
          class="sub-tab-btn"
          :class="{ active: requestTab === 'all' }"
          @click="requestTab = 'all'"
        >
          全部记录
        </button>
      </div>

      <div v-if="loadingRequests" class="loading">加载中...</div>
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
                <div class="request-time">{{ formatRequestTime(request.createTime) }}</div>
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
            <button class="btn btn-primary" @click="openChatFromRequest(request)">
              开始聊天
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { 
  getChatSessionList, 
  getChatRequestList, 
  approveChatRequest, 
  rejectChatRequest, 
  type ChatSession,
  type ChatRequest 
} from '../../api/chat';
import { websocketManager, type WebSocketMessage } from '../../utils/websocket';
import { getUserInfo } from '../../utils/user';
import { showMessage, showConfirm } from '../../utils/message';

const router = useRouter();
const route = useRoute();

// 根据路由参数决定默认标签
const currentTab = ref<'sessions' | 'requests'>(
  route.query.tab === 'requests' ? 'requests' : 'sessions'
);
const requestTab = ref<'pending' | 'all'>('pending');
const loading = ref(false);
const loadingRequests = ref(false);
const sessionList = ref<ChatSession[]>([]);
const requestList = ref<ChatRequest[]>([]);
const currentSessionId = ref<number | null>(null);

// 待处理的聊天申请
const pendingRequests = computed(() => {
  return requestList.value.filter(r => r.status === 0);
});

// 显示的申请列表
const displayRequests = computed(() => {
  if (requestTab.value === 'pending') {
    return pendingRequests.value;
  }
  return requestList.value;
});

// 监听路由变化，自动切换标签
import { watch } from 'vue';

watch(() => route.query.tab, (newTab) => {
  if (newTab === 'requests') {
    currentTab.value = 'requests';
  } else if (newTab === 'sessions') {
    currentTab.value = 'sessions';
  }
});

onMounted(() => {
  fetchSessions();
  fetchRequests();
  setupWebSocket();
  
  // 根据路由参数设置初始标签
  if (route.query.tab === 'requests') {
    currentTab.value = 'requests';
  }
});

onUnmounted(() => {
  // WebSocket会在全局管理，这里不需要断开
});

function setupWebSocket() {
  const userInfo = getUserInfo();
  if (userInfo && userInfo.id) {
    websocketManager.connect(userInfo.id, handleWebSocketMessage);
  }
}

function handleWebSocketMessage(message: WebSocketMessage) {
  if (message.type === 'chat') {
    fetchSessions();
    fetchRequests(); // 同时刷新申请列表
  }
}

async function fetchSessions() {
  loading.value = true;
  try {
    const list = await getChatSessionList();
    sessionList.value = list || [];
  } catch (e) {
    console.error('获取会话列表失败:', e);
  } finally {
    loading.value = false;
  }
}

function getPreviewText(session: ChatSession): string {
  // 检查消息是否被撤回（兼容多种可能的字段名）
  const lastMessageIsRevoked = (session as any).lastMessageIsRevoked ?? 
                                (session as any).last_message_is_revoked ?? 
                                0;
  const isRevoked = lastMessageIsRevoked === 1 || lastMessageIsRevoked === true;
  
  if (isRevoked) {
    const userInfo = getUserInfo();
    const senderId = (session as any).lastMessageSenderId ?? 
                     (session as any).last_message_sender_id;
    const isMyMessage = userInfo && senderId && Number(senderId) === Number(userInfo.id);
    return isMyMessage ? '你撤回了一条消息' : '对方撤回了一条消息';
  }
  
  if (session.lastMessageContent) {
    if (session.lastMessageType === 'text') {
      return session.lastMessageContent.length > 30
        ? session.lastMessageContent.substring(0, 30) + '...'
        : session.lastMessageContent;
    } else if (session.lastMessageType === 'image') {
      return '[图片]';
    } else if (session.lastMessageType === 'emoji') {
      return session.lastMessageContent;
    }
  }
  return '暂无消息';
}

function formatTime(timeStr?: string): string {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;

  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function openChat(session: ChatSession) {
  if (!session.id) {
    console.error('会话ID不存在');
    return;
  }
  currentSessionId.value = session.id;
  // 确保在移动端也能正确跳转
  router.push(`/doctor/chat/${session.id}`).catch((err) => {
    // 忽略导航重复的错误
    if (err.name !== 'NavigationDuplicated') {
      console.error('跳转到聊天窗口失败:', err);
    }
  });
}

// 加载聊天申请列表
async function fetchRequests() {
  loadingRequests.value = true;
  try {
    const list = await getChatRequestList();
    requestList.value = list || [];
  } catch (e) {
    console.error('获取聊天申请列表失败:', e);
    showMessage('获取聊天申请列表失败', 'error');
  } finally {
    loadingRequests.value = false;
  }
}

// 同意申请
async function approveRequest(id: number) {
  const confirmed = await showConfirm('确认同意该聊天申请吗？');
  if (!confirmed) return;

  try {
    const result = await approveChatRequest(id);
    if (result === 'SUCCESS') {
      showMessage('已同意申请，可以开始聊天了', 'success');
      await fetchRequests();
      await fetchSessions(); // 刷新会话列表
    } else {
      showMessage(result.includes('ERROR') ? result : '操作失败', 'error');
    }
  } catch (e: any) {
    console.error('同意申请失败:', e);
    showMessage(e.message || '操作失败', 'error');
  }
}

// 拒绝申请
async function rejectRequest(id: number) {
  const confirmed = await showConfirm('确认拒绝该聊天申请吗？');
  if (!confirmed) return;

  try {
    const result = await rejectChatRequest(id);
    if (result === 'SUCCESS') {
      showMessage('已拒绝申请', 'success');
      await fetchRequests();
    } else {
      showMessage(result.includes('ERROR') ? result : '操作失败', 'error');
    }
  } catch (e: any) {
    console.error('拒绝申请失败:', e);
    showMessage(e.message || '操作失败', 'error');
  }
}

// 从申请打开聊天
async function openChatFromRequest(request: ChatRequest) {
  try {
    // 获取聊天会话列表，查找对应的会话
    const sessions = await getChatSessionList();
    const session = sessions?.find(s => s.userId === request.userId);
    if (session) {
      router.push(`/doctor/chat/${session.id}`);
    } else {
      // 如果没有找到会话，跳转到聊天列表
      router.push('/doctor/chat');
    }
  } catch (e) {
    console.error('获取聊天会话失败:', e);
    // 出错时跳转到聊天列表
    router.push('/doctor/chat');
  }
}

// 获取状态文本
function getStatusText(status: number): string {
  switch (status) {
    case 0: return '待审核';
    case 1: return '已同意';
    case 2: return '已拒绝';
    default: return '未知';
  }
}

// 获取状态样式类
function getStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending';
    case 1: return 'status-approved';
    case 2: return 'status-rejected';
    default: return '';
  }
}

// 格式化申请时间
function formatRequestTime(timeStr?: string): string {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}
</script>

<style scoped>
.doctor-chat-list {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  height: 100%;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
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

.session-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.session-item:hover {
  background: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.session-item.active {
  background: linear-gradient(135deg, #f0f9f8 0%, #e8f5f3 100%);
  border: 2px solid #72C1BB;
}

.session-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  margin-right: 12px;
}

.session-avatar img {
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

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.session-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 12px;
}

.session-preview {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  overflow: hidden;
}

.preview-icon {
  flex-shrink: 0;
}

.preview-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #ff4d4f;
  color: white;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 标签切换样式 */
.chat-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px 8px 0 0;
  padding: 0 20px;
}

.tab-btn {
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: -2px;
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  color: #72C1BB;
}

.tab-btn.active {
  color: #72C1BB;
  border-bottom-color: #72C1BB;
  font-weight: 600;
}

.tab-btn .badge {
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  font-weight: 600;
}

/* 申请子标签 */
.request-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.sub-tab-btn {
  padding: 8px 16px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: -2px;
}

.sub-tab-btn:hover {
  color: #72C1BB;
}

.sub-tab-btn.active {
  color: #72C1BB;
  border-bottom-color: #72C1BB;
  font-weight: 600;
}

/* 申请列表样式 */
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
  transition: all 0.3s;
}

.request-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
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
  font-weight: 500;
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
  transform: translateY(-1px);
}

@media (max-width: 767px) {
  .doctor-chat-list {
    padding: 15px;
  }

  .session-item {
    padding: 12px;
  }

  .session-avatar {
    width: 40px;
    height: 40px;
  }

  .chat-tabs {
    padding: 0 10px;
  }

  .tab-btn {
    padding: 10px 16px;
    font-size: 14px;
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
