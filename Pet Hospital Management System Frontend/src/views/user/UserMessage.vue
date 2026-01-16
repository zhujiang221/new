<template>
  <div class="user-message">
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
            <template v-if="getMessageContent(message.id)">
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
            <div v-else class="content-text">{{ message.content }}</div>
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
              <div class="message-title">{{ message.title || '系统消息' }}</div>
              <div v-if="message.isRead === 0" class="red-dot"></div>
            </div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <div class="message-content">
            <div class="content-text">{{ getDisplayContent(message) }}</div>
          </div>
          <div v-if="message.isRead === 0" class="unread-badge">未读</div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        :disabled="currentPage <= 1" 
        @click="changePage(currentPage - 1)"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
      <button 
        :disabled="currentPage >= totalPages" 
        @click="changePage(currentPage + 1)"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 聊天申请对话框 -->
    <el-dialog
      v-model="showChatRequestDialog"
      title="聊天申请"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="loadingChatRequests" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="chatRequests.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-title">暂无聊天申请</div>
      </div>
      <div v-else class="chat-request-list">
        <div
          v-for="request in chatRequests"
          :key="request.id"
          class="chat-request-item-dialog"
        >
          <div class="request-info">
            <div class="request-user">{{ request.userName || '未知用户' }}</div>
            <div class="request-message">{{ request.requestMessage || '申请与你聊天' }}</div>
            <div class="request-time">{{ formatTime(request.createTime) }}</div>
          </div>
          <div class="request-actions" v-if="request.status === 0">
            <el-button type="primary" size="small" @click="handleAcceptRequest(request)">
              同意
            </el-button>
            <el-button type="danger" size="small" @click="handleRejectRequest(request)">
              拒绝
            </el-button>
          </div>
          <div v-else class="request-status">
            <span :class="request.status === 1 ? 'status-accepted' : 'status-rejected'">
              {{ request.status === 1 ? '已同意' : '已拒绝' }}
            </span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Loading } from '@element-plus/icons-vue';
import { useNotification } from '../../composables/useNotification';
import { getChatRequestList, approveChatRequest, rejectChatRequest } from '../../api/chat';
import { showMessage } from '../../utils/message';

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
const loadingChatRequests = ref(false);
const chatRequests = ref<any[]>([]);

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

// 所有消息（包括预约通知、其他消息）
const allMessages = computed(() => {
  const messages: any[] = [];
  // 预约通知
  messages.push(...appointmentNotifications.value);
  // 其他消息
  messages.push(...displayMessages.value);
  return messages;
});

// 消息内容缓存
const messageContentCache = ref<Record<number, any>>({});

// 获取消息内容
function getMessageContent(messageId: number) {
  if (messageContentCache.value[messageId]) {
    return messageContentCache.value[messageId];
  }
  
  const message = messageList.value.find(m => m.id === messageId);
  if (message && message.content) {
    const content = parseMessageContent(message.content);
    if (content) {
      messageContentCache.value[messageId] = content;
      return content;
    }
  }
  return null;
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
function formatTime(time: string | Date) {
  if (!time) return '';
  const date = typeof time === 'string' ? new Date(time) : time;
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${month}-${day}`;
}

// 加载消息列表
function loadMessages() {
  fetchMessageList(currentPage.value, pageSize.value);
}

// 加载聊天申请
async function loadChatRequests() {
  loadingChatRequests.value = true;
  try {
    const requests = await getChatRequestList(0); // status=0表示待审核
    chatRequests.value = requests || [];
  } catch (e) {
    console.error('加载聊天申请失败:', e);
    chatRequests.value = [];
  } finally {
    loadingChatRequests.value = false;
  }
}

// 切换页面
function changePage(page: number) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadMessages();
}

// 处理消息点击
async function handleMessageClick(message: any) {
  if (message.isRead === 0) {
    await markAsRead([message.id]);
    await fetchUnreadCount();
  }
  // 可以根据消息类型跳转到相应页面
  if (message.title === '新预约通知') {
    router.push('/user/apply');
  }
}

// 处理预约通知点击
async function handleAppointmentClick(message: any) {
  if (message.isRead === 0) {
    await markAsRead([message.id]);
    await fetchUnreadCount();
  }
  router.push('/user/apply');
}

// 处理聊天申请点击
function handleChatRequestClick() {
  showChatRequestDialog.value = true;
  loadChatRequests();
}

// 处理新消息到达
function handleNewMessage(message: any) {
  console.log('UserMessage收到新消息:', message);
  // 刷新消息列表
  loadMessages();
  // 如果是聊天申请，刷新聊天申请列表
  if (message.title === '聊天申请') {
    loadChatRequests();
  }
  // 刷新未读数量
  fetchUnreadCount();
}

// 同意聊天申请
async function handleAcceptRequest(request: any) {
  try {
    await approveChatRequest(request.id);
    showMessage('已同意聊天申请', 'success');
    await loadChatRequests();
    // 刷新未读数量
    fetchUnreadCount();
  } catch (e) {
    console.error('同意聊天申请失败:', e);
    showMessage('操作失败', 'error');
  }
}

// 拒绝聊天申请
async function handleRejectRequest(request: any) {
  try {
    await rejectChatRequest(request.id);
    showMessage('已拒绝聊天申请', 'success');
    await loadChatRequests();
    // 刷新未读数量
    fetchUnreadCount();
  } catch (e) {
    console.error('拒绝聊天申请失败:', e);
    showMessage('操作失败', 'error');
  }
}

onMounted(() => {
  loadMessages();
  loadChatRequests();
  
  // 设置新消息回调（用于显示弹窗）- 必须在 initWebSocket 之前设置
  setOnNewMessageCallback(handleNewMessage);
  console.log('UserMessage: 已设置新消息回调');
  
  // 初始化WebSocket连接
  initWebSocket();
  
  // 刷新未读数量
  fetchUnreadCount();
  
  // 定期刷新聊天申请数据
  const interval = setInterval(() => {
    loadChatRequests();
    fetchUnreadCount();
  }, 30000); // 每30秒刷新一次
  
  onUnmounted(() => {
    clearInterval(interval);
  });
});

onUnmounted(() => {
  // 清理WebSocket回调
  clearOnNewMessageCallback();
  console.log('UserMessage: 已清理新消息回调');
  // 注意：不断开WebSocket连接，因为可能在其他页面也需要使用
  // disconnectWebSocket();
});
</script>

<style scoped>
.user-message {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 12px;
  color: #666;
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
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  position: relative;
}

.message-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.message-item.unread {
  border-left: 3px solid #72C1BB;
  background: #f9fafb;
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
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.content-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  gap: 8px;
}

.detail-item .label {
  color: #999;
  font-weight: 500;
}

.detail-item .value {
  color: #333;
}

.content-text {
  color: #666;
}

.unread-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.chat-request-item {
  background: linear-gradient(135deg, #f0f9f8 0%, #e8f5f3 100%);
}

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.count-number {
  font-size: 20px;
  font-weight: bold;
  color: #72C1BB;
}

.count-text {
  font-size: 14px;
  color: #666;
}

.preview-text {
  font-size: 13px;
  color: #666;
}

.preview-label {
  color: #999;
}

.preview-content {
  color: #333;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  background: #72C1BB;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #5aa9a3;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.chat-request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.chat-request-item-dialog {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.request-info {
  flex: 1;
}

.request-user {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.request-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.request-time {
  font-size: 12px;
  color: #999;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.request-status {
  font-size: 14px;
  font-weight: 500;
}

.status-accepted {
  color: #67c23a;
}

.status-rejected {
  color: #f56c6c;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .user-message {
    padding: 15px;
  }

  .message-item {
    padding: 12px;
  }

  .chat-request-item-dialog {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .request-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
