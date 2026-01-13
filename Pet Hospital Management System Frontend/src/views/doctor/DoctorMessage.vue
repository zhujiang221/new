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
      <div v-else-if="displayMessages.length === 0 && appointmentNotifications.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-title">暂无消息</div>
        <div class="empty-desc">您还没有收到任何消息</div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="messages">
        <!-- 新预约通知汇总（置顶） -->
        <div
          v-if="appointmentNotifications.length > 0"
          class="message-item appointment-summary"
          :class="{ 'unread': hasUnreadAppointments }"
          @click="handleAppointmentSummaryClick"
        >
          <div class="message-header">
            <div class="message-title-wrapper">
              <div class="message-title">新预约通知</div>
              <div v-if="hasUnreadAppointments" class="red-dot"></div>
            </div>
            <div class="message-time">{{ formatTime(latestAppointmentTime) }}</div>
          </div>
          <div class="message-content">
            <div class="summary-info">
              <div class="summary-count">
                <span class="count-number">{{ appointmentNotifications.length }}</span>
                <span class="count-text">条新预约</span>
              </div>
              <div v-if="latestAppointment" class="summary-preview">
                <div class="preview-text">
                  <span class="preview-label">最新：</span>
                  <span class="preview-content">{{ getPreviewText(latestAppointment) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="hasUnreadAppointments" class="unread-badge">未读</div>
        </div>

        <!-- 其他消息 -->
        <div
          v-for="message in displayMessages"
          :key="message.id"
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

    <!-- 新预约通知对话框 -->
    <div v-if="showAppointmentDialog" class="modal-overlay" @click.self="showAppointmentDialog = false">
      <div class="appointment-dialog">
        <div class="dialog-header">
          <h3>新预约通知</h3>
          <button class="dialog-close" @click="showAppointmentDialog = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div v-if="appointmentNotifications.length === 0" class="empty-dialog">
            <div class="empty-icon">📋</div>
            <div class="empty-title">暂无预约通知</div>
          </div>
          <div v-else class="appointment-list">
            <div
              v-for="message in appointmentNotifications"
              :key="message.id"
              class="appointment-item"
              :class="{ 'unread': message.isRead === 0 }"
              @click="handleAppointmentItemClick(message)"
            >
              <div class="appointment-header">
                <div class="appointment-title-wrapper">
                  <div class="appointment-title">新预约通知</div>
                  <div v-if="message.isRead === 0" class="red-dot"></div>
                </div>
                <div class="appointment-time">{{ formatTime(message.createTime) }}</div>
              </div>
              <div class="appointment-content">
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Loading } from '@element-plus/icons-vue';
import { useNotification, type NotificationMessage, type NotificationContent } from '../../composables/useNotification';

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
const showAppointmentDialog = ref(false);
const isMobile = ref(false);

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

// 检查是否有未读的新预约通知
const hasUnreadAppointments = computed(() => {
  return appointmentNotifications.value.some(msg => msg.isRead === 0);
});

// 获取最新的预约通知
const latestAppointment = computed(() => {
  if (appointmentNotifications.value.length === 0) return null;
  return appointmentNotifications.value[0];
});

// 获取最新预约通知的时间
const latestAppointmentTime = computed(() => {
  if (appointmentNotifications.value.length === 0) return '';
  return appointmentNotifications.value[0].createTime;
});

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

// 处理新预约通知汇总点击
function handleAppointmentSummaryClick() {
  // 打开对话框
  showAppointmentDialog.value = true;
}

// 处理对话框内预约通知项点击
async function handleAppointmentItemClick(message: NotificationMessage) {
  // 如果未读，标记为已读
  if (message.isRead === 0) {
    await markAsRead([message.id]);
  }
  
  // 关闭对话框
  showAppointmentDialog.value = false;
  
  // 跳转到预约管理页面
  if (message.appointmentId) {
    router.push('/doctor/apply');
  }
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
    await fetchMessageList(1, 2000); // 移动端一次性拉取足够多的数据
  } else {
    await fetchMessageList(currentPage.value, pageSize.value);
  }
}

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', handleResize);
  loadMessages();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
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

/* 新预约通知汇总样式 */
.appointment-summary {
  background: linear-gradient(135deg, #f0f9f8 0%, #e8f5f3 100%);
  border-color: #72C1BB;
  border-width: 2px;
}

.appointment-summary.unread {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border-color: #ff4d4f;
  animation: pulse-border 2s infinite;
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

/* 红点指示器 */
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

/* 汇总信息样式 */
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

/* 分页样式 */
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

.appointment-dialog {
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

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.appointment-item {
  position: relative;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.appointment-item:hover {
  background: #f0f0f0;
  border-color: #72C1BB;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.1);
}

.appointment-item.unread {
  background: #fff;
  border-color: #72C1BB;
  border-width: 2px;
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.appointment-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.appointment-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.appointment-time {
  font-size: 12px;
  color: #999;
}

.appointment-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
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

  .appointment-dialog {
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

  .appointment-item {
    padding: 12px;
  }

  .appointment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
