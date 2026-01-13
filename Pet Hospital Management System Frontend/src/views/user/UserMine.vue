<template>
  <div class="user-mine">
    <!-- 用户信息卡片 -->
    <div class="user-info-card">
      <div class="avatar-section">
        <div class="avatar">👤</div>
        <div class="user-details">
          <div class="user-name">{{ userInfo.name || '用户' }}</div>
          <div class="user-username">{{ userInfo.username }}</div>
        </div>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="function-list">
      <div class="list-item" @click="openProfileDialog">
        <div class="item-icon">👤</div>
        <div class="item-content">
          <div class="item-title">个人信息</div>
          <div class="item-desc">查看和编辑个人信息</div>
        </div>
        <div class="item-arrow">→</div>
      </div>

      <div class="list-item" @click="showChangePassword">
        <div class="item-icon">🔒</div>
        <div class="item-content">
          <div class="item-title">修改密码</div>
          <div class="item-desc">更改登录密码</div>
        </div>
        <div class="item-arrow">→</div>
      </div>

      <div class="list-item" @click="showSettings">
        <div class="item-icon">⚙️</div>
        <div class="item-content">
          <div class="item-title">设置</div>
          <div class="item-desc">系统设置和关于信息</div>
        </div>
        <div class="item-arrow">→</div>
      </div>
    </div>

    <!-- 其他信息（移动端隐藏，电脑端显示） -->
    <div class="info-section info-section-desktop">
      <div class="info-item">
        <span class="info-label">电话：</span>
        <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">邮箱：</span>
        <span class="info-value">{{ userInfo.email || '未设置' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">地址：</span>
        <span class="info-value">{{ userInfo.address || '未设置' }}</span>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <!-- 个人信息编辑对话框 -->
    <el-dialog
      v-model="showProfileDialog"
      title="修改个人信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="profileForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入11位手机号码" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" type="email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="profileForm.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProfileDialog = false">取消</el-button>
          <el-button type="primary" @click="submitProfile" :loading="profileSaving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="showSettingsDialog"
      title="设置"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="settings-content">
        <div class="settings-section">
          <div class="settings-title">关于</div>
          <div class="settings-item">
            <span class="settings-label">系统版本</span>
            <span class="settings-value">v1.0.8</span>
          </div>
          <div class="settings-item">
            <span class="settings-label">版权所有</span>
            <span class="settings-value">宠物医院管理系统 © 2025</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="showSettingsDialog = false">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordModal"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码(至少6位)"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPasswordModal = false">取消</el-button>
          <el-button type="primary" @click="submitChangePassword" :loading="passwordSaving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { getUserInfo, clearUserInfo, saveUserInfo, type UserInfo } from '../../utils/user';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

const router = useRouter();
const defaultUserInfo: UserInfo = { 
  id: '', 
  name: '用户', 
  username: '', 
  role: 3,
  phone: undefined,
  email: undefined,
  address: undefined
};
const userInfo = reactive<UserInfo>(getUserInfo() || defaultUserInfo);

const showPasswordModal = ref(false);
const showProfileDialog = ref(false);
const showSettingsDialog = ref(false);
const passwordSaving = ref(false);
const profileSaving = ref(false);
const passwordFormRef = ref<FormInstance>();
const profileFormRef = ref<FormInstance>();

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const profileForm = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  address: ''
});

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 邮箱唯一性验证
const validateEmailUnique = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback();
    return;
  }
  
  // 如果邮箱没有变化，不需要验证
  if (value === userInfo.email) {
    callback();
    return;
  }
  
  // 异步验证邮箱唯一性
  http.get('/user/checkEmail', {
    params: {
      email: value,
      userId: profileForm.id
    }
  }).then((resp) => {
    if (resp.data && resp.data.status === 'SUCCESS') {
      callback();
    } else {
      callback(new Error(resp.data?.message || '该邮箱已被其他用户使用'));
    }
  }).catch((e: any) => {
    // 如果后端返回错误，说明邮箱已被使用
    if (e.response?.data?.message) {
      callback(new Error(e.response.data.message));
    } else {
      callback(new Error('验证邮箱失败，请稍后重试'));
    }
  });
};

const profileRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '电话号格式不正确，请输入11位手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { validator: validateEmailUnique, trigger: 'blur' }
  ]
};

// 加载个人信息数据
async function loadProfileData() {
  try {
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    
    let userData = data;
    if (data && data.data) {
      userData = data.data;
    } else if (data && (data.result === 'success' || data.status === 'SUCCESS')) {
      userData = data;
    }
    
    if (userData) {
      profileForm.id = String(userData.id || '');
      profileForm.username = userData.username || '';
      profileForm.name = userData.name || '';
      profileForm.phone = userData.phone || '';
      profileForm.email = userData.email || '';
      profileForm.address = userData.address || '';
      
      // 更新显示的用户信息
      Object.assign(userInfo, {
        id: String(userData.id || ''),
        username: userData.username || '',
        name: userData.name || '',
        phone: userData.phone || '',
        email: userData.email || '',
        address: userData.address || '',
        role: 3
      });
      
      saveUserInfo(userInfo);
    }
  } catch (e) {
    console.error('获取用户信息失败:', e);
    showMessage('获取用户信息失败', 'error');
  }
}

function openProfileDialog() {
  loadProfileData();
  showProfileDialog.value = true;
}

// 提交个人信息
async function submitProfile() {
  if (!profileFormRef.value) return;
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      profileSaving.value = true;
      try {
        const resp = await http.post('/user/updateUser', {
          id: profileForm.id,
          name: profileForm.name.trim(),
          phone: profileForm.phone.trim(),
          email: profileForm.email.trim(),
          address: profileForm.address.trim()
        });
        
        if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
          showMessage('保存成功', 'success');
          await loadProfileData(); // 重新加载用户信息
          showProfileDialog.value = false;
        } else if (resp.data === 'EMAIL_EXISTS') {
          showMessage('该邮箱已被其他用户使用', 'error');
        } else {
          showMessage('保存失败', 'error');
        }
      } catch (e) {
        console.error('保存失败:', e);
        showMessage('操作失败', 'error');
      } finally {
        profileSaving.value = false;
      }
    }
  });
}

function showChangePassword() {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  showPasswordModal.value = true;
}

function showSettings() {
  showSettingsDialog.value = true;
}

async function submitChangePassword() {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordSaving.value = true;
      try {
        const checkResp = await http.post('/user/checkUserPassword', {
          password: passwordForm.oldPassword
        });
        
        if (checkResp.data !== true && checkResp.data !== 'true') {
          showMessage('旧密码不正确', 'error');
          passwordSaving.value = false;
          return;
        }

        const resp = await http.post('/user/updatePassword', {
          password: passwordForm.newPassword
        });
        
        if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
          showMessage('密码修改成功，请重新登录', 'success');
          showPasswordModal.value = false;
          clearUserInfo();
          router.push('/');
        } else {
          showMessage('修改失败', 'error');
        }
      } catch (e) {
        console.error('修改密码失败:', e);
        showMessage('系统错误', 'error');
      } finally {
        passwordSaving.value = false;
      }
    }
  });
}

async function handleLogout() {
  const confirmed = await showConfirm('确认退出吗？');
  if (confirmed) {
    try {
      await http.get('/logout');
    } catch (e) {
      // Ignore logout errors
    }
    clearUserInfo();
    // 使用window.location强制刷新页面，确保清除所有状态
    window.location.href = '/';
  }
}

// 组件挂载时加载用户信息
onMounted(() => {
  loadProfileData();
});
</script>

<style scoped>
.user-mine {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.user-info-card {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  color: white;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.user-username {
  font-size: 14px;
  opacity: 0.9;
}

.function-list {
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.list-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.list-item:last-child {
  border-bottom: none;
}

.list-item:hover {
  background-color: #f5f5f5;
}

.item-icon {
  font-size: 24px;
  margin-right: 16px;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 14px;
  color: #666;
}

.item-arrow {
  font-size: 20px;
  color: #999;
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.info-value {
  color: #333;
}

.logout-section {
  margin-top: 30px;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.logout-btn:hover {
  background: #ee5a6f;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .user-mine {
    padding: 15px;
  }

  .user-info-card {
    padding: 20px;
  }

  .avatar {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }

  .user-name {
    font-size: 20px;
  }

  .list-item {
    padding: 14px 16px;
  }

  /* 移动端隐藏信息展示区域 */
  .info-section-desktop {
    display: none;
  }
}

/* Element Plus Dialog 移动端适配 */
:deep(.el-dialog) {
  margin: 5vh auto;
}

@media (max-width: 767px) {
  :deep(.el-dialog) {
    width: 90vw !important;
    margin: 5vh auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

/* 设置对话框样式 */
.settings-content {
  padding: 10px 0;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
}

.settings-item {
  padding: 12px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.settings-item:last-child {
  border-bottom: none;
}

.settings-label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
}

.settings-value {
  color: #333;
  font-size: 14px;
}
</style>
