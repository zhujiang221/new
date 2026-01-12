<template>
  <div class="user-profile">
    <div class="page-header">
      <h2>个人信息</h2>
    </div>
    
    <div class="profile-card">
      <div v-if="loading" class="loading">加载中...</div>
      <form v-else @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="form.username" class="form-control" disabled />
          <span class="hint">用户名不可修改</span>
        </div>
        <div class="form-group">
          <label>昵称</label>
          <input type="text" v-model="form.name" class="form-control" />
        </div>
        <div class="form-group">
          <label>电话</label>
          <input type="text" v-model="form.phone" class="form-control" />
        </div>
        <div class="form-group">
          <label>地址</label>
          <input type="text" v-model="form.address" class="form-control" />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage } from '../../utils/message';

const loading = ref(true);
const saving = ref(false);

const form = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  address: ''
});

async function fetchUserInfo() {
  loading.value = true;
  try {
    // 后端实际提供的是 /user/getCurrentUser
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    if (data && (data.result === 'success' || data.status === 'SUCCESS')) {
      form.id = String(data.id || '');
      form.username = data.username || '';
      form.name = data.name || '';
      form.phone = data.phone || '';
      form.address = data.address || '';
    } else if (data) {
      // 兜底：若后端直接返回对象
      form.id = String(data.id || '');
      form.username = data.username || '';
      form.name = data.name || '';
      form.phone = data.phone || '';
      form.address = data.address || '';
    }
  } catch (e) {
    console.error('获取用户信息失败:', e);
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (!form.name) {
    showMessage('请输入昵称', 'error');
    return;
  }
  
  saving.value = true;
  try {
    const resp = await http.post('/user/updateUser', {
      id: form.id,
      name: form.name,
      phone: form.phone,
      address: form.address
    });
    
    if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
      showMessage('保存成功', 'success');
    } else {
      showMessage('保存失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.user-profile {
  padding: 0;
  max-width: 600px;
}

.profile-card {
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

.form-control:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.hint {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: #999;
}

.form-actions {
  margin-top: 30px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>

