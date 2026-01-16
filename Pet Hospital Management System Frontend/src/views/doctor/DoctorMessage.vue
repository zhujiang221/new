<template>
  <div class="doctor-message">
    <!-- 消息列表 -->
    <div class="message-list">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="allMessages.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-title">暂无消息</div>
        <div class="empty-desc">您还没有收到任何消息</div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="messages">
        <!-- 聊天申请（如果有未处理的申请，显示在第二个位置） -->
        <div
          v-if="pendingChatRequests.length > 0"
          class="message-item chat-request-item"
          :class="{ 'unread': true }"
          @click="handleChatRequestClick"
        >
          <div class="message-header">
            <div class="message-title-wrapper">
              <div class="message-title">有人加你</div>
              <div class="red-dot"></div>
            </div>
            <div class="message-time">{{ formatTime(latestChatRequestTime) }}</div>
          </div>
          <div class="message-content">
            <div class="summary-info">
              <div class="summary-count">
                <span class="count-number">{{ pendingChatRequests.length }}</span>
                <span class="count-text">条聊天申请</span>
              </div>
              <div v-if="latestChatRequest" class="summary-preview">
                <div class="preview-text">
                  <span class="preview-label">最新：</span>
                  <span class="preview-content">{{ latestChatRequest.userName || '未知用户' }}：{{ latestChatRequest.requestMessage || '申请与你聊天' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="unread-badge">未读</div>
        </div>

        <!-- 预约通知（逐个显示） -->
        <div
          v-for="message in appointmentNotifications"
          :key="`appointment-${message.id}`"
          class="message-item appointment-item"
          :class="{ 'unread': message.isRead === 0 }"
          @click="handleAppointmentClick(message)"
        >
          <div class="message-header">
            <div class="message-title-wrapper">
              <div class="message-title">新预约通知</div>
              <div v-if="message.isRead === 0" class="red-dot"></div>
            </div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <div class="message-content">
            <!-- BROADCAST类型消息直接显示内容 -->
            <template v-if="message.type === 'BROADCAST'">
              <div class="content-text">{{ getDisplayContent(message) }}</div>
            </template>
            <!-- 预约通知显示详细信息 -->
            <template v-else-if="getMessageContent(message.id)">
              <div class="content-detail">
                <div class="detail-item">
                  <span class="label">用户：</span>
                  <span class="value">{{ getMessageContent(message.id)?.userName }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">日期：</span>
                  <span class="value">{{ getMessageContent(message.id)?.appDate }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">时间段：</span>
                  <span class="value">{{ getMessageContent(message.id)?.timeSlot }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">类型：</span>
                  <span class="value">{{ getMessageContent(message.id)?.appointmentTypeName }}</span>
                </div>
                <div v-if="getMessageContent(message.id)?.info" class="detail-item">
                  <span class="label">内容：</span>
                  <span class="value">{{ getMessageContent(message.id)?.info }}</span>
                </div>
              </div>
            </template>
            <!-- 其他类型消息显示文本内容 -->
            <div v-else class="content-text">{{ getDisplayContent(message) }}</div>
          </div>
          <div v-if="message.isRead === 0" class="unread-badge">未读</div>
        </div>

        <!-- 其他消息 -->
        <div
          v-for="message in displayMessages"
          :key="`msg-${message.id}`"
          class="message-item"
          :class="{ 'unread': message.isRead === 0 }"
          @click="handleMessageClick(message)"
        >
          <div class="message-header">
            <div class="message-title-wrapper">
              <div class="message-title">{{ message.title }}</div>
              <div v-if="message.isRead === 0" class="red-dot"></div>
            </div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <div class="message-content">
            <!-- BROADCAST类型消息直接显示内容 -->
            <template v-if="message.type === 'BROADCAST'">
              <div class="content-text">{{ getDisplayContent(message) }}</div>
            </template>
            <!-- 预约通知显示详细信息 -->
            <template v-else-if="getMessageContent(message.id)">
              <div class="content-detail">
                <div class="detail-item">
                  <span class="label">用户：</span>
                  <span class="value">{{ getMessageContent(message.id)?.userName }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">日期：</span>
                  <span class="value">{{ getMessageContent(message.id)?.appDate }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">时间段：</span>
                  <span class="value">{{ getMessageContent(message.id)?.timeSlot }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">类型：</span>
                  <span class="value">{{ getMessageContent(message.id)?.appointmentTypeName }}</span>
                </div>
                <div v-if="getMessageContent(message.id)?.info" class="detail-item">
                  <span class="label">内容：</span>
                  <span class="value">{{ getMessageContent(message.id)?.info }}</span>
                </div>
              </div>
            </template>
            <!-- 其他类型消息显示文本内容 -->
            <div v-else class="content-text">{{ getDisplayContent(message) }}</div>
          </div>
          <div v-if="message.isRead === 0" class="unread-badge">未读</div>
        </div>
      </div>

      <!-- 分页（桌面端显示） -->
      <div v-if="total > 0 && !isMobile" class="modern-pagination">
        <span class="modern-pagination-info">共 {{ total }} 条</span>
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span class="modern-pagination-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">下一页</button>
        <select v-model="pageSize" @change="handleSizeChange" class="modern-input" style="width: auto; padding: 8px 12px;">
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
          <option :value="50">50条/页</option>
        </select>
      </div>
    </div>

    <!-- 聊天申请管理对话框 -->
    <div v-if="showChatRequestDialog" class="modal-overlay" @click.self="showChatRequestDialog = false">
      <div class="chat-request-dialog">
        <div class="dialog-header">
          <h3>聊天申请管理</h3>
          <button class="dialog-close" @click="showChatRequestDialog = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div v-if="loadingChatRequests" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="allChatRequests.length === 0" class="empty-dialog">
            <div class="empty-icon">📋</div>
            <div class="empty-title">暂无聊天申请</div>
          </div>
          <div v-else class="chat-request-list">
            <div
              v-for="request in allChatRequests"
              :key="request.id"
              class="chat-request-item-dialog"
              :class="{ 'pending': request.status === 0 }"
            >
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
                <button class="btn btn-primary" @click="openChatFromRequest(request)">
                  开始聊天
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新消息弹窗 -->
    <NotificationModal 
      v-model="showNotificationModal" 
      :message="notificationMessage"
      @close="handleNotificationModalClose"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Loading } from '@element-plus/icons-vue';
import { useNotification, type NotificationMessage, type NotificationContent } from '../../composables/useNotification';
import { 
  getChatRequestList, 
  getChatSessionList, 
  approveChatRequest, 
  rejectChatRequest, 
  type ChatRequest,
  type ChatSession 
} from '../../api/chat';
import { showMessage, showConfirm } from '../../utils/message';
import NotificationModal from '../../components/NotificationModal.vue';

const router = useRouter();
const {
  messageList,
  isLoading,
  total,
  fetchMessageList,
  fetchUnreadCount,
  markAsRead,
  parseMessageContent,
  initWebSocket,
  disconnectWebSocket,
  setOnNewMessageCallback,
  clearOnNewMessageCallback
} = useNotification();

const currentPage = ref(1);
const pageSize = ref(10);
const showChatRequestDialog = ref(false);
const isMobile = ref(false);
const loadingChatRequests = ref(false);
const chatRequests = ref<ChatRequest[]>([]);
const showNotificationModal = ref(false);
const notificationMessage = ref('');

// 检测移动端
function checkMobile() {
  isMobile.value = window.innerWidth <= 767;
}

// 监听窗口大小变化
function handleResize() {
  const prev = isMobile.value;
  checkMobile();
  if (prev !== isMobile.value) {
    currentPage.value = 1;
    loadMessages();
  }
}

// 计算总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

// 分离新预约通知和其他消息
const appointmentNotifications = computed(() => {
  return messageList.value.filter(msg => msg.title === '新预约通知');
});

const displayMessages = computed(() => {
  return messageList.value.filter(msg => msg.title !== '新预约通知');
});

// 待处理的聊天申请
const pendingChatRequests = computed(() => {
  return chatRequests.value.filter(r => r.status === 0);
});

// 所有聊天申请
const allChatRequests = computed(() => {
  return chatRequests.value;
});

// 最新的聊天申请
const latestChatRequest = computed(() => {
  if (pendingChatRequests.value.length === 0) return null;
  return pendingChatRequests.value[0];
});

// 最新的聊天申请时间
const latestChatRequestTime = computed(() => {
  if (pendingChatRequests.value.length === 0) return '';
  return pendingChatRequests.value[0].createTime || '';
});

// 所有消息（包括聊天申请、预约通知、其他消息）
const allMessages = computed(() => {
  const messages: any[] = [];
  // 聊天申请（如果有未处理的）
  if (pendingChatRequests.value.length > 0) {
    messages.push({ type: 'chat-request', id: 'chat-request' });
  }
  // 预约通知（逐个显示）
  messages.push(...appointmentNotifications.value.map(msg => ({ type: 'appointment', id: `appointment-${msg.id}`, ...msg })));
  // 其他消息
  messages.push(...displayMessages.value);
  return messages;
});

// 处理预约通知点击
async function handleAppointmentClick(message: NotificationMessage) {
  if (message.isRead === 0) {
    await markAsRead([message.id]);
  }
  if (message.appointmentId) {
    router.push('/doctor/apply');
  }
}

// 解析消息内容
const messageContentMap = computed(() => {
  const map = new Map<number, NotificationContent | null>();
  messageList.value.forEach(msg => {
    map.set(msg.id, parseMessageContent(msg.content));
  });
  return map;
});

// 获取消息内容
function getMessageContent(messageId: number): NotificationContent | null {
  return messageContentMap.value.get(messageId) || null;
}

// 获取预览文本
function getPreviewText(message: NotificationMessage): string {
  const content = getMessageContent(message.id);
  if (!content) return message.content || '';
  
  const parts: string[] = [];
  if (content.userName) parts.push(`用户：${content.userName}`);
  if (content.appDate) parts.push(`日期：${content.appDate}`);
  if (content.timeSlot) parts.push(`时间：${content.timeSlot}`);
  if (content.info) parts.push(`内容：${content.info.substring(0, 20)}${content.info.length > 20 ? '...' : ''}`);
  
  return parts.join(' | ');
}

// 获取显示内容（处理BROADCAST类型消息）
function getDisplayContent(message: any): string {
  if (!message || !message.content) {
    return '';
  }
  
  // 如果是BROADCAST类型，解析JSON并提取message字段
  if (message.type === 'BROADCAST') {
    try {
      const contentObj = JSON.parse(message.content);
      if (contentObj && typeof contentObj.message === 'string') {
        return contentObj.message;
      }
    } catch (e) {
      console.error('解析BROADCAST消息内容失败:', e);
    }
  }
  
  // 其他类型直接返回原始内容
  return message.content;
}

// 格式化时间
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

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');

  if (year === now.getFullYear()) {
    return `${month}-${day} ${hour}:${minute}`;
  }
  return `${year}-${month}-${day} ${hour}:${minute}`;
}

// 处理聊天申请点击
function handleChatRequestClick() {
  showChatRequestDialog.value = true;
  loadChatRequests();
}


// 处理消息点击
async function handleMessageClick(message: NotificationMessage) {
  if (message.isRead === 0) {
    await markAsRead([message.id]);
  }
  if (message.appointmentId) {
    router.push('/doctor/apply');
  }
}

// 加载聊天申请列表
async function loadChatRequests() {
  loadingChatRequests.value = true;
  try {
    const list = await getChatRequestList();
    chatRequests.value = list || [];
  } catch (e) {
    console.error('获取聊天申请列表失败:', e);
    showMessage('获取聊天申请列表失败', 'error');
  } finally {
    loadingChatRequests.value = false;
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
      await loadChatRequests();
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
      await loadChatRequests();
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

// 处理分页大小变化（仅桌面端）
function handleSizeChange() {
  if (isMobile.value) return;
  currentPage.value = 1;
  loadMessages();
}

// 处理页码变化（仅桌面端）
function changePage(page: number) {
  if (isMobile.value) return;
  currentPage.value = page;
  loadMessages();
}

// 加载消息列表
async function loadMessages() {
  if (isMobile.value) {
    await fetchMessageList(1, 2000);
  } else {
    await fetchMessageList(currentPage.value, pageSize.value);
  }
}

// 处理新消息弹窗
function handleNewMessage(message: NotificationMessage) {
  console.log('收到新消息，显示弹窗:', message);
  
  // 解析消息内容
  const content = parseMessageContent(message.content);
  if (content) {
    // 构建弹窗消息文本
    const parts: string[] = [];
    parts.push(message.title || '新消息');
    if (content.userName) parts.push(`用户：${content.userName}`);
    if (content.appDate) parts.push(`日期：${content.appDate}`);
    if (content.timeSlot) parts.push(`时间：${content.timeSlot}`);
    notificationMessage.value = parts.join('\n');
  } else {
    notificationMessage.value = message.title || '您有新消息';
  }
  
  // 显示弹窗
  showNotificationModal.value = true;
  
  // 刷新消息列表
  loadMessages();
}

// 处理弹窗关闭
function handleNotificationModalClose() {
  showNotificationModal.value = false;
  notificationMessage.value = '';
}

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', handleResize);
  loadMessages();
  loadChatRequests();
  
  // 刷新未读数量（进入页面时立即刷新，确保红点正确显示）
  fetchUnreadCount();
  
  // 设置新消息回调（用于显示弹窗）- 必须在 initWebSocket 之前设置
  setOnNewMessageCallback(handleNewMessage);
  console.log('DoctorMessage: 已设置新消息回调');
  
  // 初始化WebSocket连接
  initWebSocket();
  
  // 定期刷新聊天申请数据和未读数量
  const interval = setInterval(() => {
    loadChatRequests();
    fetchUnreadCount(); // 定期刷新未读数量
  }, 30000); // 每30秒刷新一次
  
  onUnmounted(() => {
    clearInterval(interval);
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  // 清理WebSocket回调
  clearOnNewMessageCallback();
  console.log('DoctorMessage: 已清理新消息回调');
  // 注意：不断开WebSocket连接，因为可能在其他页面也需要使用
  // disconnectWebSocket();
});
</script>

<style scoped>
.doctor-message {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  height: 100%;
}

.message-list {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #666;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  position: relative;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.message-item:hover {
  background: #f0f0f0;
  border-color: #72C1BB;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.1);
}

.message-item.unread {
  background: #fff;
  border-color: #72C1BB;
  border-width: 2px;
}

/* 聊天申请项样式 */
.chat-request-item {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border-color: #ff4d4f;
  border-width: 2px;
  animation: pulse-border 2s infinite;
}

.chat-session-item {
  background: #f0f9f8;
}

@keyframes pulse-border {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(255, 77, 79, 0.1);
  }
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.message-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.red-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  flex-shrink: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-count {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.count-number {
  font-size: 24px;
  font-weight: 700;
  color: #72C1BB;
}

.count-text {
  font-size: 14px;
  color: #666;
}

.summary-preview {
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

.preview-text {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  line-height: 1.6;
}

.preview-label {
  color: #999;
  flex-shrink: 0;
}

.preview-content {
  color: #333;
  flex: 1;
}

.chat-preview {
  color: #666;
  font-size: 14px;
}

.content-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  color: #999;
  min-width: 60px;
  flex-shrink: 0;
}

.detail-item .value {
  color: #333;
  flex: 1;
}

.content-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.unread-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 2px 8px;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  border-radius: 10px;
}

/* 新预约通知汇总样式 */
.appointment-summary {
  background: linear-gradient(135deg, #f0f9f8 0%, #e8f5f3 100%);
  border-color: #72C1BB;
  border-width: 2px;
}

.appointment-summary.unread {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border-color: #ff4d4f;
}

.modern-pagination {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.modern-pagination button {
  padding: 8px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #333;
}

.modern-pagination button:hover:not(:disabled) {
  border-color: #72C1BB;
  color: #72C1BB;
  background: #f0f9f8;
}

.modern-pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modern-pagination-info {
  color: #666;
  font-size: 14px;
  margin: 0 4px;
}

.modern-input {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #333;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.modern-input:hover {
  border-color: #72C1BB;
}

.modern-input:focus {
  outline: none;
  border-color: #72C1BB;
  box-shadow: 0 0 0 3px rgba(114, 193, 187, 0.1);
}

/* 对话框样式 */
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
  padding: 20px;
  backdrop-filter: blur(4px);
}

.appointment-dialog,
.chat-request-dialog {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
}

.dialog-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 28px;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.dialog-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-dialog {
  text-align: center;
  padding: 60px 20px;
}

.appointment-list,
.chat-request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.appointment-item,
.chat-request-item-dialog {
  position: relative;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fafafa;
}

.chat-request-item-dialog.pending {
  background: #fff5f5;
  border-color: #ff4d4f;
  border-width: 2px;
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
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
  background: #72C1BB;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.request-time {
  font-size: 12px;
  color: #999;
}

.request-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.request-message {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

.message-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.request-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #218838;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.btn-primary {
  background: #72C1BB;
  color: white;
}

.btn-primary:hover {
  background: #5aa9a3;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .doctor-message {
    padding: 15px;
  }

  .message-list {
    padding: 15px;
  }

  .message-item {
    padding: 12px;
  }

  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .message-title {
    font-size: 15px;
  }

  .count-number {
    font-size: 20px;
  }

  .summary-info {
    gap: 8px;
  }

  .detail-item {
    flex-direction: column;
    gap: 4px;
  }

  .detail-item .label {
    min-width: auto;
  }

  .modal-overlay {
    padding: 10px;
  }

  .appointment-dialog,
  .chat-request-dialog {
    max-height: 90vh;
    border-radius: 8px;
  }

  .dialog-header {
    padding: 15px;
  }

  .dialog-header h3 {
    font-size: 18px;
  }

  .dialog-body {
    padding: 15px;
  }
}
</style>
