<template>
  <div class="user-layout">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="logo">
        <!-- 移动端返回按钮（仅在非主页面显示） -->
        <button 
          v-if="isMobile && !showMainPageView" 
          class="back-button" 
          @click="goBack"
          title="返回"
        >
          <
        </button>
        <img src="/imgs/catFace.png" alt="logo" />
        <div class="title-wrapper">
          <span class="title">
            {{ isMobile && !showMainPageView ? (route.meta?.title as string || '详情') : '宠物医院管理系统' }}
          </span>
          <span v-if="!isMobile || (isMobile && !showMainPageView)" class="subtitle">用户端</span>
        </div>
      </div>
      <div class="user-info" v-if="!isMobile">
        <!-- 通知按钮 -->
        <div class="notification-button" @click="goToMessage" title="通知">
          <span class="notification-icon">🔔</span>
          <span v-if="unreadMessageCount > 0" class="notification-badge">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</span>
        </div>
        <!-- 聊天按钮 -->
        <div class="help-button" @click="goToChat" title="聊天">
          <span class="help-icon">💬</span>
          <span v-if="chatUnreadCount > 0" class="chat-badge">
            {{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}
          </span>
        </div>
        <!-- 用户头像/信息 -->
        <div class="user-avatar-button" @click="goToMine" title="用户主页">
          <span class="user-avatar-icon">👤</span>
        </div>
        <!-- 下拉菜单 -->
        <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
          <span style="display: none;"></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">修改个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <!-- 移动端只显示用户名和通知按钮 -->
      <div class="user-info-mobile" v-if="isMobile">
        <!-- 通知按钮 -->
        <div class="notification-button-mobile" @click="goToMessage" title="通知">
          <span class="notification-icon">🔔</span>
          <span v-if="unreadMessageCount > 0" class="notification-dot"></span>
        </div>
        <!-- 聊天按钮 -->
        <div class="chat-button-mobile" @click="goToChat" title="聊天">
          <span class="chat-icon">💬</span>
          <span v-if="chatUnreadCount > 0" class="chat-badge">
            {{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}
          </span>
        </div>
        <!-- 用户头像 -->
        <div class="user-avatar-button-mobile" @click="goToMine" title="用户主页">
          <span class="user-avatar-icon">👤</span>
        </div>
      </div>
    </div>

    <div class="main-area">
      <!-- 电脑端左侧导航栏 -->
      <div 
        v-if="!isMobile"
        class="left-menu"
      >
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          :class="{ active: currentPath === item.path || (item.path === '/user' && currentPath === '/user') }"
          @click="navigate(item.path)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-text">{{ item.label }}</span>
        </div>
      </div>

      <!-- 移动端底部导航栏（在主页面相关路由下显示） -->
      <div v-if="shouldShowBottomNav" class="bottom-nav">
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'home' }"
          @click="switchTab('home')"
        >
          <div class="nav-icon">🏠</div>
          <div class="nav-label">首页</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'pets' }"
          @click="switchTab('pets')"
        >
          <div class="nav-icon">🐾</div>
          <div class="nav-label">宠物管理</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'apply' }"
          @click="switchTab('apply')"
        >
          <div class="nav-icon">📅</div>
          <div class="nav-label">预约管理</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'monitor' }"
          @click="switchTab('monitor')"
        >
          <div class="nav-icon">❤️</div>
          <div class="nav-label">健康监测</div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area" :class="{ 'with-bottom-nav': shouldShowBottomNav }">
        <!-- 移动端显示核心页面或路由内容 -->
        <template v-if="isMobile">
          <!-- 如果在主页面，显示对应的主页面组件 -->
          <template v-if="showMainPageView">
            <UserHome v-if="currentTab === 'home'" @navigate="handleChildNavigate" />
          </template>
          <!-- 如果在子路由页面，显示路由内容 -->
          <router-view v-else />
        </template>
        <!-- 电脑端：如果是首页路径，显示主页组件，否则显示路由内容 -->
        <template v-else>
          <UserHome v-if="route.path === '/user'" @navigate="handleChildNavigate" />
          <router-view v-else />
        </template>
      </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, onUnmounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import http from '../../api/http';
import { getUserInfo, clearUserInfo, clearUserInfoOnly, saveUserInfo, type UserInfo, ROLE_DOCTOR, ROLE_ADMIN, ROLE_USER, getDeviceId } from '../../utils/user';
import { getCurrentUserInfo } from '../../api/user';
import { showMessage, showConfirm } from '../../utils/message';
import { getUnreadChatMessageCount } from '../../api/chat';
import { websocketManager } from '../../utils/websocket';
import UserHome from './UserHome.vue';
import { useNotification } from '../../composables/useNotification';

const router = useRouter();
const route = useRoute();

// 移动端检测
const isMobile = ref(false);
const currentTab = ref<'home' | 'pets' | 'apply' | 'monitor'>('home');

// 用于跟踪是否应该显示主页面视图（而不是路由视图）
// true: 显示主页面组件（UserHome/UserMine/UserMore）
// false: 显示路由视图（router-view）
const showMainPageView = ref(true);

function checkMobile() {
  isMobile.value = window.innerWidth < 768;
}

// 返回按钮点击事件
function goBack() {
  const currentPath = route.path;
  
  // 定义路由的层级关系：子路由 -> 父路由
  const routeHierarchy: Record<string, string> = {
    // 预约相关
    '/user/apply-flow': '/user/apply',  // 预约就诊 -> 我的预约
    // 宠物相关
    '/user/pet-detail': '/user/pets',   // 宠物详情 -> 宠物管理
    '/user/pet-edit': '/user/pets',     // 编辑宠物 -> 宠物管理
    '/user/pet-add': '/user/pets',      // 添加宠物 -> 宠物管理
    // 可以继续添加其他路由关系
  };
  
  // 检查是否有明确的父路由
  if (routeHierarchy[currentPath]) {
    const parentPath = routeHierarchy[currentPath];
    // 检查父路由是否是主页面相关路由
    if (parentPath === '/user' || parentPath === '/user/pets' || 
        parentPath === '/user/apply' || parentPath === '/user/assess') {
      // 设置对应的tab
      if (parentPath === '/user') {
  currentTab.value = 'home';
  showMainPageView.value = true;
      } else if (parentPath === '/user/pets') {
        currentTab.value = 'pets';
        showMainPageView.value = false;
      } else if (parentPath === '/user/apply') {
        currentTab.value = 'apply';
        showMainPageView.value = false;
      } else if (parentPath === '/user/assess') {
        currentTab.value = 'monitor';
        showMainPageView.value = false;
      }
      isTabNavigation.value = true;
      router.push(parentPath).catch(() => {
    // 忽略导航重复的错误
  });
      return;
    }
  }
  
  // 如果没有明确的父路由，尝试从路径推断
  // 处理特殊的路由模式，如 /user/apply-flow -> /user/apply
  if (currentPath.startsWith('/user/')) {
    const pathParts = currentPath.split('/').filter(p => p);
    
    // 处理带后缀的路由，如 apply-flow, pet-detail 等
    if (pathParts.length === 3 && pathParts[0] === 'user') {
      const secondPart = pathParts[1];
      const thirdPart = pathParts[2];
      
      // 如果是 apply-flow，返回到 apply
      if (secondPart === 'apply' && thirdPart === 'flow') {
        currentTab.value = 'apply';
        showMainPageView.value = false;
        isTabNavigation.value = true;
        router.push('/user/apply').catch(() => {});
        return;
      }
      
      // 如果是 pets 相关的子路由，返回到 pets
      if (secondPart === 'pets') {
        currentTab.value = 'pets';
        showMainPageView.value = false;
        isTabNavigation.value = true;
        router.push('/user/pets').catch(() => {});
        return;
      }
    }
    
    // 如果是三级或更深的路由（如 /user/pets/123），返回到二级路由
    if (pathParts.length >= 3) {
      const parentPath = '/' + pathParts.slice(0, 2).join('/');
      // 检查是否是主页面相关路由
      if (parentPath === '/user/pets' || parentPath === '/user/apply' || 
          parentPath === '/user/assess') {
        if (parentPath === '/user/pets') {
          currentTab.value = 'pets';
        } else if (parentPath === '/user/apply') {
          currentTab.value = 'apply';
        } else if (parentPath === '/user/assess') {
          currentTab.value = 'monitor';
        }
        showMainPageView.value = false;
        isTabNavigation.value = true;
        router.push(parentPath).catch(() => {});
        return;
      }
    }
  }
  
  // 默认情况：使用浏览器历史记录返回
  // 但需要先检查上一个路由，以便正确设置状态
  const historyState = window.history.state;
  if (window.history.length > 1) {
    // 使用 router.go(-1) 返回，路由监听器会自动处理状态更新
    router.go(-1);
  } else {
    // 如果没有历史记录，返回到主页
    currentTab.value = 'home';
    isTabNavigation.value = true;
    showMainPageView.value = true;
    router.push('/user').catch(() => {});
  }
}

interface MenuItem {
  label: string;
  path: string;
  icon: string;
}

const userInfo = reactive<UserInfo>({
  id: '',
  username: '',
  name: '',
  role: 3 // 用户角色
});

const showProfileDialog = ref(false);
const profileSaving = ref(false);
const profileFormRef = ref<FormInstance>();
const chatUnreadCount = ref(0);
const unreadMessageCount = ref(0);
const showMoreMenu = ref(false);

const profileForm = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  address: ''
});

// 用户端菜单项（按照原型图样式 - 平级列表）
const menuItems: MenuItem[] = [
  {
    label: '首页',
    path: '/user',
    icon: '🏠'
  },
  {
    label: '宠物管理',
    path: '/user/pets',
    icon: '🐾'
  },
  {
    label: '预约管理',
    path: '/user/apply',
    icon: '📅'
  },
  {
    label: '健康监测',
    path: '/user/assess',
    icon: '❤️'
  },
  {
    label: '诊断记录',
    path: '/user/diagnosis',
    icon: '📋'
  },
  {
    label: '消息中心',
    path: '/user/message',
    icon: '💬'
  },
  {
    label: '个人中心',
    path: '/user/mine',
    icon: '👤'
  }
];

const currentPath = computed(() => route.path);

// 计算是否应该显示底部导航栏（在移动端，且在主页面相关路由下）
const shouldShowBottomNav = computed(() => {
  if (!isMobile.value) return false;
  const path = route.path;
  // 在这些路由下显示底部导航栏
  return path === '/user' || 
         path === '/user/pets' || 
         path === '/user/apply' || 
         path === '/user/assess';
});

function navigate(path: string) {
  // 移动端：标记这是功能卡片点击
  if (isMobile.value) {
    // 重置tab导航标志，确保功能卡片点击优先
    isTabNavigation.value = false;
    isFunctionCardClick.value = true;
    // 如果当前路由就是目标路由，先导航到主页再导航到目标路由，确保路由变化被触发
    if (route.path === path) {
      router.push('/user').then(() => {
        nextTick(() => {
          isTabNavigation.value = false;
          isFunctionCardClick.value = true;
          router.push(path).catch(() => {
            // 忽略导航错误
          });
        });
      }).catch(() => {
        // 忽略导航错误
      });
    } else {
      router.push(path).catch(() => {
        // 忽略导航错误，但确保视图更新
        if (route.path !== path) {
          // 如果路由确实变化了，手动更新视图
          showMainPageView.value = false;
        }
      });
    }
  } else {
    // 电脑端直接导航
    if (path === '/user') {
      // 如果点击首页，显示主页视图
      showMainPageView.value = true;
      currentTab.value = 'home';
    } else {
      showMainPageView.value = false;
    }
    router.push(path);
  }
}

// 处理子组件的导航事件
function handleChildNavigate(path: string) {
  // 移动端：标记这是功能卡片点击
  if (isMobile.value) {
    // 重置tab导航标志，确保功能卡片点击优先
    isTabNavigation.value = false;
    isFunctionCardClick.value = true;
    // 如果当前路由就是目标路由，先导航到主页再导航到目标路由，确保路由变化被触发
    if (route.path === path) {
      router.push('/user').then(() => {
        nextTick(() => {
          isTabNavigation.value = false;
          isFunctionCardClick.value = true;
          router.push(path).catch(() => {
            // 忽略导航错误
          });
        });
      }).catch(() => {
        // 忽略导航错误
      });
    } else {
      router.push(path).catch(() => {
        // 忽略导航错误，但确保视图更新
        if (route.path !== path) {
          // 如果路由确实变化了，手动更新视图
          showMainPageView.value = false;
        }
      });
    }
  } else {
    // 电脑端直接导航
    router.push(path);
  }
}

function switchTab(tab: 'home' | 'pets' | 'apply' | 'monitor') {
  currentTab.value = tab;
  // 切换tab时，根据tab类型决定显示主页面视图还是路由视图
  if (isMobile.value) {
    // 设置标志，表示这是tab导航
    isTabNavigation.value = true;
    
    // 根据tab跳转到对应页面
    if (tab === 'home') {
      showMainPageView.value = true;
      router.push('/user').catch(() => {});
    } else {
      // 对于其他tab，显示路由视图
      showMainPageView.value = false;
      if (tab === 'pets') {
      router.push('/user/pets').catch(() => {});
    } else if (tab === 'apply') {
      router.push('/user/apply').catch(() => {});
    } else if (tab === 'monitor') {
      router.push('/user/assess').catch(() => {});
      }
    }
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

// 跳转到消息页面
function goToMessage() {
  router.push('/user/message');
}

// 跳转到聊天页面（包含聊天和聊天申请）
function goToChat() {
  router.push('/user/chat');
}

// 跳转到用户主页
function goToMine() {
  router.push('/user/mine');
}

// 获取聊天未读消息数
async function fetchChatUnreadCount() {
  try {
    // 获取聊天未读消息数
    const count = await getUnreadChatMessageCount();
    chatUnreadCount.value = typeof count === 'number' ? count : 0;
  } catch (e) {
    console.error('获取聊天未读消息数失败:', e);
    chatUnreadCount.value = 0;
  }
}

// 处理聊天消息已读事件
function handleChatMessageRead() {
  console.log('UserLayout收到聊天消息已读事件，刷新未读数量');
  fetchChatUnreadCount();
}

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
          await refreshUserInfo();
          showProfileDialog.value = false;
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

// 强制从API刷新用户信息
async function refreshUserInfo() {
  try {
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    
    let userData = data;
    if (data && data.data) {
      userData = data.data;
    } else if (data && (data.result === 'success' || data.status === 'SUCCESS')) {
      userData = data;
    }
    
    if (userData && userData.id) {
      // 只有在获取到有效数据时才更新
      Object.assign(userInfo, {
        id: String(userData.id || ''),
        username: userData.username || userInfo.username || '',
        name: userData.name || userInfo.name || '',
        phone: userData.phone || userInfo.phone || '',
        role: 3
      });
      
      saveUserInfo(userInfo);
      
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
    // 从localStorage恢复用户信息
    const savedUserInfo = getUserInfo();
    if (savedUserInfo) {
      Object.assign(userInfo, savedUserInfo);
    }
  }
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
    
    // 3. 从后端获取最新的用户信息
    const apiUserInfo = await getCurrentUserInfo();
    
    if (apiUserInfo && apiUserInfo.id) {
      // 4. 检查后端返回的用户信息是否与本地一致
      if (apiUserInfo.id !== savedUserInfo.id) {
        console.warn('检测到用户切换：本地用户ID:', savedUserInfo.id, '后端用户ID:', apiUserInfo.id);
        
        // 5. 检查后端返回的用户角色是否与当前路由匹配
        if (apiUserInfo.role !== expectedRole) {
          // 6. 拒绝切换，清除本地存储，重定向到登录页
          console.error('检测到用户切换，但角色不匹配，拒绝切换');
          console.error('本地用户角色:', savedUserInfo.role, '后端用户角色:', apiUserInfo.role, '期望角色:', expectedRole);
          clearUserInfo();
          showMessage('检测到用户切换，但角色不匹配，请重新登录', 'error');
          router.push('/');
          return;
        } else {
          // 角色匹配，但用户ID不同，可能是同一角色的不同用户
          // 这种情况可能是正常的（比如用户A退出，用户B登录），允许切换
          // 注意：不清除Token，只清除旧的用户信息，因为Token仍然有效
          console.warn('用户ID不同，但角色匹配，允许切换');
          clearUserInfoOnly(savedUserInfo.id, savedUserInfo.role);
        }
      }
      
      // 使用后端返回的用户信息
      Object.assign(userInfo, {
        ...apiUserInfo,
        role: apiUserInfo.role || ROLE_USER
      });
      saveUserInfo(userInfo);
      console.log('加载用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role);
    } else {
      // 后端没有返回用户信息，但本地有，可能是未登录状态
      // 使用本地存储的用户信息（可能是缓存的）
      Object.assign(userInfo, {
        ...savedUserInfo,
        role: savedUserInfo.role || ROLE_USER
      });
      console.warn('后端未返回用户信息，使用本地缓存，主机名:', window.location.hostname);
    }
  } else {
    // 本地没有用户信息，或者角色不匹配，从后端获取
    console.log('本地没有用户信息或角色不匹配，从后端获取');
    const apiUserInfo = await getCurrentUserInfo();
    
    if (apiUserInfo && apiUserInfo.id) {
      // 检查后端返回的用户角色是否与当前路由匹配
      if (apiUserInfo.role !== expectedRole) {
        console.error('后端返回的用户角色与当前路由不匹配，拒绝加载');
        console.error('后端用户角色:', apiUserInfo.role, '期望角色:', expectedRole);
        clearUserInfo();
        showMessage('用户角色与当前路由不匹配，请重新登录', 'error');
        router.push('/');
        return;
      }
      
      // 使用后端返回的用户信息
      Object.assign(userInfo, {
        ...apiUserInfo,
        role: apiUserInfo.role || ROLE_USER
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
  
  // 不需要展开菜单组了
  
  // 加载聊天未读消息数
  fetchChatUnreadCount();
  
  // 加载通知未读消息数
  const notification = useNotification();
  notification.fetchUnreadCount().then(() => {
    unreadMessageCount.value = notification.unreadCount.value;
  });
  
  // 定期刷新未读消息数
  setInterval(() => {
    notification.fetchUnreadCount().then(() => {
      unreadMessageCount.value = notification.unreadCount.value;
    });
  }, 30000);
}

let chatUnreadInterval: NodeJS.Timeout | null = null;

onMounted(() => {
  loadUserInfo();
  checkMobile();
  window.addEventListener('resize', checkMobile);
  
  // 定期刷新聊天未读消息数
  chatUnreadInterval = setInterval(() => {
    if (userInfo.id) {
      fetchChatUnreadCount();
    }
  }, 30000); // 每30秒刷新一次
  
  // 添加WebSocket消息处理器，用于处理聊天消息
  websocketManager.addMessageHandler((message: any) => {
    // 处理聊天消息，更新聊天未读数量
    if (message.type === 'chat' && message.data) {
      console.log('UserLayout收到聊天消息，更新未读数量');
      fetchChatUnreadCount();
    }
  });
  
  // 监听聊天消息已读事件，刷新未读数量
  window.addEventListener('chat-message-read', handleChatMessageRead);
  
  // 检查初始路径，如果是主页面路径，显示主页面组件
  const currentPath = route.path;
  if (currentPath === '/user') {
    // 主页面路径，显示主页面视图
    showMainPageView.value = true;
    currentTab.value = 'home';
  } else if (currentPath.startsWith('/user/') && currentPath !== '/user') {
    // 子路由路径，显示路由视图
    showMainPageView.value = false;
  }
  
  // 使用 nextTick 确保移动端检测完成后再设置视图
  nextTick(() => {
    // 如果路径是 /user，确保显示主页视图
    if (route.path === '/user') {
      showMainPageView.value = true;
      currentTab.value = 'home';
    }
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
  window.removeEventListener('chat-message-read', handleChatMessageRead);
  if (chatUnreadInterval) {
    clearInterval(chatUnreadInterval);
  }
});

// 用于跟踪是否是用户主动点击tab（而不是点击功能卡片）
const isTabNavigation = ref(false);

// 用于跟踪是否是用户主动点击功能卡片（用于区分路由重定向和主动跳转）
const isFunctionCardClick = ref(false);

// 监听路由变化，自动切换视图和刷新未读数量
watch(() => route.path, (newPath, oldPath) => {
  // 当从聊天窗口返回时，刷新未读数量
  if (oldPath && oldPath.startsWith('/user/chat/') && !newPath.startsWith('/user/chat/')) {
    // 从聊天窗口返回，刷新未读数量
    fetchChatUnreadCount();
  }
  // 电脑端：如果路径是 /user，确保显示主页视图
  if (!isMobile.value) {
    if (newPath === '/user') {
      showMainPageView.value = true;
      currentTab.value = 'home';
    } else {
      showMainPageView.value = false;
    }
    return;
  }
  
  // 移动端：根据路由路径和导航类型自动切换视图
  if (isMobile.value) {
    // 如果路径正好是 /user，显示主页面视图
    if (newPath === '/user') {
      showMainPageView.value = true;
      currentTab.value = 'home'; // 确保设置为home tab
      // 只有在确实是tab导航时才重置标志，否则保留功能卡片点击标志
      if (isTabNavigation.value) {
        isTabNavigation.value = false;
        isFunctionCardClick.value = false;
      }
      return;
    } 
    
    // 如果路径是子路由
    if (newPath.startsWith('/user/') && newPath !== '/user') {
      // 根据路径同步currentTab（用于底部导航栏高亮）
      if (newPath === '/user/pets') {
        currentTab.value = 'pets';
      } else if (newPath === '/user/apply') {
        currentTab.value = 'apply';
      } else if (newPath === '/user/assess') {
        currentTab.value = 'monitor';
      }
      
      // 优先检查是否是功能卡片点击（功能卡片点击优先级更高）
      if (isFunctionCardClick.value) {
        showMainPageView.value = false;
        isFunctionCardClick.value = false; // 重置标志
        isTabNavigation.value = false; // 同时重置tab导航标志
        return;
      }
      
      // 如果是通过tab导航（点击底部导航），显示路由视图（因为switchTab已经设置了showMainPageView）
      if (isTabNavigation.value) {
        // switchTab函数已经根据tab类型设置了showMainPageView的值
        // 这里只需要重置标志即可
        isTabNavigation.value = false; // 重置标志
        isFunctionCardClick.value = false; // 重置标志
        return;
      } 
      
      // 其他情况：显示路由视图（用户直接访问子路由或从其他页面跳转）
      showMainPageView.value = false;
    }
  }
}, { immediate: true });
</script>

<style scoped>
.user-layout {
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
  background-image: none;
  background: none;
}


/* 移动端返回按钮 */
.back-button {
  background: none;
  border: none;
  padding: 8px 12px;
  margin-right: 8px;
  cursor: pointer;
  color: #72C1BB;
  font-size: 20px;
  font-weight: bold;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s, color 0.2s;
  min-width: 40px;
  height: 36px;
}

.back-button:hover {
  background-color: rgba(114, 193, 187, 0.15);
  color: #a5f3eb;
}

.back-button:active {
  transform: scale(0.95);
}

.logo img {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo .title {
  color: #72C1BB;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
  line-height: 1.2;
}

.logo .subtitle {
  color: #72C1BB;
  font-size: 12px;
  opacity: 0.8;
  line-height: 1;
}

.user-info {
  color: white;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.user-info .username {
  color: #72C1BB;
  margin: 0 5px;
}

.notification-button,
.help-button,
.user-avatar-button {
  position: relative;
  cursor: pointer;
  margin: 0 8px;
  padding: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  border-radius: 4px;
}

.notification-button:hover,
.help-button:hover,
.user-avatar-button:hover {
  background-color: rgba(114, 193, 187, 0.1);
}

.notification-icon,
.help-icon,
.user-avatar-icon {
  font-size: 20px;
  color: #72C1BB;
}

.notification-button .notification-badge,
.help-button .chat-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff4d4f;
  color: white;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #2b2b2b;
}

.notification-button-mobile,
.chat-button-mobile,
.user-avatar-button-mobile {
  position: relative;
  cursor: pointer;
  margin-left: 12px;
  padding: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.notification-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid #2b2b2b;
}

.chat-icon {
  font-size: 20px;
  color: #72C1BB;
}

.chat-button-mobile .chat-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff4d4f;
  color: white;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #2b2b2b;
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

/* 移动端用户信息样式（只显示用户名，无下拉菜单） */
.user-info-mobile {
  color: white;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.user-info-mobile .username {
  color: #72C1BB;
  margin: 0;
}

.main-area {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.left-menu {
  width: 200px;
  background-color: rgba(255, 255, 255, 1);
  overflow-y: auto;
  flex-shrink: 0;
  border-right: 1px solid rgba(224, 224, 224, 1);
  padding: 8px 0;
}

.menu-item {
  padding: 14px 20px;
  color: #333;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  font-size: 15px;
  border-left: 3px solid transparent;
  margin: 0;
}

.menu-item:hover {
  background-color: #f0f0f0;
}

.menu-item.active {
  background-color: #e8f5f3;
  color: #72C1BB;
  border-left: 3px solid #72C1BB;
  font-weight: 600;
}

.menu-icon {
  font-size: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: white;
  transition: margin-left 0.2s ease, width 0.2s ease;
}

.content-area.with-bottom-nav {
  padding-bottom: 70px;
}

/* 移动端底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: color 0.2s;
  color: #999;
}

.nav-item.active {
  color: #72C1BB;
}

.nav-icon {
  font-size: 24px;
  margin-bottom: 4px;
  position: relative;
}

.nav-icon .notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 1px solid white;
}

.nav-label {
  font-size: 12px;
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
  
  .content-area {
    padding: 10px;
    width: 100%;
  }
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

@media (max-width: 767px) {
  :deep(.el-dialog) {
    width: 90vw !important;
    margin: 5vh auto;
  }
}
</style>
