/**
 * 全局聊天状态管理 Composable
 * 统一管理聊天会话和消息状态，确保数据流转链路完整
 */
import { ref, reactive, computed } from 'vue';
import type { ChatSession, ChatMessage } from '../api/chat';

// 全局消息存储：按会话ID组织消息
const sessionMessagesMap = reactive<Map<number, ChatMessage[]>>(new Map());

// 全局会话列表
const sessionList = ref<ChatSession[]>([]);

// 当前活跃会话ID
const activeSessionId = ref<number | null>(null);

// 消息发送确认队列：记录已发送但未确认的消息
const pendingMessages = reactive<Map<number, { message: ChatMessage; timestamp: number; retryCount: number }>>(new Map());

export function useChatStore() {
  /**
   * 获取指定会话的消息列表
   */
  const getSessionMessages = (sessionId: number): ChatMessage[] => {
    return sessionMessagesMap.get(sessionId) || [];
  };

  /**
   * 添加消息到指定会话
   */
  const addMessage = (sessionId: number, message: ChatMessage): void => {
    console.log(`[ChatStore] 添加消息到会话 ${sessionId}:`, message);
    
    if (!sessionMessagesMap.has(sessionId)) {
      sessionMessagesMap.set(sessionId, []);
    }
    
    const messages = sessionMessagesMap.get(sessionId)!;
    
    // 检查消息是否已存在（避免重复添加）
    const existingIndex = messages.findIndex(m => m.id === message.id);
    if (existingIndex === -1) {
      messages.push(message);
      // 按时间排序
      messages.sort((a, b) => {
        const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
        const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
        return timeA - timeB;
      });
      console.log(`[ChatStore] 消息已添加，会话 ${sessionId} 当前消息数:`, messages.length);
    } else {
      console.log(`[ChatStore] 消息已存在，跳过添加，消息ID:`, message.id);
    }
  };

  /**
   * 更新消息（用于撤回等操作）
   */
  const updateMessage = (sessionId: number, messageId: number, updates: Partial<ChatMessage>): void => {
    console.log(`[ChatStore] 更新消息，会话 ${sessionId}，消息ID ${messageId}:`, updates);
    
    const messages = sessionMessagesMap.get(sessionId);
    if (messages) {
      const index = messages.findIndex(m => m.id === messageId);
      if (index !== -1) {
        Object.assign(messages[index], updates);
        console.log(`[ChatStore] 消息已更新`);
      }
    }
  };

  /**
   * 设置会话消息列表
   */
  const setSessionMessages = (sessionId: number, messages: ChatMessage[]): void => {
    console.log(`[ChatStore] 设置会话 ${sessionId} 的消息列表，消息数:`, messages.length);
    sessionMessagesMap.set(sessionId, [...messages]);
  };

  /**
   * 清空指定会话的消息
   */
  const clearSessionMessages = (sessionId: number): void => {
    console.log(`[ChatStore] 清空会话 ${sessionId} 的消息`);
    sessionMessagesMap.delete(sessionId);
  };

  /**
   * 设置会话列表
   */
  const setSessionList = (sessions: ChatSession[]): void => {
    console.log(`[ChatStore] 设置会话列表，会话数:`, sessions.length);
    sessionList.value = [...sessions];
  };

  /**
   * 更新会话信息
   */
  const updateSession = (sessionId: number, updates: Partial<ChatSession>): void => {
    console.log(`[ChatStore] 更新会话 ${sessionId}:`, updates);
    const index = sessionList.value.findIndex(s => s.id === sessionId);
    if (index !== -1) {
      Object.assign(sessionList.value[index], updates);
    }
  };

  /**
   * 设置当前活跃会话
   */
  const setActiveSession = (sessionId: number | null): void => {
    console.log(`[ChatStore] 设置当前活跃会话:`, sessionId);
    activeSessionId.value = sessionId;
  };

  /**
   * 添加待确认消息（发送后等待服务端确认）
   */
  const addPendingMessage = (messageId: number, message: ChatMessage): void => {
    console.log(`[ChatStore] 添加待确认消息，消息ID:`, messageId);
    pendingMessages.set(messageId, {
      message,
      timestamp: Date.now(),
      retryCount: 0
    });
  };

  /**
   * 确认消息已收到（从待确认队列中移除）
   */
  const confirmMessage = (messageId: number): void => {
    console.log(`[ChatStore] 确认消息已收到，消息ID:`, messageId);
    pendingMessages.delete(messageId);
  };

  /**
   * 获取待确认消息
   */
  const getPendingMessages = (): Array<{ message: ChatMessage; timestamp: number; retryCount: number }> => {
    return Array.from(pendingMessages.values());
  };

  /**
   * 增加消息重试次数
   */
  const incrementRetryCount = (messageId: number): void => {
    const pending = pendingMessages.get(messageId);
    if (pending) {
      pending.retryCount++;
      console.log(`[ChatStore] 增加消息重试次数，消息ID: ${messageId}，重试次数: ${pending.retryCount}`);
    }
  };

  /**
   * 移除待确认消息（超过最大重试次数）
   */
  const removePendingMessage = (messageId: number): void => {
    console.log(`[ChatStore] 移除待确认消息，消息ID:`, messageId);
    pendingMessages.delete(messageId);
  };

  /**
   * 同步指定会话的消息（定时同步兜底方案）
   * 每隔固定时间拉取最新消息，确保离线消息同步
   */
  const syncSessionMessages = async (sessionId: number): Promise<void> => {
    try {
      console.log(`[ChatStore] 同步会话 ${sessionId} 的消息`);
      const list = await getChatMessageList(sessionId, 1, 100);
      
      if (Array.isArray(list) && list.length > 0) {
        // 合并消息，避免重复
        const existingMessages = sessionMessagesMap.get(sessionId) || [];
        const existingIds = new Set(existingMessages.map(m => m.id));
        
        const newMessages = list.filter(msg => msg.id && !existingIds.has(msg.id));
        
        if (newMessages.length > 0) {
          console.log(`[ChatStore] 发现 ${newMessages.length} 条新消息，会话ID: ${sessionId}`);
          const allMessages = [...existingMessages, ...newMessages];
          // 按时间排序
          allMessages.sort((a, b) => {
            const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
            const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
            return timeA - timeB;
          });
          sessionMessagesMap.set(sessionId, allMessages);
        }
      }
    } catch (e) {
      console.error(`[ChatStore] 同步会话 ${sessionId} 消息失败:`, e);
    }
  };

  /**
   * 启动定时同步（为所有活跃会话）
   */
  let syncTimer: NodeJS.Timeout | null = null;
  const startPeriodicSync = (interval: number = 30000): void => {
    // 30秒同步一次
    if (syncTimer) {
      clearInterval(syncTimer);
    }
    
    syncTimer = setInterval(() => {
      // 同步当前活跃会话
      if (activeSessionId.value) {
        syncSessionMessages(activeSessionId.value);
      }
      
      // 同步所有会话列表中的会话（可选，避免过多请求）
      // sessionList.value.forEach(session => {
      //   if (session.id) {
      //     syncSessionMessages(session.id);
      //   }
      // });
    }, interval);
    
    console.log(`[ChatStore] 启动定时同步，间隔: ${interval}ms`);
  };

  /**
   * 停止定时同步
   */
  const stopPeriodicSync = (): void => {
    if (syncTimer) {
      clearInterval(syncTimer);
      syncTimer = null;
      console.log('[ChatStore] 停止定时同步');
    }
  };

  return {
    // 状态
    sessionList: computed(() => sessionList.value),
    activeSessionId: computed(() => activeSessionId.value),
    
    // 方法
    getSessionMessages,
    addMessage,
    updateMessage,
    setSessionMessages,
    clearSessionMessages,
    setSessionList,
    updateSession,
    setActiveSession,
    addPendingMessage,
    confirmMessage,
    getPendingMessages,
    incrementRetryCount,
    removePendingMessage,
    syncSessionMessages,
    startPeriodicSync,
    stopPeriodicSync
  };
}
