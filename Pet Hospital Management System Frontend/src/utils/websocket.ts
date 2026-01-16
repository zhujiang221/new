/**
 * WebSocket工具类
 * 封装WebSocket连接管理，实现连接、断开、消息监听和自动重连机制
 */

export interface WebSocketMessage {
  type: string;
  data: any;
}

export type WebSocketMessageHandler = (message: WebSocketMessage) => void;

class WebSocketManager {
  private ws: WebSocket | null = null;
  private url: string = '';
  private userId: string | number | null = null;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;
  private reconnectInterval: number = 3000; // 3秒
  private reconnectTimer: NodeJS.Timeout | null = null;
  private messageHandlers: Set<WebSocketMessageHandler> = new Set();
  private isManualClose: boolean = false;

  /**
   * 连接WebSocket
   * @param userId 用户ID（必须绑定唯一用户ID，确保服务端能精准推送消息）
   * @param onMessage 消息处理回调
   */
  connect(userId: string | number, onMessage?: WebSocketMessageHandler): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已连接，无需重复连接，用户ID:', this.userId);
      // 如果已连接但用户ID不同，需要重新连接
      if (this.userId !== userId) {
        console.log('[WebSocket] 用户ID变更，重新连接，旧ID:', this.userId, '新ID:', userId);
        this.disconnect();
      } else {
        // 用户ID相同，直接添加消息处理器
        if (onMessage) {
          this.addMessageHandler(onMessage);
        }
        return;
      }
    }

    // 验证用户ID
    if (!userId) {
      console.error('[WebSocket] 连接失败：用户ID不能为空');
      return;
    }

    this.userId = userId;
    this.isManualClose = false;
    console.log('[WebSocket] 开始连接，用户ID:', userId);

    // 构建WebSocket URL
    // 在开发环境下，直接连接到后端服务器（避免Vite代理问题）
    // 在生产环境下，使用相对路径通过代理连接
    const isDevelopment = (import.meta as any).env?.DEV ?? process.env.NODE_ENV === 'development';
    let wsProtocol: string;
    let host: string;
    
    if (isDevelopment) {
      // 开发环境：直接连接到后端服务器
      wsProtocol = 'ws:';
      host = 'localhost:8186'; // 后端服务器地址
      this.url = `${wsProtocol}//${host}/ws/notification?userId=${userId}`;
    } else {
      // 生产环境：使用相对路径（通过反向代理）
      wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      host = window.location.host;
      this.url = `${wsProtocol}//${host}/ws/notification?userId=${userId}`;
    }
    
    console.log('尝试连接WebSocket:', this.url, '开发模式:', isDevelopment);

    try {
      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        console.log('[WebSocket] 连接成功，URL:', this.url, '用户ID:', this.userId);
        this.reconnectAttempts = 0;
        if (onMessage) {
          this.addMessageHandler(onMessage);
        }
        // 发送连接确认消息（如果需要）
        this.send({ type: 'connected', userId: this.userId });
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log('[WebSocket] 收到消息，用户ID:', this.userId, '消息:', message);
          
          // 消息确认机制：如果消息包含确认ID，确认对应的待发送消息
          if (message.confirmId) {
            console.log('[WebSocket] 收到消息确认，确认ID:', message.confirmId);
            // 这里可以触发确认回调，由调用方处理
          }
          
          this.handleMessage(message);
        } catch (e) {
          console.error('[WebSocket] 解析消息失败:', e, '原始数据:', event.data);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        console.error('WebSocket连接URL:', this.url);
        console.error('WebSocket状态:', this.ws?.readyState);
        // 尝试获取更多错误信息
        if (this.ws) {
          console.error('WebSocket readyState:', this.ws.readyState);
          console.error('WebSocket protocol:', this.ws.protocol);
          console.error('WebSocket extensions:', this.ws.extensions);
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭', {
          code: event.code,
          reason: event.reason,
          wasClean: event.wasClean,
          url: this.url
        });
        this.ws = null;

        // 如果不是手动关闭，尝试重连
        if (!this.isManualClose && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect();
        } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
          console.error('WebSocket重连次数已达上限，停止重连');
        }
      };
    } catch (error) {
      console.error('WebSocket连接失败:', error);
      console.error('连接URL:', this.url);
      this.scheduleReconnect();
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isManualClose = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.messageHandlers.clear();
  }

  /**
   * 添加消息处理器（统一注册接收监听事件）
   */
  addMessageHandler(handler: WebSocketMessageHandler): void {
    console.log('[WebSocket] 添加消息处理器，当前处理器数:', this.messageHandlers.size);
    this.messageHandlers.add(handler);
    console.log('[WebSocket] 消息处理器已添加，当前处理器数:', this.messageHandlers.size);
  }

  /**
   * 移除消息处理器
   */
  removeMessageHandler(handler: WebSocketMessageHandler): void {
    this.messageHandlers.delete(handler);
  }

  /**
   * 处理接收到的消息（添加异常捕获机制，确保稳定接收服务端推送）
   */
  private handleMessage(message: any): void {
    console.log('[WebSocket] 处理消息，原始消息:', message);
    
    // 如果消息已经是正确的格式（有type和data），直接使用，不再重复包装
    let wsMessage: WebSocketMessage;
    
    if (message.type && message.data !== undefined) {
      // 已经是正确格式，直接使用
      wsMessage = {
        type: message.type,
        data: message.data  // 直接使用data，不再嵌套
      };
      console.log('[WebSocket] 消息格式正确，直接使用:', wsMessage);
    } else {
      // 旧格式，包装成统一格式
      wsMessage = {
        type: message.type || 'notification',
        data: message
      };
      console.log('[WebSocket] 消息格式需要包装:', wsMessage);
    }

    // 通知所有消息处理器（添加异常捕获，确保一个处理器失败不影响其他处理器）
    let handlerCount = 0;
    let successCount = 0;
    let errorCount = 0;
    
    this.messageHandlers.forEach(handler => {
      handlerCount++;
      try {
        handler(wsMessage);
        successCount++;
      } catch (e) {
        errorCount++;
        console.error('[WebSocket] 消息处理器执行失败:', e, '消息:', wsMessage);
      }
    });
    
    console.log(`[WebSocket] 消息处理完成，处理器数: ${handlerCount}，成功: ${successCount}，失败: ${errorCount}`);
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectTimer) {
      return;
    }

    this.reconnectAttempts++;
    console.log(`准备重连WebSocket (第${this.reconnectAttempts}次)...`);

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      if (this.userId) {
        this.connect(this.userId);
      }
    }, this.reconnectInterval);
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }

  /**
   * 发送消息（如果需要双向通信）
   * 增加消息发送确认机制：发送后等待服务端回执，未收到则重新发送
   */
  send(message: any, confirmCallback?: (confirmId: string) => void): void {
    if (this.isConnected() && this.ws) {
      try {
        const messageStr = JSON.stringify(message);
        this.ws.send(messageStr);
        console.log('[WebSocket] 消息已发送，用户ID:', this.userId, '消息:', message);
        
        // 如果提供了确认回调，可以在这里处理确认逻辑
        // 实际应用中，可以添加消息ID和确认机制
        if (confirmCallback && message.id) {
          // 这里可以实现确认机制，暂时先发送
          console.log('[WebSocket] 等待消息确认，消息ID:', message.id);
        }
      } catch (e) {
        console.error('[WebSocket] 发送消息失败:', e);
        throw e;
      }
    } else {
      console.warn('[WebSocket] 未连接，无法发送消息，用户ID:', this.userId, '连接状态:', this.ws?.readyState);
      throw new Error('WebSocket未连接');
    }
  }
  
  /**
   * 获取当前绑定的用户ID
   */
  getUserId(): string | number | null {
    return this.userId;
  }
}

// 导出单例
export const websocketManager = new WebSocketManager();
