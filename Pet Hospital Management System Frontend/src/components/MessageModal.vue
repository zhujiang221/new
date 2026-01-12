<template>
  <!-- 消息提示弹窗 -->
  <div v-if="showMessageModal" class="modal-overlay" @click.self="closeMessageModal">
    <div class="modal message-modal">
      <div class="modal-header" :class="messageType === 'error' ? 'message-header-error' : 'message-header-success'">
        <h3>{{ messageType === 'error' ? '错误提示' : '成功提示' }}</h3>
        <button class="modal-close" @click="closeMessageModal">&times;</button>
      </div>
      <div class="modal-body">
        <div class="message-content">
          <p>{{ messageText }}</p>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-primary" @click="closeMessageModal">确定</button>
      </div>
    </div>
  </div>

  <!-- 确认对话框 -->
  <div v-if="showConfirmModal" class="modal-overlay" @click.self="closeConfirmModal(false)">
    <div class="modal message-modal">
      <div class="modal-header message-header-confirm">
        <h3>确认提示</h3>
        <button class="modal-close" @click="closeConfirmModal(false)">&times;</button>
      </div>
      <div class="modal-body">
        <div class="message-content">
          <p>{{ confirmText }}</p>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-primary" @click="closeConfirmModal(true)">确认</button>
        <button class="btn" @click="closeConfirmModal(false)">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  showMessageModal, 
  messageText, 
  messageType,
  showConfirmModal,
  confirmText,
  closeMessageModal,
  closeConfirmModal
} from '../utils/message';
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.modal {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  min-width: 400px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 8px 8px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: color 0.3s;
}

.modal-close:hover {
  color: #606266;
}

.message-header-error {
  background-color: #f44336;
  color: white;
}

.message-header-success {
  background-color: #4caf50;
  color: white;
}

.message-header-confirm {
  background-color: #2196f3;
  color: white;
}

.message-header-error h3,
.message-header-success h3,
.message-header-confirm h3 {
  color: white;
}

.message-header-error .modal-close,
.message-header-success .modal-close,
.message-header-confirm .modal-close {
  color: white;
}

.message-header-error .modal-close:hover,
.message-header-success .modal-close:hover,
.message-header-confirm .modal-close:hover {
  color: rgba(255, 255, 255, 0.8);
}

.modal-body {
  padding: 20px;
}

.message-content {
  padding: 10px 0;
  text-align: center;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-content p {
  margin: 0;
  word-break: break-word;
  white-space: pre-wrap;
}

.modal-footer {
  padding: 10px 20px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  background-color: white;
  color: #606266;
  transition: all 0.3s;
}

.btn:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.btn-primary {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.btn-primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .modal {
    min-width: auto;
    width: 90%;
    max-width: 90%;
  }
  
  .message-content {
    padding: 15px 0;
    font-size: 14px;
    min-height: 50px;
  }
  
  .modal-header {
    padding: 15px;
  }
  
  .modal-header h3 {
    font-size: 16px;
  }
  
  .modal-body {
    padding: 15px;
  }
  
  .modal-footer {
    padding: 10px 15px 15px;
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>

