<template>
  <div 
    class="message-bubble" 
    :class="{ 'is-sender': isSender, 'is-receiver': !isSender }"
    @contextmenu.prevent="handleContextMenu"
    @touchstart="handleTouchStart"
    @touchend="handleTouchEnd"
  >
    <div class="message-avatar">
      <img v-if="avatar" :src="avatar" :alt="name" />
      <div v-else class="avatar-placeholder">{{ name?.charAt(0) || 'U' }}</div>
    </div>
    <div class="message-content-wrapper">
      <div class="message-name" v-if="!isSender">{{ name }}</div>
      <div 
        class="message-bubble-content" 
        :class="`message-type-${messageType}`"
        :style="{ opacity: isRevoked ? 0.5 : 1 }"
      >
        <!-- 已撤回消息 -->
        <div v-if="isRevoked" class="message-revoked">
          <span class="revoked-icon">🗑️</span>
          <span class="revoked-text">{{ isSender ? '你' : name }}撤回了一条消息</span>
        </div>
        <!-- 文字消息 -->
        <div v-else-if="messageType === 'text'" class="message-text">{{ content }}</div>
        <!-- 表情消息 -->
        <div v-else-if="messageType === 'emoji'" class="message-emoji">{{ content }}</div>
        <!-- 图片消息 -->
        <div v-else-if="messageType === 'image'" class="message-image">
          <img 
            :src="getImageUrl(content)" 
            :alt="content" 
            @click="previewImage"
            @error="handleImageError"
            loading="lazy"
          />
          <div v-if="imageLoadError" class="image-error">
            <span class="error-icon">🖼️</span>
            <span class="error-text">图片加载失败</span>
          </div>
        </div>
        <!-- 文件消息 -->
        <div v-else-if="messageType === 'file'" class="message-file">
          <div class="file-icon">📎</div>
          <div class="file-name">{{ getFileName(content) }}</div>
        </div>
      </div>
      <div class="message-time">{{ formatTime(time) }}</div>
    </div>
    <!-- 右键菜单 -->
    <div v-if="showContextMenu && canRevoke" class="context-menu" :style="contextMenuStyle" @click.stop>
      <div class="menu-item" @click="handleRevoke">
        <span class="menu-icon">🗑️</span>
        <span>撤回</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps<{
  content: string;
  messageType: string;
  time: string;
  isSender: boolean;
  name?: string;
  avatar?: string;
  showAvatar?: boolean;
  showName?: boolean;
  messageId?: number;
  isRevoked?: boolean;
}>();

const emit = defineEmits<{
  revoke: [messageId: number];
}>();

const showContextMenu = ref(false);
const contextMenuStyle = ref({ top: '0px', left: '0px' });
const touchStartTime = ref(0);
const imageLoadError = ref(false);

// 计算是否可以撤回（3分钟内且是自己发送的）
const canRevoke = computed(() => {
  if (!props.isSender || !props.messageId || props.isRevoked) {
    return false;
  }
  if (!props.time) return false;
  const messageTime = new Date(props.time).getTime();
  const now = Date.now();
  const diff = now - messageTime;
  // 3分钟 = 180000毫秒
  return diff <= 180000;
});

function handleContextMenu(event: MouseEvent) {
  if (!canRevoke.value) return;
  event.preventDefault();
  showContextMenu.value = true;
  contextMenuStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  };
  // 点击其他地方关闭菜单
  setTimeout(() => {
    document.addEventListener('click', closeContextMenu, { once: true });
  }, 0);
}

function handleTouchStart() {
  if (!canRevoke.value) return;
  touchStartTime.value = Date.now();
}

function handleTouchEnd(event: TouchEvent) {
  if (!canRevoke.value) return;
  const touchDuration = Date.now() - touchStartTime.value;
  // 长按超过500ms显示菜单
  if (touchDuration > 500) {
    const touch = event.changedTouches[0];
    showContextMenu.value = true;
    contextMenuStyle.value = {
      top: `${touch.clientY}px`,
      left: `${touch.clientX}px`
    };
    // 点击其他地方关闭菜单
    setTimeout(() => {
      document.addEventListener('touchstart', closeContextMenu, { once: true });
    }, 0);
  }
}

function closeContextMenu() {
  showContextMenu.value = false;
}

function handleRevoke() {
  if (props.messageId && canRevoke.value) {
    emit('revoke', props.messageId);
    closeContextMenu();
  }
}

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu);
  document.removeEventListener('touchstart', closeContextMenu);
});

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

function getImageUrl(path: string): string {
  if (!path) return '';
  // 如果已经是完整的 URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  // 如果已经以 /file/ 开头，直接返回（避免重复添加）
  if (path.startsWith('/file/')) {
    return path;
  }
  // 如果路径不包含 /file/，添加前缀
  // 处理可能的情况：只有文件名（如 "xxx.jpg"）或相对路径
  return `/file/${path}`;
}

function previewImage() {
  // 图片预览功能
  const img = new Image();
  img.src = getImageUrl(props.content);
  window.open(img.src, '_blank');
}

function getFileName(path: string): string {
  if (!path) return '未知文件';
  const parts = path.split('/');
  return parts[parts.length - 1] || '未知文件';
}

function handleImageError(event: Event) {
  console.error('图片加载失败:', props.content);
  imageLoadError.value = true;
  const img = event.target as HTMLImageElement;
  if (img) {
    img.style.display = 'none';
  }
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.message-bubble.is-sender {
  flex-direction: row-reverse;
}

.message-bubble.is-receiver {
  flex-direction: row;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  margin: 0 8px;
}

.message-avatar img {
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
  font-size: 18px;
  font-weight: 600;
}

.message-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding: 0 8px;
}

.message-bubble-content {
  padding: 10px 14px;
  border-radius: 12px;
  word-wrap: break-word;
  word-break: break-all;
}

.message-bubble.is-sender .message-bubble-content {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bubble.is-receiver .message-bubble-content {
  background: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-emoji {
  font-size: 32px;
  line-height: 1;
}

.message-image {
  max-width: 200px;
  max-height: 300px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-image img {
  max-width: 100%;
  max-height: 300px;
  width: auto;
  height: auto;
  display: block;
  cursor: pointer;
  transition: opacity 0.2s;
  object-fit: contain;
}

.message-image img:hover {
  opacity: 0.9;
}

.message-image img[src=""],
.message-image img:not([src]) {
  display: none;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #999;
  font-size: 12px;
  gap: 8px;
}

.error-icon {
  font-size: 32px;
  opacity: 0.5;
}

.error-text {
  font-size: 12px;
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 24px;
}

.file-name {
  font-size: 14px;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  padding: 0 8px;
  text-align: right;
}

.message-bubble.is-receiver .message-time {
  text-align: left;
}

.message-revoked {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 13px;
  font-style: italic;
}

.revoked-icon {
  font-size: 14px;
}

.revoked-text {
  flex: 1;
}

.context-menu {
  position: fixed;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  z-index: 1000;
  min-width: 120px;
}

.menu-item {
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-icon {
  font-size: 16px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .message-content-wrapper {
    max-width: 80%;
  }

  .message-avatar {
    width: 32px;
    height: 32px;
    margin: 0 6px;
  }
  
  .message-avatar img {
    border-radius: 50%;
  }
  
  .avatar-placeholder {
    border-radius: 50%;
  }

  .message-bubble-content {
    padding: 8px 12px;
  }

  .message-text {
    font-size: 13px;
  }

  .message-emoji {
    font-size: 28px;
  }

  .message-image {
    max-width: 150px;
  }
}
</style>
