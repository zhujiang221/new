<template>
  <div class="doctor-chat-list">
    <div class="page-header">
      <h1>💬 我的聊天</h1>
    </div>

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
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { getChatSessionList, type ChatSession } from '../../api/chat';
import { websocketManager, type WebSocketMessage } from '../../utils/websocket';
import { getUserInfo } from '../../utils/user';

const router = useRouter();

const loading = ref(false);
const sessionList = ref<ChatSession[]>([]);
const currentSessionId = ref<number | null>(null);

onMounted(() => {
  fetchSessions();
  setupWebSocket();
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
}
</style>
