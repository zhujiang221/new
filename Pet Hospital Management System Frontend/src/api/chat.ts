/**
 * 聊天相关API
 */
import http from './http';

// 类型定义
export interface ChatRequest {
  id?: number;
  userId: number;
  doctorId: number;
  status: number; // 0=待审核, 1=已同意, 2=已拒绝
  requestMessage?: string;
  createTime?: string;
  updateTime?: string;
  userName?: string;
  userImg?: string;
  doctorName?: string;
  doctorImg?: string;
}

export interface ChatSession {
  id?: number;
  userId: number;
  doctorId: number;
  requestId?: number;
  status: number; // 0=已关闭, 1=进行中
  lastMessageTime?: string;
  createTime?: string;
  userName?: string;
  userImg?: string;
  doctorName?: string;
  doctorImg?: string;
  unreadCount?: number;
  lastMessageContent?: string;
  lastMessageType?: string;
  lastMessageIsRevoked?: number; // 0=未撤回, 1=已撤回
  lastMessageSenderId?: number; // 最后一条消息的发送者ID
}

export interface ChatMessage {
  id?: number;
  sessionId: number;
  senderId: number;
  receiverId: number;
  messageType: string; // text, emoji, image, file
  content: string;
  isRead: number; // 0=未读, 1=已读
  isRevoked?: number; // 0=未撤回, 1=已撤回
  createTime?: string;
  senderName?: string;
  senderImg?: string;
  receiverName?: string;
  receiverImg?: string;
}

/**
 * 类型守卫：检查对象是否为有效的ChatMessage
 */
export function isChatMessage(obj: any): obj is ChatMessage {
  if (!obj || typeof obj !== 'object') {
    console.warn('[TypeGuard] 对象不是有效对象:', obj);
    return false;
  }
  
  const requiredFields = ['sessionId', 'senderId', 'receiverId', 'messageType', 'content', 'isRead'];
  const missingFields = requiredFields.filter(field => obj[field] === undefined);
  
  if (missingFields.length > 0) {
    console.warn('[TypeGuard] ChatMessage缺少必需字段:', missingFields, '对象:', obj);
    return false;
  }
  
  // 类型校验
  if (typeof obj.sessionId !== 'number' || 
      typeof obj.senderId !== 'number' || 
      typeof obj.receiverId !== 'number' ||
      typeof obj.messageType !== 'string' ||
      typeof obj.content !== 'string' ||
      typeof obj.isRead !== 'number') {
    console.warn('[TypeGuard] ChatMessage字段类型不正确:', obj);
    return false;
  }
  
  return true;
}

/**
 * 类型守卫：检查对象是否为有效的ChatSession
 */
export function isChatSession(obj: any): obj is ChatSession {
  if (!obj || typeof obj !== 'object') {
    console.warn('[TypeGuard] 对象不是有效对象:', obj);
    return false;
  }
  
  const requiredFields = ['userId', 'doctorId', 'status'];
  const missingFields = requiredFields.filter(field => obj[field] === undefined);
  
  if (missingFields.length > 0) {
    console.warn('[TypeGuard] ChatSession缺少必需字段:', missingFields, '对象:', obj);
    return false;
  }
  
  return true;
}

/**
 * 用户发起聊天申请
 */
export async function createChatRequest(doctorId: number, requestMessage?: string): Promise<string> {
  const resp = await http.post('/api/chat/request', {
    doctorId,
    requestMessage
  });
  return resp.data;
}

/**
 * 医生同意申请
 */
export async function approveChatRequest(id: number): Promise<string> {
  const resp = await http.put(`/api/chat/request/${id}/approve`);
  return resp.data;
}

/**
 * 医生拒绝申请
 */
export async function rejectChatRequest(id: number): Promise<string> {
  const resp = await http.put(`/api/chat/request/${id}/reject`);
  return resp.data;
}

/**
 * 查询申请列表
 */
export async function getChatRequestList(status?: number): Promise<ChatRequest[]> {
  const params: any = {};
  if (status !== undefined) {
    params.status = status;
  }
  console.log('调用getChatRequestList API，参数:', params);
  const resp = await http.get('/api/chat/request/list', { params });
  console.log('getChatRequestList API响应:', resp.data);
  // 确保返回的是数组
  if (Array.isArray(resp.data)) {
    return resp.data;
  }
  // 如果后端返回的不是数组，返回空数组
  console.warn('API返回的数据不是数组:', resp.data);
  return [];
}

/**
 * 查询所有申请（管理员）
 */
export async function getAllChatRequests(page: number = 1, limit: number = 10, filters?: any): Promise<any> {
  const params: any = { page, limit, ...filters };
  const resp = await http.get('/api/chat/request/all', { params });
  return resp.data;
}

/**
 * 查询会话列表
 */
export async function getChatSessionList(): Promise<ChatSession[]> {
  const resp = await http.get('/api/chat/session/list');
  const data = resp.data;
  
  // 处理可能的返回格式
  if (typeof data === 'string') {
    // 如果是错误字符串，返回空数组
    if (data === 'NOT_LOGIN' || data === 'ERROR' || data === 'NOT_PERMISSION') {
      return [];
    }
  }
  
  // 如果是数组，直接返回
  if (Array.isArray(data)) {
    return data;
  }
  
  // 如果是对象且有data字段，提取data
  if (data && data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  // 如果是对象且有rows字段，提取rows
  if (data && data.rows && Array.isArray(data.rows)) {
    return data.rows;
  }
  
  return [];
}

/**
 * 获取会话详情
 */
export async function getChatSession(id: number): Promise<ChatSession> {
  const resp = await http.get(`/api/chat/session/${id}`);
  return resp.data;
}

/**
 * 关闭会话
 */
export async function closeChatSession(id: number): Promise<string> {
  const resp = await http.put(`/api/chat/session/${id}/close`);
  return resp.data;
}

/**
 * 查询所有会话（管理员）
 */
export async function getAllChatSessions(page: number = 1, limit: number = 10, filters?: any): Promise<any> {
  const params: any = { page, limit, ...filters };
  const resp = await http.get('/api/chat/session/all', { params });
  return resp.data;
}

/**
 * 发送消息
 */
export async function sendChatMessage(sessionId: number, messageType: string, content: string): Promise<ChatMessage> {
  const resp = await http.post('/api/chat/message', {
    sessionId,
    messageType,
    content
  });
  return resp.data;
}

/**
 * 查询消息列表
 */
export async function getChatMessageList(sessionId: number, page: number = 1, limit: number = 50): Promise<ChatMessage[]> {
  const resp = await http.get('/api/chat/message/list', {
    params: { sessionId, page, limit }
  });
  return resp.data;
}

/**
 * 标记消息为已读
 */
export async function markChatMessageRead(sessionId: number): Promise<string> {
  const resp = await http.put('/api/chat/message/read', null, {
    params: { sessionId }
  });
  return resp.data;
}

/**
 * 获取未读消息数
 */
export async function getUnreadChatMessageCount(sessionId?: number): Promise<number> {
  const params: any = {};
  if (sessionId !== undefined) {
    params.sessionId = sessionId;
  }
  const resp = await http.get('/api/chat/message/unread', { params });
  return resp.data;
}

/**
 * 查询所有消息（管理员）
 */
export async function getAllChatMessages(page: number = 1, limit: number = 10, filters?: any): Promise<any> {
  const params: any = { page, limit, ...filters };
  const resp = await http.get('/api/chat/message/all', { params });
  return resp.data;
}

/**
 * 撤回消息
 */
export async function revokeChatMessage(messageId: number): Promise<string> {
  const resp = await http.put(`/api/chat/message/${messageId}/revoke`);
  return resp.data;
}

/**
 * 上传聊天图片
 */
export async function uploadChatImage(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('upload', file);
  const resp = await http.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  // 后端返回的是JSON字符串，需要解析
  let result = resp.data;
  if (typeof result === 'string') {
    try {
      result = JSON.parse(result);
    } catch (e) {
      // 如果不是JSON，直接使用字符串
    }
  }
  // 返回文件路径
  if (result && result.url) {
    return result.url;
  } else if (result && result.data && result.data.url) {
    return result.data.url;
  } else if (typeof result === 'string' && result.startsWith('/file/')) {
    return result;
  }
  throw new Error('上传失败：无法获取文件路径');
}
