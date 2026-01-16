<template>
  <div class="user-chat">
    <!-- 标签页切换 -->
    <div class="chat-tabs">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'list' }"
        @click="activeTab = 'list'"
      >
        <span class="tab-icon">💬</span>
        <span class="tab-label">我的聊天</span>
        <span v-if="chatUnreadCount > 0" class="tab-badge">{{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'request' }"
        @click="activeTab = 'request'"
      >
        <span class="tab-icon">📋</span>
        <span class="tab-label">聊天申请</span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="chat-content">
      <!-- 我的聊天 -->
      <div v-show="activeTab === 'list'" class="tab-panel">
        <UserChatList />
      </div>

      <!-- 聊天申请 -->
      <div v-show="activeTab === 'request'" class="tab-panel">
        <UserChatRequest />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import UserChatList from './UserChatList.vue';
import UserChatRequest from './UserChatRequest.vue';
import { getUnreadChatMessageCount } from '../../api/chat';
import { websocketManager, type WebSocketMessage } from '../../utils/websocket';
import { getUserInfo } from '../../utils/user';

const activeTab = ref<'list' | 'request'>('list');
const chatUnreadCount = ref(0);

// 获取未读消息数
async function fetchUnreadCounts() {
  try {
    // 获取聊天未读消息数
    const chatCount = await getUnreadChatMessageCount();
    chatUnreadCount.value = typeof chatCount === 'number' ? chatCount : 0;
  } catch (e) {
    console.error('获取未读数失败:', e);
  }
}

// 设置WebSocket监听
function setupWebSocket() {
  const userInfo = getUserInfo();
  if (userInfo && userInfo.id) {
    websocketManager.connect(userInfo.id, handleWebSocketMessage);
  }
}

function handleWebSocketMessage(message: WebSocketMessage) {
  if (message.type === 'chat') {
    // 收到新消息，刷新未读数
    fetchUnreadCounts();
  }
}

let unreadInterval: ReturnType<typeof setInterval> | null = null;

onMounted(() => {
  fetchUnreadCounts();
  setupWebSocket();
  // 定期刷新未读数
  unreadInterval = setInterval(fetchUnreadCounts, 30000); // 每30秒刷新一次
});

onUnmounted(() => {
  if (unreadInterval) {
    clearInterval(unreadInterval);
  }
});
</script>

<style scoped>
.user-chat {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-tabs {
  display: flex;
  background: white;
  border-bottom: 2px solid #e0e0e0;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 20px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  gap: 8px;
}

.tab-item:hover {
  background: #f5f5f5;
}

.tab-item.active {
  color: #72C1BB;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #72C1BB;
}

.tab-icon {
  font-size: 18px;
}

.tab-label {
  font-size: 16px;
}

.tab-badge {
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
  margin-left: 4px;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
}

.tab-panel {
  height: 100%;
}

@media (max-width: 767px) {
  .chat-tabs {
    padding: 0 10px;
  }

  .tab-item {
    padding: 12px 10px;
  }

  .tab-label {
    font-size: 14px;
  }
}
</style>
