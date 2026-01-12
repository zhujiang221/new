/**
 * 消息管理 Composable
 * 统一管理消息状态（未读数量、消息列表）
 * 提供获取未读数量、刷新消息列表、标记已读等方法
 * 监听WebSocket消息并更新状态
 */

import { ref, onMounted, onUnmounted } from 'vue';
import http from '../api/http';
import { websocketManager, type WebSocketMessage } from '../utils/websocket';
import { getUserInfo } from '../utils/user';

export interface NotificationMessage {
  id: number;
  receiverId: number;
  senderId: number;
  appointmentId: number;
  type: string;
  title: string;
  content: string;
  isRead: number;
  createTime: string;
}

export interface NotificationContent {
  userName: string;
  appDate: string;
  timeSlot: string;
  appointmentTypeName: string;
  info?: string;
}

// 全局状态
const unreadCount = ref(0);
const messageList = ref<NotificationMessage[]>([]);
const isLoading = ref(false);
const total = ref(0);

// 新消息到达时的回调函数（用于显示弹窗）
let onNewMessageCallback: ((message: NotificationMessage) => void) | null = null;

/**
 * 使用消息通知功能
 */
export function useNotification() {
  const userInfo = getUserInfo();

  /**
   * 获取未读消息数量
   */
  async function fetchUnreadCount() {
    try {
      const resp = await http.get('/notification/unreadCount');
      if (typeof resp.data === 'number') {
        unreadCount.value = resp.data;
      }
    } catch (e) {
      console.error('获取未读消息数量失败:', e);
    }
  }

  /**
   * 获取消息列表
   */
  async function fetchMessageList(page: number = 1, limit: number = 10) {
    if (isLoading.value) return;
    
    isLoading.value = true;
    try {
      const resp = await http.get('/notification/list', {
        params: {
          page,
          limit
        }
      });
      
      if (resp.data && resp.data.rows) {
        if (page === 1) {
          messageList.value = resp.data.rows;
        } else {
          messageList.value.push(...resp.data.rows);
        }
        // 更新总数
        if (resp.data.total !== undefined) {
          total.value = resp.data.total;
        }
      }
    } catch (e) {
      console.error('获取消息列表失败:', e);
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * 标记消息为已读
   */
  async function markAsRead(ids?: number[]) {
    try {
      const params: any = {};
      if (ids && ids.length > 0) {
        params.ids = ids;
      }
      await http.post('/notification/markRead', params);
      // 更新本地状态
      if (ids && ids.length > 0) {
        ids.forEach(id => {
          const msg = messageList.value.find(m => m.id === id);
          if (msg) {
            msg.isRead = 1;
          }
        });
      } else {
        // 全部标记为已读
        messageList.value.forEach(msg => {
          msg.isRead = 1;
        });
      }
      // 刷新未读数量
      await fetchUnreadCount();
    } catch (e) {
      console.error('标记消息已读失败:', e);
    }
  }

  /**
   * 登录时检查未读消息
   */
  async function checkOnLogin() {
    try {
      const resp = await http.get('/notification/checkOnLogin');
      console.log('checkOnLogin响应:', resp.data);
      
      // 处理不同的响应格式
      let data = resp.data;
      // 如果返回的是字符串（错误情况），直接返回
      if (typeof data === 'string') {
        console.warn('checkOnLogin返回字符串:', data);
        return {
          hasUnread: false,
          unreadCount: 0,
          message: '检查消息失败'
        };
      }
      
      // 检查是否有未读消息
      const hasUnread = data && (data.hasUnread === true || (data.unreadCount && data.unreadCount > 0));
      const unreadCount = data?.unreadCount || 0;
      const message = data?.message || '暂无新消息';
      
      if (hasUnread) {
        // 刷新未读数量
        await fetchUnreadCount();
        console.log('发现未读消息:', { unreadCount, message });
        return {
          hasUnread: true,
          unreadCount: unreadCount,
          message: message
        };
      }
      
      return {
        hasUnread: false,
        unreadCount: 0,
        message: '暂无新消息'
      };
    } catch (e) {
      console.error('登录检查未读消息失败:', e);
      return {
        hasUnread: false,
        unreadCount: 0,
        message: '检查消息失败'
      };
    }
  }

  /**
   * 处理WebSocket消息
   */
  function handleWebSocketMessage(message: WebSocketMessage) {
    if (message.type === 'notification' || message.data) {
      // 收到新消息，增加未读数量
      unreadCount.value++;
      // 如果有消息列表，将新消息添加到列表顶部
      if (message.data && message.data.id) {
        const newMessage = message.data as NotificationMessage;
        messageList.value.unshift(newMessage);
        
        // 触发新消息回调（用于显示弹窗）
        if (onNewMessageCallback) {
          console.log('收到新消息，触发弹窗回调:', newMessage);
          onNewMessageCallback(newMessage);
        }
      }
    }
  }
  
  /**
   * 设置新消息到达时的回调函数（用于显示弹窗）
   */
  function setOnNewMessageCallback(callback: (message: NotificationMessage) => void) {
    onNewMessageCallback = callback;
  }
  
  /**
   * 清除新消息回调
   */
  function clearOnNewMessageCallback() {
    onNewMessageCallback = null;
  }

  /**
   * 初始化WebSocket连接
   */
  function initWebSocket() {
    if (!userInfo || !userInfo.id) {
      console.warn('用户未登录，无法建立WebSocket连接');
      return;
    }

    // 连接WebSocket
    websocketManager.connect(userInfo.id, handleWebSocketMessage);
  }

  /**
   * 断开WebSocket连接
   */
  function disconnectWebSocket() {
    websocketManager.disconnect();
  }

  /**
   * 解析消息内容
   */
  function parseMessageContent(content: string): NotificationContent | null {
    try {
      return JSON.parse(content);
    } catch (e) {
      console.error('解析消息内容失败:', e);
      return null;
    }
  }

  return {
    // 状态
    unreadCount,
    messageList,
    isLoading,
    total,
    // 方法
    fetchUnreadCount,
    fetchMessageList,
    markAsRead,
    checkOnLogin,
    initWebSocket,
    disconnectWebSocket,
    parseMessageContent,
    setOnNewMessageCallback,
    clearOnNewMessageCallback
  };
}
