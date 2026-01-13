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
   * @param userId 用户ID
   * @param onMessage 消息处理回调
   */
  connect(userId: string | number, onMessage?: WebSocketMessageHandler): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接，无需重复连接');
      return;
    }

    this.userId = userId;
    this.isManualClose = false;

    // 构建WebSocket URL
    // 在开发环境下，直接连接到后端服务器（避免Vite代理问题）
    // 在生产环境下，使用相对路径通过代理连接
    const isDevelopment = import.meta.env.DEV;
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
        console.log('WebSocket连接成功，URL:', this.url);
        this.reconnectAttempts = 0;
        if (onMessage) {
          this.addMessageHandler(onMessage);
        }
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log('收到WebSocket消息:', message);
          this.handleMessage(message);
        } catch (e) {
          console.error('解析WebSocket消息失败:', e, '原始数据:', event.data);
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
   * 添加消息处理器
   */
  addMessageHandler(handler: WebSocketMessageHandler): void {
    this.messageHandlers.add(handler);
  }

  /**
   * 移除消息处理器
   */
  removeMessageHandler(handler: WebSocketMessageHandler): void {
    this.messageHandlers.delete(handler);
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(message: any): void {
    const wsMessage: WebSocketMessage = {
      type: message.type || 'notification',
      data: message
    };

    // 通知所有消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(wsMessage);
      } catch (e) {
        console.error('消息处理器执行失败:', e);
      }
    });
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
   */
  send(message: any): void {
    if (this.isConnected() && this.ws) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  }
}

// 导出单例
export const websocketManager = new WebSocketManager();
