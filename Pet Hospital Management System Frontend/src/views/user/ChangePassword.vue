<template>
  <div class="change-password">
    <div class="page-header">
      <h2>修改密码</h2>
    </div>
    
    <div class="password-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>旧密码</label>
          <input 
            type="password" 
            v-model="form.oldPassword" 
            class="form-control" 
            placeholder="请输入旧密码"
          />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input 
            type="password" 
            v-model="form.newPassword" 
            class="form-control" 
            placeholder="请输入新密码(至少6位)"
          />
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input 
            type="password" 
            v-model="form.confirmPassword" 
            class="form-control" 
            placeholder="请再次输入新密码"
          />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? '提交中...' : '确认修改' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import http from '../../api/http';
import { showMessage } from '../../utils/message';

const router = useRouter();
const loading = ref(false);

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

async function handleSubmit() {
  if (!form.oldPassword) {
    showMessage('请输入旧密码', 'error');
    return;
  }
  if (form.newPassword.length < 6) {
    showMessage('新密码长度不能小于6位', 'error');
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    showMessage('两次输入的新密码不一致', 'error');
    return;
  }
  
  loading.value = true;
  try {
    // First verify old password
    const checkResp = await http.post('/user/checkUserPassword', {
      password: form.oldPassword
    });
    
    if (checkResp.data !== true && checkResp.data !== 'true') {
      showMessage('旧密码不正确', 'error');
      return;
    }
    
    // Update password
    const resp = await http.post('/user/updatePassword', {
      password: form.newPassword
    });
    
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('密码修改成功，请重新登录', 'success');
      router.push('/');
    } else {
      showMessage('修改失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.change-password {
  padding: 0;
  max-width: 500px;
}

.password-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  margin-top: 30px;
}
</style>

