<template>
  <div class="doctor-chat-window">
    <div v-if="!sessionId" class="no-session">
      <div class="empty-icon">💬</div>
      <div class="empty-text">请选择一个聊天会话</div>
      <button class="btn btn-primary" @click="goToList">返回聊天列表</button>
    </div>

    <div v-else class="chat-container">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <button class="back-btn" @click="goToList">←</button>
        <div class="chat-title">
          <div class="title-name">{{ currentSession?.userName || '用户' }}</div>
          <div class="title-status">在线</div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="loading" class="loading-messages">加载中...</div>
        <div v-else-if="messages.length === 0" class="empty-messages">
          <div class="empty-icon">💬</div>
          <div class="empty-text">开始聊天吧</div>
        </div>
        <ChatMessageBubble
          v-for="message in messages"
          :key="message.id"
          :content="message.content"
          :message-type="message.messageType"
          :time="message.createTime || ''"
          :is-sender="message.senderId === Number(currentUserId)"
          :name="message.senderId === Number(currentUserId) ? currentUserInfo.name : otherUserInfo.name"
          :avatar="message.senderId === Number(currentUserId) ? currentUserInfo.img : otherUserInfo.img"
          :message-id="message.id"
          :is-revoked="message.isRevoked === 1"
          @revoke="handleRevokeMessage"
        />
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-toolbar">
          <button class="tool-btn" @click="triggerImageUpload" title="图片">
            📷
          </button>
        </div>
        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleImageSelect"
        />
        <div class="input-wrapper">
          <textarea
            v-model="inputText"
            ref="inputTextarea"
            class="chat-input"
            placeholder="输入消息..."
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact="insertNewline"
            @input="autoResize"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="!canSend">
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ChatMessageBubble from '../../components/ChatMessageBubble.vue';
import { getChatSession, getChatMessageList, sendChatMessage, markChatMessageRead, revokeChatMessage, type ChatMessage, type ChatSession, isChatMessage } from '../../api/chat';
import { useChatStore } from '../../composables/useChatStore';
import { uploadChatImage } from '../../api/chat';
import { websocketManager, type WebSocketMessage } from '../../utils/websocket';
import { getUserInfo } from '../../utils/user';
import { showMessage } from '../../utils/message';

const route = useRoute();
const router = useRouter();

// 使用全局聊天状态管理
const chatStore = useChatStore();

const sessionId = ref<number | null>(null);
const currentSession = ref<ChatSession | null>(null);
// 使用computed从全局状态获取消息，确保响应式
const messages = computed(() => {
  if (sessionId.value) {
    return chatStore.getSessionMessages(sessionId.value);
  }
  return [];
});
const loading = ref(false);
const inputText = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const inputTextarea = ref<HTMLTextAreaElement | null>(null);
const imageInput = ref<HTMLInputElement | null>(null);
const currentUserId = ref<string>('');
const uploadingImage = ref(false);
const currentUserInfo = ref<{ name?: string; img?: string }>({});
const otherUserInfo = ref<{ name?: string; img?: string }>({});

const canSend = computed(() => {
  return inputText.value.trim().length > 0 || uploadingImage.value;
});

onMounted(() => {
  const userInfo = getUserInfo();
  if (userInfo) {
    currentUserId.value = String(userInfo.id);
    currentUserInfo.value = {
      name: userInfo.name || '医生',
      img: (userInfo as any).img
    };
  }

  const id = route.params.id;
  if (id) {
    const sessionIdNum = Number(id);
    if (isNaN(sessionIdNum)) {
      console.error('无效的会话ID:', id);
      router.push('/doctor/chat');
      return;
    }
    sessionId.value = sessionIdNum;
    // 设置当前活跃会话
    chatStore.setActiveSession(sessionIdNum);
    loadSession();
    loadMessages();
    setupWebSocket();
    // 启动定时同步（兜底方案）
    chatStore.startPeriodicSync(30000); // 30秒同步一次
  } else {
    // 如果没有会话ID，返回聊天列表
    router.push('/doctor/chat');
  }
});

onUnmounted(() => {
  // 移除WebSocket消息处理器
  websocketManager.removeMessageHandler(handleWebSocketMessage);
  if (sessionId.value) {
    markChatMessageRead(sessionId.value).catch(() => {});
  }
});

watch(() => route.params.id, (newId) => {
  if (newId) {
    sessionId.value = Number(newId);
    loadSession();
    loadMessages();
  }
});

function setupWebSocket() {
  const userInfo = getUserInfo();
  if (userInfo && userInfo.id) {
    // 确保WebSocket已连接并添加消息处理器
    if (!websocketManager.isConnected()) {
      websocketManager.connect(userInfo.id, handleWebSocketMessage);
    } else {
      // 如果已连接，直接添加消息处理器
      websocketManager.addMessageHandler(handleWebSocketMessage);
    }
  }
}

function handleWebSocketMessage(message: WebSocketMessage) {
  console.log('DoctorChatWindow收到WebSocket消息:', message);
  
  if (message.type === 'chat') {
    // 处理嵌套格式：message.data可能是ChatMessage，也可能是{type: "chat", data: ChatMessage}
    let chatMessage: ChatMessage | null = null;
    
    if (message.data) {
      // 检查是否是嵌套格式
      if (message.data.type === 'chat' && message.data.data) {
        chatMessage = message.data.data as ChatMessage;
        console.log('从嵌套结构中提取聊天消息:', chatMessage);
      } else if (message.data.sessionId !== undefined) {
        // 直接是ChatMessage对象
        chatMessage = message.data as ChatMessage;
        console.log('直接使用message.data作为聊天消息:', chatMessage);
      }
    }
    
    if (chatMessage) {
      // 检查消息是否属于当前会话
      if (chatMessage.sessionId === sessionId.value) {
        console.log('消息属于当前会话，会话ID:', sessionId.value);
        // 检查是否是撤回消息
        if (chatMessage.isRevoked === 1) {
          // 更新本地消息列表
          const index = messages.value.findIndex(m => m.id === chatMessage!.id);
          if (index !== -1) {
            messages.value[index].isRevoked = 1;
            console.log('消息已撤回，ID:', chatMessage.id);
          }
        } else {
          // 检查消息是否已存在（避免重复添加）
          const existingIndex = messages.value.findIndex(m => m.id === chatMessage!.id);
          if (existingIndex === -1) {
            console.log('添加新消息到列表，消息ID:', chatMessage.id);
            messages.value.push(chatMessage);
            scrollToBottom();
            if (chatMessage.receiverId === Number(currentUserId.value)) {
              markChatMessageRead(sessionId.value!).catch(() => {});
            }
          } else {
            console.log('消息已存在，跳过添加，消息ID:', chatMessage.id);
          }
        }
      } else {
        console.log('消息不属于当前会话，当前会话ID:', sessionId.value, '消息会话ID:', chatMessage.sessionId);
      }
    } else {
      console.warn('无法提取聊天消息，message.data:', message.data);
    }
  }
}

async function loadSession() {
  if (!sessionId.value) return;
  try {
    const session = await getChatSession(sessionId.value);
    currentSession.value = session;
    // 设置对方（用户）信息
    otherUserInfo.value = {
      name: session.userName || '用户',
      img: session.userImg
    };
  } catch (e) {
    console.error('加载会话失败:', e);
  }
}

async function loadMessages() {
  if (!sessionId.value) return;
  loading.value = true;
  try {
    console.log('[DoctorChatWindow] 加载消息，会话ID:', sessionId.value);
    const list = await getChatMessageList(sessionId.value, 1, 100);
    
    // 使用类型守卫验证消息列表
    const validMessages = (list || []).filter(msg => {
      if (isChatMessage(msg)) {
        return true;
      } else {
        console.warn('[DoctorChatWindow] 无效的消息格式，已过滤:', msg);
        return false;
      }
    });
    
    // 使用全局状态管理设置消息列表
    chatStore.setSessionMessages(sessionId.value, validMessages);
    
    await nextTick();
    scrollToBottom();
    await markChatMessageRead(sessionId.value);
    console.log('[DoctorChatWindow] 消息加载完成，消息数:', validMessages.length);
  } catch (e) {
    console.error('[DoctorChatWindow] 加载消息失败:', e);
  } finally {
    loading.value = false;
  }
}

async function sendMessage() {
  if (!sessionId.value || !canSend.value) return;

  const text = inputText.value.trim();
  if (!text && !uploadingImage.value) return;

  if (uploadingImage.value) {
    return;
  }

  try {
    console.log('[DoctorChatWindow] 发送消息，会话ID:', sessionId.value, '内容:', text);
    
    // 发送消息
    const sentMessage = await sendChatMessage(sessionId.value, 'text', text);
    
    // 使用类型守卫验证返回的消息
    if (isChatMessage(sentMessage)) {
      // 使用全局状态管理添加消息
      chatStore.addMessage(sessionId.value, sentMessage);
      
      // 添加到待确认队列（如果需要确认机制）
      if (sentMessage.id) {
        chatStore.addPendingMessage(sentMessage.id, sentMessage);
      }
      
      await nextTick();
      scrollToBottom();
      console.log('[DoctorChatWindow] 消息已发送并添加到列表，消息ID:', sentMessage.id);
    } else {
      console.warn('[DoctorChatWindow] 返回的消息格式无效:', sentMessage);
    }
    
    inputText.value = '';
    if (inputTextarea.value) {
      inputTextarea.value.style.height = 'auto';
    }
  } catch (e: any) {
    console.error('[DoctorChatWindow] 发送消息失败:', e);
    showMessage(e.message || '发送消息失败', 'error');
  }
}

function triggerImageUpload() {
  imageInput.value?.click();
}

async function handleImageSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    showMessage('请选择图片文件', 'warning');
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    showMessage('图片大小不能超过5MB', 'warning');
    return;
  }

  uploadingImage.value = true;
  try {
    console.log('[DoctorChatWindow] 上传图片，会话ID:', sessionId.value);
    const imageUrl = await uploadChatImage(file);
    const sentMessage = await sendChatMessage(sessionId.value!, 'image', imageUrl);
    
    // 使用类型守卫验证返回的消息
    if (isChatMessage(sentMessage)) {
      // 使用全局状态管理添加消息
      chatStore.addMessage(sessionId.value!, sentMessage);
      
      // 添加到待确认队列
      if (sentMessage.id) {
        chatStore.addPendingMessage(sentMessage.id, sentMessage);
      }
      
      await nextTick();
      scrollToBottom();
      console.log('[DoctorChatWindow] 图片已发送并添加到列表，消息ID:', sentMessage.id);
    } else {
      console.warn('[DoctorChatWindow] 返回的消息格式无效:', sentMessage);
    }
    
    showMessage('图片发送成功', 'success');
  } catch (e: any) {
    console.error('上传图片失败:', e);
    showMessage(e.message || '上传图片失败', 'error');
  } finally {
    uploadingImage.value = false;
    if (target) {
      target.value = '';
    }
  }
}

function insertNewline() {
  inputText.value += '\n';
}

function autoResize() {
  if (inputTextarea.value) {
    inputTextarea.value.style.height = 'auto';
    inputTextarea.value.style.height = inputTextarea.value.scrollHeight + 'px';
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}

function goToList() {
  router.push('/doctor/chat');
}

// 处理撤回消息
async function handleRevokeMessage(messageId: number) {
  try {
    const result = await revokeChatMessage(messageId);
    if (result === 'SUCCESS') {
      // 更新本地消息列表，标记为已撤回
      const message = messages.value.find(m => m.id === messageId);
      if (message) {
        message.isRevoked = 1;
      }
      showMessage('消息已撤回', 'success');
    } else {
      showMessage(result.includes('ERROR') ? result : '撤回失败', 'error');
    }
  } catch (e: any) {
    console.error('撤回消息失败:', e);
    showMessage(e.message || '撤回消息失败', 'error');
  }
}
</script>

<style scoped>
.doctor-chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
}

.no-session {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  color: #666;
  font-size: 16px;
  margin-bottom: 20px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 8px;
  margin-right: 12px;
  border-radius: 4px;
  transition: background 0.2s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-title {
  flex: 1;
}

.title-name {
  font-size: 16px;
  font-weight: 600;
}

.title-status {
  font-size: 12px;
  opacity: 0.9;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
}

.loading-messages,
.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.chat-input-area {
  position: relative;
  padding: 12px;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: #e0e0e0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.chat-input {
  flex: 1;
  padding: 10px 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  max-height: 120px;
  font-family: inherit;
}

.chat-input:focus {
  outline: none;
  border-color: #72C1BB;
}

.send-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn {
  padding: 10px 24px;
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
  transform: translateY(-1px);
}

@media (max-width: 767px) {
  .chat-header {
    padding: 10px 12px;
  }

  .chat-messages {
    padding: 12px;
  }

  .chat-input-area {
    padding: 10px;
  }
}
</style>
