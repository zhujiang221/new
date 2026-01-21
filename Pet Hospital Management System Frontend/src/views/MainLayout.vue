<template>
  <div class="layout">
    <!-- Top Bar -->
    <div class="top-bar">
      <div class="logo">
        <button class="menu-toggle" @click="toggleMobileMenu" v-if="isMobile">
          <span class="hamburger" :class="{ active: mobileMenuOpen }">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
        <button 
          v-if="!isMobile" 
          class="menu-toggle-desktop" 
          @click="toggleCollapse"
          :title="isCollapsed ? '展开导航栏' : '收起导航栏'"
        >
          <span class="toggle-icon" :class="{ 'collapsed': isCollapsed }">
            <span class="line"></span>
            <span class="line"></span>
            <span class="line"></span>
          </span>
        </button>
        <img src="/imgs/catFace.png" alt="logo" />
        <span class="title">
          宠物医院管理系统
        </span>
      </div>
      <div class="user-info">
        <span class="greeting">你好！</span>
        <span class="username">{{ userInfo.name || '用户' }}</span>
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="dropdown-trigger">
            ▼
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">修改个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-area">
      <!-- Mobile Menu Overlay -->
      <div 
        v-if="isMobile && mobileMenuOpen" 
        class="mobile-menu-overlay" 
        @click="closeMobileMenu"
      ></div>
      
      <!-- Left Menu -->
      <div 
        class="left-menu" 
        :class="{
          'mobile-open': mobileMenuOpen,
          'collapsed': isCollapsed && !isMobile
        }"
      >
        <div
          v-for="group in menuGroups"
          :key="group.title"
          class="menu-group"
        >
          <div
            class="menu-title"
            :class="{ active: expandedGroup === group.title }"
            @click="toggleGroup(group.title)"
          >
            <span class="menu-icon" v-if="group.icon">{{ group.icon }}</span>
            <span class="menu-text" v-show="!isCollapsed || isMobile">
              {{ group.title }}
            </span>
            <span class="arrow" v-show="!isCollapsed || isMobile">{{ expandedGroup === group.title ? '▼' : '▶' }}</span>
          </div>
          <ul v-show="expandedGroup === group.title && (!isCollapsed || isMobile)" class="menu-list">
            <li
              v-for="item in group.items"
              :key="item.path"
              :class="{ selected: currentPath === item.path }"
              @click="navigate(item.path)"
            >
              {{ item.label }}
            </li>
          </ul>
        </div>
      </div>

      <!-- Main Content -->
      <div class="content-area">
        <router-view />
      </div>
    </div>

    <!-- Footer -->
    <div class="footer">
      <p>宠物医院管理系统 © 2025</p>
    </div>

    <!-- Profile Edit Dialog -->
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
        <el-form-item>
          <el-button type="primary" @click="showChangePasswordDialog">修改密码</el-button>
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

    <!-- Change Password Dialog -->
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
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import http from '../api/http';
import { getUserInfo, clearUserInfo, clearUserInfoOnly, saveUserInfo, type UserInfo, ROLE_ADMIN, ROLE_DOCTOR, ROLE_USER, roleToString, getDeviceId } from '../utils/user';
import { getCurrentUserInfo } from '../api/user';
import { showMessage, showConfirm } from '../utils/message';

const router = useRouter();
const route = useRoute();

// 移动端检测 & 左侧导航折叠（默认展开）
const isMobile = ref(false);
const mobileMenuOpen = ref(false);
const isCollapsed = ref(false); // 默认展开

function checkMobile() {
  isMobile.value = window.innerWidth < 768;
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value;
}

function closeMobileMenu() {
  mobileMenuOpen.value = false;
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value;
}

interface MenuItem {
  label: string;
  path: string;
}

interface MenuGroup {
  title: string;
  items: MenuItem[];
  role: string;
  icon: string;
}

const userInfo = reactive<UserInfo>({
  id: '',
  username: '',
  name: '',
  role: ROLE_ADMIN // 1=管理员, 2=医生, 3=用户（MainLayout只用于管理员）
});

const expandedGroup = ref('');
const showPasswordModal = ref(false);
const showProfileDialog = ref(false);
const profileSaving = ref(false);
const passwordSaving = ref(false);

const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();

const profileForm = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  address: ''
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 个人信息表单验证规则
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
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
};

// 密码表单验证规则
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

// 管理员菜单组（MainLayout只用于管理员）
const menuGroups: MenuGroup[] = [
  {
    title: '用户管理',
    icon: '👤',
    role: String(ROLE_ADMIN),
    items: [
      { label: '用户管理', path: '/admin/users' },
      { label: '医生空闲时间', path: '/admin/free-time' },
      { label: '排班管理', path: '/admin/schedule' },
      { label: '服务类型管理', path: '/admin/service-type' }
    ]
  },
  {
    title: '消息管理',
    icon: '💬',
    role: String(ROLE_ADMIN),
    items: [
      { label: '聊天管理', path: '/admin/chat' },
      { label: '发送全局通知', path: '/admin/broadcast' }
    ]
  },
  {
    title: '宠物管理',
    icon: '🐾',
    role: String(ROLE_ADMIN),
    items: [
      { label: '宠物列表', path: '/admin/pets' },
      { label: '诊断记录', path: '/admin/diagnosis' }
    ]
  },
  {
    title: '预约管理',
    icon: '📅',
    role: String(ROLE_ADMIN),
    items: [
      { label: '预约列表', path: '/admin/apply' },
      { label: '预约统计', path: '/admin/tj-apply' },
      { label: '预约类型管理', path: '/admin/appointment-type' }
    ]
  },
  {
    title: '日常健康',
    icon: '❤️',
    role: String(ROLE_ADMIN),
    items: [
      { label: '健康指南', path: '/admin/notices' },
      { label: '健康评估', path: '/admin/assess' },
      { label: '健康标准', path: '/admin/standards' }
    ]
  },
  {
    title: '宠物档案',
    icon: '📊',
    role: String(ROLE_ADMIN),
    items: [
      { label: '宠物日志', path: '/admin/pet-daily' },
      { label: '日志统计', path: '/admin/tj-daily' }
    ]
  },
  {
    title: '药品管理',
    icon: '💊',
    role: String(ROLE_ADMIN),
    items: [
      { label: '药品列表', path: '/admin/medicine' },
      { label: '开药记录', path: '/admin/medicine-record' }
    ]
  },
  {
    title: '日志管理',
    icon: '📋',
    role: String(ROLE_ADMIN),
    items: [
      { label: 'API日志', path: '/admin/api-log' }
    ]
  }
];

const currentPath = computed(() => route.path);

function toggleGroup(title: string) {
  expandedGroup.value = expandedGroup.value === title ? '' : title;
}

function navigate(path: string) {
  router.push(path);
  // 移动端导航后关闭菜单
  if (isMobile.value) {
    closeMobileMenu();
  }
}

// 处理下拉菜单命令
function handleCommand(command: string) {
  if (command === 'profile') {
    showProfileDialog.value = true;
    loadProfileData();
  } else if (command === 'logout') {
    handleLogout();
  }
}

// 加载个人信息数据
async function loadProfileData() {
  try {
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    
    // 处理响应数据格式
    let userData = data;
    if (data && data.data) {
      userData = data.data;
    } else if (data && (data.result === 'success' || data.status === 'SUCCESS')) {
      userData = data;
    }
    
    if (userData) {
      // 更新个人信息表单
      profileForm.id = String(userData.id || '');
      profileForm.username = userData.username || '';
      profileForm.name = userData.name || '';
      profileForm.phone = userData.phone || '';
      profileForm.email = userData.email || '';
      profileForm.address = userData.address || '';
      
      // 同时更新顶部显示的用户信息（确保显示最新）
      const roleId = userData.roleId || userData.role || userInfo.role || ROLE_USER;
      const role = Number(roleId) || ROLE_USER;
      
      if (userData.name) {
        userInfo.name = userData.name;
      }
      if (userData.username) {
        userInfo.username = userData.username;
      }
    }
  } catch (e) {
    console.error('获取用户信息失败:', e);
    showMessage('获取用户信息失败', 'error');
  }
}

// 提交个人信息
async function submitProfile() {
  if (!profileFormRef.value) return;
  
  try {
    const valid = await profileFormRef.value.validate();
    if (valid) {
      profileSaving.value = true;
      const resp = await http.post('/user/updateUser', {
        id: profileForm.id,
        name: profileForm.name.trim(),
        phone: profileForm.phone.trim(),
        email: profileForm.email.trim(),
        address: profileForm.address.trim()
      });
      
      if (resp.data === 'SUCCESS' || resp.data?.status === 'SUCCESS') {
        showMessage('保存成功', 'success');
        // 强制从API重新获取最新的用户信息
        await refreshUserInfo();
        showProfileDialog.value = false;
      } else {
        showMessage('保存失败', 'error');
      }
    }
  } catch (e) {
    console.error('保存失败:', e);
    showMessage('操作失败', 'error');
  } finally {
    profileSaving.value = false;
  }
}

// 强制从API刷新用户信息
async function refreshUserInfo() {
  try {
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    
    // 处理响应数据格式
    let userData = data;
    if (data && data.data) {
      userData = data.data;
    } else if (data && (data.result === 'success' || data.status === 'SUCCESS')) {
      userData = data;
    }
    
    if (userData && userData.id) {
      // 只有在获取到有效数据时才更新
      // 更新顶部显示的用户信息
      const roleId = userData.roleId || userData.role || userInfo.role || ROLE_USER;
      const role = Number(roleId) || ROLE_USER;
      
      Object.assign(userInfo, {
        id: String(userData.id || ''),
        username: userData.username || userInfo.username || '',
        name: userData.name || userInfo.name || '',
        phone: userData.phone || userInfo.phone || '',
        role: role
      });
      
      // 更新localStorage
      saveUserInfo(userInfo);
      
      // 更新个人信息表单（如果弹窗还打开着）
      profileForm.id = String(userData.id || '');
      profileForm.username = userData.username || '';
      profileForm.name = userData.name || '';
      profileForm.phone = userData.phone || '';
      profileForm.email = userData.email || '';
      profileForm.address = userData.address || '';
    } else {
      // 如果API返回的数据无效，保留现有用户信息，不覆盖
      console.warn('API返回的用户数据无效，保留现有用户信息');
    }
  } catch (e) {
    console.error('刷新用户信息失败:', e);
    // API调用失败时，不覆盖现有用户信息
    // 从localStorage恢复用户信息（根据当前路由匹配角色）
    const currentPath = router.currentRoute.value.path;
    const expectedRole = currentPath.startsWith('/doctor') ? ROLE_DOCTOR : 
                         currentPath.startsWith('/admin') ? ROLE_ADMIN : ROLE_USER;
    const savedUserInfo = getUserInfo(expectedRole);
    if (savedUserInfo) {
      Object.assign(userInfo, savedUserInfo);
    }
  }
}

// 显示修改密码弹窗
function showChangePasswordDialog() {
  showProfileDialog.value = false;
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  showPasswordModal.value = true;
}

async function submitChangePassword() {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordSaving.value = true;
      try {
        // First check old password
        const checkResp = await http.post('/user/checkUserPassword', {
          password: passwordForm.oldPassword
        });
        
        if (checkResp.data !== true && checkResp.data !== 'true') {
          showMessage('旧密码不正确', 'error');
          passwordSaving.value = false;
          return;
        }

        // Update password
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
    // 只清除当前用户的信息，不影响其他已登录的账号
    if (userInfo.id && userInfo.role !== undefined) {
      clearUserInfo(userInfo.id, userInfo.role);
    } else {
      clearUserInfo(); // 如果没有用户信息，清除所有（向后兼容）
    }
    // 使用window.location强制刷新页面，确保清除所有状态
    window.location.href = '/';
  }
}

async function loadUserInfo() {
  // 获取设备ID（用于日志记录）
  const deviceId = getDeviceId();
  console.log('加载用户信息，设备ID:', deviceId, '主机名:', window.location.hostname);
  
  // 1. 检查当前路由路径，确定期望的角色
  const currentPath = router.currentRoute.value.path;
  const expectedRole = currentPath.startsWith('/doctor') ? ROLE_DOCTOR : 
                       currentPath.startsWith('/admin') ? ROLE_ADMIN : ROLE_USER;
  console.log('当前路由路径:', currentPath, '期望角色:', expectedRole);
  
  // 2. 根据期望的角色获取用户信息（支持多账号）
  const savedUserInfo = getUserInfo(expectedRole);
  
  // 3. 如果本地有用户信息，且角色与当前路由匹配，进行双重验证
  if (savedUserInfo && savedUserInfo.id && savedUserInfo.role === expectedRole) {
    console.log('本地有用户信息，且角色匹配，进行双重验证');
    
    // 先使用本地的用户信息（确保role是数字类型）
    const role = Number(savedUserInfo.role) || savedUserInfo.role || ROLE_ADMIN;
    Object.assign(userInfo, {
      ...savedUserInfo,
      role: role
    });
    
    // 3. 从后端获取最新的用户信息
    const apiUserInfo = await getCurrentUserInfo();
    
    if (apiUserInfo && apiUserInfo.id) {
      // 4. 检查后端返回的用户信息是否与本地一致
      if (apiUserInfo.id !== savedUserInfo.id) {
        console.warn('检测到用户切换：本地用户ID:', savedUserInfo.id, '后端用户ID:', apiUserInfo.id);
        
        // 5. 检查后端返回的用户角色是否与当前路由匹配
        const apiRole = Number(apiUserInfo.role) || apiUserInfo.role || ROLE_ADMIN;
        if (apiRole !== expectedRole) {
          // 6. 拒绝切换，清除本地存储，重定向到登录页
          console.error('检测到用户切换，但角色不匹配，拒绝切换');
          console.error('本地用户角色:', savedUserInfo.role, '后端用户角色:', apiRole, '期望角色:', expectedRole);
          clearUserInfo();
          showMessage('检测到用户切换，但角色不匹配，请重新登录', 'error');
          router.push('/');
          return;
        } else {
          // 角色匹配，但用户ID不同，可能是同一角色的不同用户
          // 这种情况可能是正常的（比如管理员A退出，管理员B登录），允许切换
          // 注意：不清除Token，只清除旧的用户信息，因为Token仍然有效
          console.warn('用户ID不同，但角色匹配，允许切换');
          // 只清除旧的用户信息，保留Token以便后续请求
          clearUserInfoOnly(savedUserInfo.id, savedUserInfo.role);
        }
      }
      
      // 使用后端返回的用户信息
      const apiRole = Number(apiUserInfo.role) || apiUserInfo.role || ROLE_ADMIN;
      Object.assign(userInfo, {
        ...apiUserInfo,
        role: apiRole
      });
      saveUserInfo(userInfo);
      console.log('加载用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role);
    } else {
      // 后端没有返回用户信息，但本地有，可能是未登录状态
      // 使用本地存储的用户信息（可能是缓存的）
      console.warn('后端未返回用户信息，使用本地缓存，主机名:', window.location.hostname);
    }
  } else {
    // 本地没有用户信息，或者角色不匹配，从后端获取
    console.log('本地没有用户信息或角色不匹配，从后端获取');
    const apiUserInfo = await getCurrentUserInfo();
    
    if (apiUserInfo && apiUserInfo.id) {
      // 检查后端返回的用户角色是否与当前路由匹配
      const apiRole = Number(apiUserInfo.role) || apiUserInfo.role || ROLE_ADMIN;
      if (apiRole !== expectedRole) {
        console.error('后端返回的用户角色与当前路由不匹配，拒绝加载');
        console.error('后端用户角色:', apiRole, '期望角色:', expectedRole);
        clearUserInfo();
        showMessage('用户角色与当前路由不匹配，请重新登录', 'error');
        router.push('/');
        return;
      }
      
      // 使用后端返回的用户信息
      Object.assign(userInfo, {
        ...apiUserInfo,
        role: apiRole
      });
      saveUserInfo(userInfo);
      console.log('加载用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role);
    } else {
      // 后端也没有返回用户信息
      console.error('无法获取用户信息，请重新登录');
      clearUserInfo();
      router.push('/');
      return;
    }
  }
  
  // 默认展开第一个菜单组
  if (menuGroups.length > 0) {
    expandedGroup.value = menuGroups[0].title;
  }
}

onMounted(() => {
  loadUserInfo();
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});

// 监听路由变化，自动展开对应的菜单组
watch(() => route.path, (newPath) => {
  // 自动展开包含当前路径的菜单组
  for (const group of menuGroups) {
    for (const item of group.items) {
      if (item.path === newPath) {
        expandedGroup.value = group.title;
        return;
      }
    }
  }
}, { immediate: true });
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.top-bar {
  height: 51px;
  background-color: rgba(255, 255, 255, 1);
  border-style: solid;
  border-width: 1px;
  border-color: rgba(224, 224, 224, 1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-toggle {
  background: none;
  border: none;
  padding: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.menu-toggle-desktop {
  background: none;
  border: none;
  padding: 8px;
  margin-right: 8px;
  cursor: pointer;
  color: #72C1BB;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  width: 32px;
  height: 32px;
}

.menu-toggle-desktop:hover {
  background-color: rgba(114, 193, 187, 0.15);
}

.toggle-icon {
  width: 20px;
  height: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

.toggle-icon .line {
  display: block;
  height: 2px;
  width: 100%;
  background-color: #72C1BB;
  border-radius: 1px;
  transition: all 0.3s ease;
}

.toggle-icon.collapsed .line:nth-child(1) {
  transform: rotate(45deg) translate(7px, 7px);
}

.toggle-icon.collapsed .line:nth-child(2) {
  opacity: 0;
}

.toggle-icon.collapsed .line:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -7px);
}

.menu-toggle-desktop:hover .toggle-icon .line {
  background-color: #a5f3eb;
}

.hamburger {
  width: 24px;
  height: 18px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
}

.hamburger span {
  display: block;
  height: 2px;
  width: 100%;
  background-color: #72C1BB;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.hamburger.active span:nth-child(1) {
  transform: rotate(45deg) translate(8px, 8px);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -7px);
}

.logo img {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  box-shadow: 0px 4px 12px 0px rgba(0, 0, 0, 0.15);
  transform: scaleX(-1);
  background-clip: unset;
  -webkit-background-clip: unset;
  color: rgba(51, 51, 51, 1);
  background-color: unset;
  border-color: rgba(0, 0, 0, 0);
  background: unset;
  border-image: none;
}

.logo .title {
  color: #72C1BB;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
}

.user-info {
  color: #333;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.user-info .greeting {
  display: none;
}

.user-info .username {
  color: rgba(0, 0, 0, 1);
  margin: 0 5px;
}

.dropdown-trigger {
  color: rgba(0, 0, 0, 1);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  padding: 5px;
  margin-left: 5px;
  transition: color 0.3s;
}

.dropdown-trigger:hover {
  color: #5aa9a3;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .top-bar {
    padding: 0 10px;
  }
  
  .logo .title {
    font-size: 14px;
  }
  
  .logo img {
    width: 32px;
    height: 32px;
  }
  
  .user-info {
    font-size: 12px;
  }
  
  .user-info .greeting {
    display: inline;
  }
  
  .dropdown-trigger {
    font-size: 16px;
  }
}

.main-area {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.mobile-menu-overlay {
  position: fixed;
  top: 51px;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 998;
}

.left-menu {
  width: 180px;
  background-color: rgba(255, 255, 255, 1);
  overflow-y: auto;
  flex-shrink: 0;
  transition: transform 0.3s ease, width 0.2s ease;
  border-right: 1px solid rgba(224, 224, 224, 1);
}

.left-menu.collapsed {
  width: 60px;
}

.left-menu.collapsed .menu-title {
  padding: 12px 8px;
  justify-content: center;
  align-items: center;
}

.left-menu.collapsed .arrow {
  display: none;
}

.left-menu.collapsed .menu-list {
  display: none;
}

.menu-icon {
  font-size: 20px;
  display: inline-block;
}

.menu-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.left-menu.collapsed .menu-text {
  display: none;
}

.left-menu.collapsed .menu-icon {
  font-size: 24px;
}

/* 移动端菜单样式 */
@media (max-width: 767px) {
  .left-menu {
    position: fixed;
    top: 51px;
    left: 0;
    bottom: 0;
    width: 250px;
    z-index: 999;
    transform: translateX(-100%);
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.2);
  }
  
  .left-menu.mobile-open {
    transform: translateX(0);
  }
  
  .content-area {
    width: 100%;
  }
}

.menu-group {
  border-bottom: 1px solid rgba(224, 224, 224, 1);
}

.menu-title {
  padding: 12px 15px;
  color: #333;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s;
}

.menu-title:hover {
  background-color: rgba(114, 193, 187, 0.1);
}

.menu-title.active {
  background-color: rgba(114, 193, 187, 0.15);
  color: #72C1BB;
}

.arrow {
  font-size: 10px;
}

.menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
  background-color: transparent;
}

.menu-list li {
  padding: 10px 15px 10px 25px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
  border-left: 3px solid transparent;
}

.menu-list li:hover {
  background-color: rgba(114, 193, 187, 0.1);
}

.menu-list li.selected {
  background-color: rgba(114, 193, 187, 0.15);
  color: #72C1BB;
  border-left: 3px solid #72C1BB;
  font-weight: 500;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: white;
  transition: margin-left 0.3s ease;
}

/* 移动端内容区域样式 */
@media (max-width: 767px) {
  .content-area {
    padding: 10px;
    width: 100%;
    margin-left: 0;
  }
}

.footer {
  height: 40px;
  background-color: rgba(255, 255, 255, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.footer p {
  color: rgba(0, 0, 0, 1);
  font-size: 12px;
  margin: 0;
}

/* Element Plus Dialog 样式覆盖 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  border-top: 1px solid #ebeef5;
}

/* 移动端弹窗样式 */
@media (max-width: 767px) {
  :deep(.el-dialog) {
    width: 90vw !important;
    margin: 5vh auto;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  :deep(.el-input) {
    font-size: 14px;
  }
}
</style>
