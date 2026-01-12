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
      <div v-else-if="messageList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-title">暂无消息</div>
        <div class="empty-desc">您还没有收到任何消息</div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="messages">
        <div
          v-for="message in messageList"
          :key="message.id"
          class="message-item"
          :class="{ 'unread': message.isRead === 0 }"
          @click="handleMessageClick(message)"
        >
          <div class="message-header">
            <div class="message-title">{{ message.title }}</div>
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
      </div>

      <!-- 分页 -->
      <div v-if="messageList.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Loading } from '@element-plus/icons-vue';
import { useNotification, type NotificationMessage, type NotificationContent } from '../../composables/useNotification';
import { showMessage } from '../../utils/message';

const router = useRouter();
const {
  messageList,
  isLoading,
  total,
  fetchMessageList,
  markAsRead,
  parseMessageContent
} = useNotification();

const currentPage = ref(1);
const pageSize = ref(10);

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

// 格式化时间
function formatTime(timeStr: string): string {
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

// 处理消息点击
async function handleMessageClick(message: NotificationMessage) {
  // 如果未读，标记为已读
  if (message.isRead === 0) {
    await markAsRead([message.id]);
  }
  
  // 跳转到预约管理页面
  if (message.appointmentId) {
    router.push('/doctor/apply');
  }
}

// 处理分页大小变化
function handleSizeChange(size: number) {
  pageSize.value = size;
  currentPage.value = 1;
  loadMessages();
}

// 处理页码变化
function handlePageChange(page: number) {
  currentPage.value = page;
  loadMessages();
}

// 加载消息列表
async function loadMessages() {
  await fetchMessageList(currentPage.value, pageSize.value);
}

onMounted(() => {
  loadMessages();
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

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

  .detail-item {
    flex-direction: column;
    gap: 4px;
  }

  .detail-item .label {
    min-width: auto;
  }
}
</style>
