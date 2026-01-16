import { ref } from 'vue';

// 消息弹窗状态
const showMessageModal = ref(false);
const messageText = ref('');
const messageType = ref<'success' | 'error' | 'warning'>('success');
const messageCallback = ref<(() => void) | null>(null);

// 确认弹窗状态
const showConfirmModal = ref(false);
const confirmText = ref('');
const confirmCallback = ref<((confirmed: boolean) => void) | null>(null);

/**
 * 显示消息提示（成功、错误或警告）
 * @param text 提示文本
 * @param type 消息类型：'success' | 'error' | 'warning'
 */
export function showMessage(text: string, type: 'success' | 'error' | 'warning' = 'success') {
  messageText.value = text;
  messageType.value = type;
  showMessageModal.value = true;
}

/**
 * 显示确认对话框
 * @param text 确认提示文本
 * @returns Promise<boolean> 用户确认返回 true，取消返回 false
 */
export function showConfirm(text: string): Promise<boolean> {
  return new Promise((resolve) => {
    confirmText.value = text;
    confirmCallback.value = (confirmed: boolean) => {
      resolve(confirmed);
    };
    showConfirmModal.value = true;
  });
}

/**
 * 关闭消息弹窗
 */
export function closeMessageModal() {
  showMessageModal.value = false;
  messageText.value = '';
}

/**
 * 关闭确认弹窗
 * @param confirmed 是否确认
 */
export function closeConfirmModal(confirmed: boolean) {
  showConfirmModal.value = false;
  confirmText.value = '';
  if (confirmCallback.value) {
    confirmCallback.value(confirmed);
    confirmCallback.value = null;
  }
}

// 导出状态，供组件使用
export {
  showMessageModal,
  messageText,
  messageType,
  showConfirmModal,
  confirmText
};

