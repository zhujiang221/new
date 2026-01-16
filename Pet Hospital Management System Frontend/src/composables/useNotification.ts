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
import { getToken } from '../utils/token';

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
      // 确保Token存在
      const token = getToken();
      if (!token) {
        console.warn('fetchUnreadCount: Token不存在，跳过获取');
        unreadCount.value = 0;
        return;
      }
      
      const resp = await http.get('/notification/unreadCount');
      if (typeof resp.data === 'number') {
        unreadCount.value = resp.data;
      } else {
        console.warn('fetchUnreadCount: 返回数据格式不正确:', resp.data);
        unreadCount.value = 0;
      }
    } catch (e) {
      console.error('获取未读消息数量失败:', e);
      unreadCount.value = 0;
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
      // 确保Token存在
      const token = getToken();
      if (!token) {
        console.warn('checkOnLogin: Token不存在，跳过检查');
        return {
          hasUnread: false,
          unreadCount: 0,
          message: '检查消息失败：未登录'
        };
      }
      
      const resp = await http.get('/notification/checkOnLogin');
      console.log('checkOnLogin响应:', resp.data);
      
      // 处理不同的响应格式
      let data = resp.data;
      
      // 如果返回的是字符串（可能是错误信息或HTML页面）
      if (typeof data === 'string') {
        // 检查是否是HTML页面（错误情况）
        if (data.trim().startsWith('<!DOCTYPE') || data.trim().startsWith('<html')) {
          console.error('checkOnLogin返回HTML页面，可能是Token无效或请求被重定向');
          return {
            hasUnread: false,
            unreadCount: 0,
            message: '检查消息失败：请求被重定向'
          };
        }
        
        // 如果是错误信息字符串
        if (data === 'NOT_LOGIN' || data === 'ERROR') {
          console.warn('checkOnLogin返回错误:', data);
          return {
            hasUnread: false,
            unreadCount: 0,
            message: '检查消息失败'
          };
        }
        
        // 其他字符串情况
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
    console.log('handleWebSocketMessage收到消息:', message);
    
    // 如果是聊天消息，不在这里处理，让聊天组件自己处理
    if (message.type === 'chat') {
      console.log('收到聊天消息，跳过通知消息处理');
      return;
    }
    
    // 检查消息类型和数据
    // 后端发送的格式：{"type": "notification", "data": NotificationMessage}
    // websocket.ts 的 handleMessage 会把整个 message 作为 data，所以实际结构是：
    // {type: "notification", data: {type: "notification", data: NotificationMessage}}
    let messageData: any = null;
    
    if (message.data) {
      // 如果 message.data 有 data 字段，说明是嵌套结构，需要提取内层的 data
      if (message.data.data && typeof message.data.data === 'object' && ('receiverId' in message.data.data || 'id' in message.data.data)) {
        messageData = message.data.data;
        console.log('从嵌套结构中提取消息数据:', messageData);
      } else if (message.data && typeof message.data === 'object' && ('receiverId' in message.data || 'id' in message.data)) {
        // 如果 message.data 本身就是 NotificationMessage 对象（有 receiverId 或 id 字段）
        messageData = message.data;
        console.log('直接使用 message.data 作为消息数据:', messageData);
      }
    }
    
    if (messageData) {
      console.log('最终解析到的消息数据:', messageData);
      
      // 检查是否是聊天消息（通过title判断）
      const messageTitle = messageData.title || '';
      if (messageTitle && (messageTitle.includes('聊天') && messageTitle !== '聊天申请')) {
        console.log('收到聊天消息，跳过通知消息处理');
        return;
      }
      
      // 收到新消息，增加未读数量（只处理通知消息）
      unreadCount.value++;
      
      // 将新消息添加到列表顶部（如果有ID）
      if (messageData.id) {
        const newMessage = messageData as NotificationMessage;
        // 检查是否已存在（避免重复添加）
        const exists = messageList.value.some(m => m.id === newMessage.id);
        if (!exists) {
          messageList.value.unshift(newMessage);
          console.log('新消息已添加到列表:', newMessage);
        }
      }
      
      // 触发新消息回调（用于显示弹窗）
      // 无论是否有ID，都应该显示弹窗
      console.log('检查回调函数，onNewMessageCallback:', onNewMessageCallback);
      if (onNewMessageCallback) {
        console.log('触发弹窗回调，消息数据:', messageData);
        try {
          onNewMessageCallback(messageData as NotificationMessage);
        } catch (e) {
          console.error('执行回调函数失败:', e);
        }
      } else {
        console.warn('onNewMessageCallback未设置，无法显示弹窗');
      }
    } else {
      console.warn('WebSocket消息格式不正确，无法解析消息数据:', message);
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
