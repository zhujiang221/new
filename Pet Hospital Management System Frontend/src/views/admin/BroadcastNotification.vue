<template>
  <div class="broadcast-notification-modern">
    <div class="modern-page-header">
      <h1 class="modern-page-title">
        <span>📢</span>
        发送全局通知
      </h1>
      <p class="modern-page-subtitle">向指定角色的所有用户发送系统通知</p>
    </div>

    <div class="modern-card">
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">通知标题：</label>
          <input
            type="text"
            v-model="form.title"
            class="form-control"
            placeholder="请输入通知标题"
            maxlength="100"
          />
          <div class="char-count">{{ form.title.length }}/100</div>
        </div>

        <div class="form-group">
          <label class="form-label">通知内容：</label>
          <textarea
            v-model="form.content"
            class="form-control"
            rows="8"
            placeholder="请输入通知内容..."
            maxlength="1000"
          ></textarea>
          <div class="char-count">{{ form.content.length }}/1000</div>
        </div>

        <div class="form-group">
          <label class="form-label">发送对象：</label>
          <div class="role-checkboxes">
          
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="form.roleIds"
                value="2"
                class="checkbox-input"
              />
              <span class="checkbox-text">医生</span>
            </label>
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="form.roleIds"
                value="3"
                class="checkbox-input"
              />
              <span class="checkbox-text">用户</span>
            </label>
          </div>
          <div v-if="form.roleIds.length === 0" class="form-error">
            请至少选择一个角色
          </div>
        </div>

        <div class="form-actions">
          <button
            class="modern-btn modern-btn-primary"
            @click="submit"
            :disabled="submitting || !canSubmit"
          >
            {{ submitting ? '发送中...' : '📤 发送通知' }}
          </button>
          <button
            class="modern-btn modern-btn-secondary"
            @click="reset"
            :disabled="submitting"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 发送结果提示 -->
    <el-dialog
      v-model="showResultDialog"
      title="发送结果"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="result-content">
        <div v-if="result.success" class="result-success">
          <div class="result-icon">✅</div>
          <div class="result-message">{{ result.message }}</div>
        </div>
        <div v-else class="result-error">
          <div class="result-icon">❌</div>
          <div class="result-message">{{ result.message }}</div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showResultDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import http from '../../api/http';
import { showMessage } from '../../utils/message';

const form = ref({
  title: '',
  content: '',
  roleIds: [] as string[]
});

const submitting = ref(false);
const showResultDialog = ref(false);
const result = ref({ success: false, message: '' });

const canSubmit = computed(() => {
  return (
    form.value.title.trim() !== '' &&
    form.value.content.trim() !== '' &&
    form.value.roleIds.length > 0
  );
});

function reset() {
  form.value = {
    title: '',
    content: '',
    roleIds: []
  };
}

async function submit() {
  if (!canSubmit.value) {
    showMessage('请填写完整信息并至少选择一个角色', 'warning');
    return;
  }

  submitting.value = true;
  try {
    const roleIdsStr = form.value.roleIds.join(',');
    const resp = await http.post('/admin/sendBroadcastNotification', {
      title: form.value.title.trim(),
      content: form.value.content.trim(),
      roleIds: roleIdsStr
    });

    if (resp.data && resp.data.result === 'success') {
      result.value = {
        success: true,
        message: resp.data.message || '通知发送成功'
      };
      showResultDialog.value = true;
      reset();
    } else {
      result.value = {
        success: false,
        message: resp.data?.message || '通知发送失败'
      };
      showResultDialog.value = true;
    }
  } catch (e: any) {
    console.error('发送全局通知失败:', e);
    result.value = {
      success: false,
      message: e.response?.data?.message || e.message || '通知发送失败'
    };
    showResultDialog.value = true;
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.broadcast-notification-modern {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.modern-page-header {
  margin-bottom: 30px;
}

.modern-page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.modern-page-title span {
  font-size: 32px;
}

.modern-page-subtitle {
  color: #666;
  font-size: 14px;
}

.modern-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-control:focus {
  outline: none;
  border-color: #72C1BB;
}

textarea.form-control {
  resize: vertical;
  min-height: 120px;
}

.char-count {
  text-align: right;
  color: #999;
  font-size: 12px;
}

.role-checkboxes {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #72C1BB;
}

.checkbox-text {
  font-size: 15px;
  color: #333;
}

.form-error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.modern-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.modern-btn-primary {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

.modern-btn-primary:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(114, 193, 187, 0.3);
}

.modern-btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.modern-btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.modern-btn-secondary:hover:not(:disabled) {
  background: #e0e0e0;
}

.result-content {
  padding: 20px;
  text-align: center;
}

.result-success,
.result-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.result-icon {
  font-size: 48px;
}

.result-message {
  font-size: 16px;
  color: #333;
}

.result-error .result-message {
  color: #ff4d4f;
}

@media (max-width: 767px) {
  .broadcast-notification-modern {
    padding: 15px;
  }

  .modern-card {
    padding: 20px;
  }

  .role-checkboxes {
    flex-direction: column;
    gap: 12px;
  }

  .form-actions {
    flex-direction: column;
  }

  .modern-btn {
    width: 100%;
  }
}
</style>
