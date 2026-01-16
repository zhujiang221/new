<template>
  <div class="admin-chat-manage">
    <div class="page-header">
      <h1>💬 聊天管理</h1>
      <p>管理系统中的所有聊天申请、会话和消息</p>
    </div>

    <div class="manage-tabs">
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'requests' }"
        @click="currentTab = 'requests'"
      >
        申请记录
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'sessions' }"
        @click="currentTab = 'sessions'"
      >
        聊天会话
      </button>
      <button
        class="tab-btn"
        :class="{ active: currentTab === 'messages' }"
        @click="currentTab = 'messages'"
      >
        聊天消息
      </button>
    </div>

    <!-- 申请记录 -->
    <div v-if="currentTab === 'requests'" class="manage-content">
      <div class="search-bar">
        <input
          v-model="requestFilters.search"
          type="text"
          placeholder="搜索用户或医生..."
          class="search-input"
        />
        <select v-model="requestFilters.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已同意</option>
          <option value="2">已拒绝</option>
        </select>
        <button class="btn btn-primary" @click="loadRequests">查询</button>
      </div>

      <div v-if="requestLoading" class="loading">加载中...</div>
      <div v-else-if="requestList.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无申请记录</div>
      </div>
      <div v-else class="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>医生</th>
              <th>申请留言</th>
              <th>状态</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in requestList" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.userName || '未知' }}</td>
              <td>{{ item.doctorName || '未知' }}</td>
              <td>{{ item.requestMessage || '-' }}</td>
              <td>
                <span :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
              </td>
              <td>{{ formatTime(item.createTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 聊天会话 -->
    <div v-if="currentTab === 'sessions'" class="manage-content">
      <div class="search-bar">
        <input
          v-model="sessionFilters.search"
          type="text"
          placeholder="搜索用户或医生..."
          class="search-input"
        />
        <select v-model="sessionFilters.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="0">已关闭</option>
          <option value="1">进行中</option>
        </select>
        <button class="btn btn-primary" @click="loadSessions">查询</button>
      </div>

      <div v-if="sessionLoading" class="loading">加载中...</div>
      <div v-else-if="sessionList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">暂无会话记录</div>
      </div>
      <div v-else class="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>医生</th>
              <th>状态</th>
              <th>最后消息时间</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in sessionList" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.userName || '未知' }}</td>
              <td>{{ item.doctorName || '未知' }}</td>
              <td>
                <span :class="item.status === 1 ? 'status-active' : 'status-closed'">
                  {{ item.status === 1 ? '进行中' : '已关闭' }}
                </span>
              </td>
              <td>{{ formatTime(item.lastMessageTime) }}</td>
              <td>{{ formatTime(item.createTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 聊天消息 -->
    <div v-if="currentTab === 'messages'" class="manage-content">
      <div class="search-bar">
        <input
          v-model="messageFilters.search"
          type="text"
          placeholder="搜索消息内容..."
          class="search-input"
        />
        <select v-model="messageFilters.messageType" class="filter-select">
          <option value="">全部类型</option>
          <option value="text">文字</option>
          <option value="emoji">表情</option>
          <option value="image">图片</option>
          <option value="file">文件</option>
        </select>
        <button class="btn btn-primary" @click="loadMessages">查询</button>
      </div>

      <div v-if="messageLoading" class="loading">加载中...</div>
      <div v-else-if="messageList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">暂无消息记录</div>
      </div>
      <div v-else class="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>会话ID</th>
              <th>发送者</th>
              <th>接收者</th>
              <th>类型</th>
              <th>内容</th>
              <th>已读</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in messageList" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.sessionId }}</td>
              <td>{{ item.senderName || '未知' }}</td>
              <td>{{ item.receiverName || '未知' }}</td>
              <td>{{ item.messageType }}</td>
              <td class="message-content-cell">
                <span v-if="item.messageType === 'image'">[图片]</span>
                <span v-else-if="item.messageType === 'emoji'">{{ item.content }}</span>
                <span v-else>{{ item.content?.substring(0, 50) || '-' }}{{ item.content && item.content.length > 50 ? '...' : '' }}</span>
              </td>
              <td>{{ item.isRead === 1 ? '已读' : '未读' }}</td>
              <td>{{ formatTime(item.createTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { getAllChatRequests, getAllChatSessions, getAllChatMessages, type ChatRequest, type ChatSession, type ChatMessage } from '../../api/chat';
import { showMessage } from '../../utils/message';

const currentTab = ref<'requests' | 'sessions' | 'messages'>('requests');

const requestLoading = ref(false);
const sessionLoading = ref(false);
const messageLoading = ref(false);

const requestList = ref<ChatRequest[]>([]);
const sessionList = ref<ChatSession[]>([]);
const messageList = ref<ChatMessage[]>([]);

const requestFilters = reactive({
  search: '',
  status: ''
});

const sessionFilters = reactive({
  search: '',
  status: ''
});

const messageFilters = reactive({
  search: '',
  messageType: ''
});

onMounted(() => {
  loadRequests();
});

async function loadRequests() {
  requestLoading.value = true;
  try {
    const result = await getAllChatRequests(1, 50, requestFilters);
    if (result && result.rows) {
      requestList.value = result.rows;
    } else {
      requestList.value = [];
    }
  } catch (e) {
    console.error('加载申请记录失败:', e);
    showMessage('加载申请记录失败', 'error');
  } finally {
    requestLoading.value = false;
  }
}

async function loadSessions() {
  sessionLoading.value = true;
  try {
    const result = await getAllChatSessions(1, 50, sessionFilters);
    if (result && result.rows) {
      sessionList.value = result.rows;
    } else {
      sessionList.value = [];
    }
  } catch (e) {
    console.error('加载会话记录失败:', e);
    showMessage('加载会话记录失败', 'error');
  } finally {
    sessionLoading.value = false;
  }
}

async function loadMessages() {
  messageLoading.value = true;
  try {
    const result = await getAllChatMessages(1, 50, messageFilters);
    if (result && result.rows) {
      messageList.value = result.rows;
    } else {
      messageList.value = [];
    }
  } catch (e) {
    console.error('加载消息记录失败:', e);
    showMessage('加载消息记录失败', 'error');
  } finally {
    messageLoading.value = false;
  }
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
  if (!timeStr) return '-';
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}
</script>

<style scoped>
.admin-chat-manage {
  padding: 20px;
  max-width: 1400px;
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

.manage-tabs {
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

.manage-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #72C1BB;
}

.filter-select {
  padding: 10px 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: #72C1BB;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
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

.empty-text {
  color: #666;
  font-size: 16px;
}

.data-table {
  overflow-x: auto;
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.data-table th {
  background: #f5f5f5;
  font-weight: 600;
  color: #333;
  position: sticky;
  top: 0;
}

.data-table tr:hover {
  background: #f9f9f9;
}

.message-content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pending {
  padding: 4px 12px;
  background: #fff7e6;
  color: #fa8c16;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-approved {
  padding: 4px 12px;
  background: #f6ffed;
  color: #52c41a;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-rejected {
  padding: 4px 12px;
  background: #fff1f0;
  color: #ff4d4f;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  padding: 4px 12px;
  background: #f6ffed;
  color: #52c41a;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-closed {
  padding: 4px 12px;
  background: #f5f5f5;
  color: #999;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
}

@media (max-width: 767px) {
  .admin-chat-manage {
    padding: 15px;
  }

  .manage-content {
    padding: 16px;
  }

  .data-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 8px;
  }
}
</style>
