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

    // 构建WebSocket URL - 通过Vite代理连接到后端服务器
    // Vite会自动将 /ws 路径代理到后端服务器
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    this.url = `${wsProtocol}//${host}/ws/notification?userId=${userId}`;

    try {
      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        console.log('WebSocket连接成功');
        this.reconnectAttempts = 0;
        if (onMessage) {
          this.addMessageHandler(onMessage);
        }
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleMessage(message);
        } catch (e) {
          console.error('解析WebSocket消息失败:', e);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
      };

      this.ws.onclose = () => {
        console.log('WebSocket连接关闭');
        this.ws = null;

        // 如果不是手动关闭，尝试重连
        if (!this.isManualClose && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect();
        }
      };
    } catch (error) {
      console.error('WebSocket连接失败:', error);
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
