<template>
  <div class="doctor-layout">
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
          <span v-if="!isMobile || (isMobile && !showMainPageView)" class="subtitle">医生端</span>
        </div>
      </div>
      <div class="user-info" v-if="!isMobile">
        <!-- 通知按钮 -->
        <div class="notification-button" @click="goToMessage" title="通知">
          <span class="notification-icon">🔔</span>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </div>
        <!-- 聊天按钮 -->
        <div class="help-button" @click="goToChat" title="聊天">
          <span class="help-icon">💬</span>
          <span v-if="chatUnreadCount > 0" class="chat-badge">
            {{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}
          </span>
        </div>
        <!-- 用户头像/信息 -->
        <div class="user-avatar-button" @click="goToMine" title="医生主页">
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
          <span v-if="unreadCount > 0" class="notification-dot"></span>
        </div>
        <!-- 聊天按钮 -->
        <div class="chat-button-mobile" @click="goToChat" title="聊天">
          <span class="chat-icon">💬</span>
          <span v-if="chatUnreadCount > 0" class="chat-badge">
            {{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}
          </span>
        </div>
        <!-- 用户头像 -->
        <div class="user-avatar-button-mobile" @click="goToMine" title="医生主页">
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
          :class="{ active: currentPath === item.path || (item.path === '/doctor' && currentPath === '/doctor') }"
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
          :class="{ active: currentTab === 'apply' }"
          @click="switchTab('apply')"
        >
          <div class="nav-icon">📅</div>
          <div class="nav-label">预约管理</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'health' }"
          @click="switchTab('health')"
        >
          <div class="nav-icon">❤️</div>
          <div class="nav-label">宠物健康史</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'schedule' }"
          @click="switchTab('schedule')"
        >
          <div class="nav-icon">⏰</div>
          <div class="nav-label">排班管理</div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area" :class="{ 'with-bottom-nav': shouldShowBottomNav }">
        <!-- 移动端显示核心页面或路由内容 -->
        <template v-if="isMobile">
          <!-- 如果在主页面，显示对应的主页面组件 -->
          <template v-if="showMainPageView">
            <DoctorHome v-if="currentTab === 'home'" @navigate="handleChildNavigate" />
          </template>
          <!-- 如果在子路由页面，显示路由内容 -->
          <router-view v-else />
        </template>
        <!-- 电脑端：如果是首页路径，显示主页组件，否则显示路由内容 -->
        <template v-else>
          <DoctorHome v-if="route.path === '/doctor'" @navigate="handleChildNavigate" />
          <router-view v-else />
        </template>
      </div>
    </div>

    <!-- 消息提醒弹窗 -->
    <NotificationModal
      v-model="showNotificationModal"
      :message="notificationMessage"
      @close="showNotificationModal = false"
    />

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
import { getToken } from '../../utils/token';
import { showMessage, showConfirm } from '../../utils/message';
import { useNotification } from '../../composables/useNotification';
import NotificationModal from '../../components/NotificationModal.vue';
import '../../assets/notification.css';
import DoctorHome from './DoctorHome.vue';
import DoctorMine from './DoctorMine.vue';
import DoctorMessage from './DoctorMessage.vue';
import DoctorChatList from './DoctorChatList.vue';
import { getChatRequestList, getChatSessionList, getUnreadChatMessageCount } from '../../api/chat';
import { websocketManager } from '../../utils/websocket';

const router = useRouter();
const route = useRoute();

// 移动端检测
const isMobile = ref(false);
const currentTab = ref<'home' | 'apply' | 'health' | 'schedule'>('home');

// 用于跟踪是否应该显示主页面视图（而不是路由视图）
const showMainPageView = ref(true);

// 用于跟踪是否是用户主动点击tab（而不是点击功能卡片）
const isTabNavigation = ref(false);

// 用于跟踪是否是用户主动点击功能卡片（用于区分路由重定向和主动跳转）
const isFunctionCardClick = ref(false);

function checkMobile() {
  isMobile.value = window.innerWidth < 768;
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
  role: 2 // 医生角色
});

// 消息提醒相关
const {
  unreadCount,
  fetchUnreadCount,
  checkOnLogin,
  initWebSocket,
  disconnectWebSocket,
  setOnNewMessageCallback,
  clearOnNewMessageCallback
} = useNotification();

// 聊天未读消息数（包括申请和聊天消息）
const chatUnreadCount = ref(0);

// 总未读消息数（通知消息 + 聊天消息）
const totalUnreadCount = computed(() => {
  return unreadCount.value + chatUnreadCount.value;
});

// 获取聊天未读消息数
async function fetchChatUnreadCount() {
  try {
    let count = 0;
    // 获取待处理的聊天申请数
    const requests = await getChatRequestList(0); // status=0表示待审核
    count += (requests || []).length;
    
    // 获取聊天会话的未读消息数
    const sessions = await getChatSessionList();
    if (sessions && sessions.length > 0) {
      for (const session of sessions) {
        const unread = await getUnreadChatMessageCount(session.id);
        count += unread || 0;
      }
    }
    
    chatUnreadCount.value = count;
  } catch (e) {
    console.error('获取聊天未读消息数失败:', e);
    chatUnreadCount.value = 0;
  }
}

const showNotificationModal = ref(false);
const notificationMessage = ref('您有新消息');

// 处理新消息到达（用于显示弹窗和更新未读数量）
function handleNewMessage(message: any) {
  console.log('DoctorLayout收到新消息:', message);
  
  try {
    if (!message) {
      return;
    }
    
    // 检查消息类型
    const messageTitle = message.title || '';
    const messageType = message.type || '';
    
    // 如果是聊天消息，更新聊天未读数量，不弹窗
    if (messageType === 'chat' || (messageTitle && messageTitle.includes('聊天') && messageTitle !== '聊天申请')) {
      console.log('收到聊天消息，更新聊天未读数量');
      // 刷新聊天未读消息数
      fetchChatUnreadCount();
      return;
    }
    
    // 如果是聊天申请，更新聊天未读数量并弹窗
    if (messageTitle === '聊天申请') {
      console.log('收到聊天申请，更新未读数量并弹窗');
      fetchChatUnreadCount();
      notificationMessage.value = '您有新的好友申请';
      nextTick(() => {
        showNotificationModal.value = true;
      });
      return;
    }
    
    // 只有预约消息才弹窗
    if (messageTitle !== '预约提醒') {
      console.log('非预约消息，不弹窗');
      return;
    }
    
    let messageText = '您有新消息';
    
    // 检查消息标题
    if (message.title) {
      messageText = message.title;
    }
    
    // 尝试解析消息内容（content字段是JSON字符串）
    if (message.content) {
      try {
        const content = typeof message.content === 'string' 
          ? JSON.parse(message.content) 
          : message.content;
        
        if (content && typeof content === 'object') {
          if (content.userName && content.appointmentTypeName) {
            messageText = `${content.userName}预约了${content.appointmentTypeName}`;
            if (content.appDate && content.timeSlot) {
              messageText += `，时间：${content.appDate} ${content.timeSlot}`;
            }
          } else if (content.userName) {
            messageText = `${content.userName}提交了新的预约申请`;
          }
        }
      } catch (e) {
        console.warn('解析消息内容失败:', e, '原始content:', message.content);
      }
    }
    
    notificationMessage.value = messageText;
    
    // 使用nextTick确保DOM已更新
    nextTick(() => {
      showNotificationModal.value = true;
    });
  } catch (e) {
    console.error('处理新消息失败:', e, '消息对象:', message);
  }
}

const showProfileDialog = ref(false);
const profileSaving = ref(false);
const profileFormRef = ref<FormInstance>();

const profileForm = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  address: ''
});

// 医生端菜单项
const menuItems: MenuItem[] = [
  { label: '首页', path: '/doctor', icon: '🏠' },
  { label: '预约管理', path: '/doctor/apply', icon: '📅' },
  { label: '宠物健康史', path: '/doctor/diagnosis', icon: '❤️' },
  { label: '排班管理', path: '/doctor/schedule', icon: '⏰' },
  { label: '药品管理', path: '/doctor/medicine', icon: '💊' },
  { label: '开药记录', path: '/doctor/medicine-record', icon: '📝' },
  { label: '消息中心', path: '/doctor/message', icon: '💬' },
  { label: '更多设置', path: '/doctor/more', icon: '⚙️' }
];

const currentPath = computed(() => route.path);

// 是否显示底部导航栏
const shouldShowBottomNav = computed(() => {
  return isMobile.value && (
    route.path === '/doctor' || 
    route.path === '/doctor/apply' || 
    route.path === '/doctor/diagnosis' || 
    route.path === '/doctor/schedule'
  );
});

function navigate(path: string) {
  // 移动端：标记这是功能卡片点击
  if (isMobile.value) {
    // 重置tab导航标志，确保功能卡片点击优先
    isTabNavigation.value = false;
    isFunctionCardClick.value = true;
    // 如果当前路由就是目标路由，先导航到主页再导航到目标路由，确保路由变化被触发
    if (route.path === path) {
      router.push('/doctor').then(() => {
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
    if (path === '/doctor') {
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
      router.push('/doctor').then(() => {
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

function switchTab(tab: 'home' | 'apply' | 'health' | 'schedule') {
  currentTab.value = tab;
  // 切换tab时，根据tab类型决定显示主页面视图还是路由视图
  if (isMobile.value) {
    // 设置标志，表示这是tab导航
    isTabNavigation.value = true;
    isFunctionCardClick.value = false; // 重置标志
    
    // 根据tab跳转到对应页面
    if (tab === 'home') {
      showMainPageView.value = true;
      router.push('/doctor').catch(() => {});
    } else {
      // 对于其他tab，显示路由视图
      showMainPageView.value = false;
      if (tab === 'apply') {
        router.push('/doctor/apply').catch(() => {});
      } else if (tab === 'health') {
        router.push('/doctor/diagnosis').catch(() => {});
      } else if (tab === 'schedule') {
        router.push('/doctor/schedule').catch(() => {});
      }
    }
  }
}

// 返回按钮点击事件
function goBack() {
  const currentPath = route.path;
  
  // 定义路由的层级关系：子路由 -> 父路由
  const routeHierarchy: Record<string, string> = {
    '/doctor/prescribe-medicine': '/doctor/diagnosis',  // 开具药品 -> 宠物健康史
    '/doctor/pet-detail': '/doctor/diagnosis',          // 宠物详情 -> 宠物健康史
    '/doctor/apply-flow': '/doctor/apply',              // 预约流程 -> 预约管理
  };
  
  // 检查是否有明确的父路由
  if (routeHierarchy[currentPath]) {
    const parentPath = routeHierarchy[currentPath];
    if (parentPath === '/doctor' || parentPath === '/doctor/apply' || 
        parentPath === '/doctor/diagnosis' || parentPath === '/doctor/schedule') {
      if (parentPath === '/doctor') {
        currentTab.value = 'home';
        showMainPageView.value = true;
      } else if (parentPath === '/doctor/apply') {
        currentTab.value = 'apply';
        showMainPageView.value = false;
      } else if (parentPath === '/doctor/diagnosis') {
        currentTab.value = 'health';
        showMainPageView.value = false;
      } else if (parentPath === '/doctor/schedule') {
        currentTab.value = 'schedule';
        showMainPageView.value = false;
      }
      isTabNavigation.value = true;
      router.push(parentPath).catch(() => {});
      return;
    }
  }
  
  // 如果当前路径是子路由，尝试推断父路由
  if (currentPath.startsWith('/doctor/')) {
    const pathParts = currentPath.split('/').filter(p => p);
    if (pathParts.length >= 2) {
      const parentPath = '/' + pathParts.slice(0, 2).join('/');
      if (parentPath === '/doctor/apply' || parentPath === '/doctor/diagnosis' || 
          parentPath === '/doctor/schedule') {
        if (parentPath === '/doctor/apply') {
          currentTab.value = 'apply';
        } else if (parentPath === '/doctor/diagnosis') {
          currentTab.value = 'health';
        } else if (parentPath === '/doctor/schedule') {
          currentTab.value = 'schedule';
        }
        showMainPageView.value = false;
        isTabNavigation.value = true;
        router.push(parentPath).catch(() => {});
        return;
      }
    }
  }
  
  // 默认情况：使用浏览器历史记录返回
  if (window.history.length > 1) {
    router.go(-1);
  } else {
    // 如果没有历史记录，返回到主页
    currentTab.value = 'home';
    isTabNavigation.value = true;
    showMainPageView.value = true;
    router.push('/doctor').catch(() => {});
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
        role: 2
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
  let finalUserInfo: UserInfo | null = null;
  
  // 3. 如果本地有用户信息，且角色与当前路由匹配，进行双重验证
  if (savedUserInfo && savedUserInfo.id && savedUserInfo.role === expectedRole) {
    console.log('本地有用户信息，且角色匹配，进行双重验证');
    
    // 3. 从后端获取最新的用户信息（注意：此时Token应该已经存在）
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
          // 这种情况可能是正常的（比如医生A退出，医生B登录），允许切换
          console.warn('用户ID不同，但角色匹配，允许切换');
          // 只清除用户信息，保留Token
          clearUserInfoOnly(savedUserInfo.id, savedUserInfo.role);
        }
      }
      
      // 使用后端返回的用户信息
      Object.assign(userInfo, {
        ...apiUserInfo,
        role: apiUserInfo.role || ROLE_DOCTOR
      });
      saveUserInfo(userInfo);
      finalUserInfo = { ...userInfo };
      console.log('加载用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role);
    } else {
      // 后端没有返回用户信息，但本地有，可能是未登录状态
      // 使用本地存储的用户信息（可能是缓存的）
      Object.assign(userInfo, {
        ...savedUserInfo,
        role: savedUserInfo.role || ROLE_DOCTOR
      });
      finalUserInfo = { ...userInfo };
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
        role: apiUserInfo.role || ROLE_DOCTOR
      });
      saveUserInfo(userInfo);
      finalUserInfo = { ...userInfo };
      console.log('加载用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role);
    } else {
      // 后端也没有返回用户信息
      console.error('无法获取用户信息，请重新登录');
      clearUserInfo();
      router.push('/');
      return;
    }
  }
  

  
  // 加载用户信息后，初始化消息提醒功能
  // 使用finalUserInfo确保有用户信息
  const userId = finalUserInfo?.id || userInfo.id;
  if (userId) {
    console.log('初始化消息提醒功能，用户ID:', userId);
    
    // 确保Token存在
    const token = getToken();
    if (!token) {
      console.warn('Token不存在，无法初始化消息提醒功能');
      return;
    }
    
    // 设置新消息回调（用于显示弹窗）
    setOnNewMessageCallback(handleNewMessage);
    
    // 初始化WebSocket连接
    initWebSocket();
    
    // 延迟获取未读消息数量，确保Token已经正确设置
    // 使用setTimeout确保在下一个事件循环中执行，给Token设置足够的时间
    setTimeout(async () => {
      try {
        // 再次检查Token是否存在
        const currentToken = getToken();
        if (!currentToken) {
          console.warn('Token不存在，跳过消息提醒初始化');
          return;
        }
        
        // 获取未读消息数量
        await fetchUnreadCount();
        console.log('未读消息数量:', unreadCount.value);
        
        // 登录时检查未读消息
        const checkResult = await checkOnLogin();
        console.log('checkOnLogin结果:', checkResult);
        
        if (checkResult && checkResult.hasUnread) {
          console.log('显示消息弹窗:', checkResult.message);
          notificationMessage.value = checkResult.message;
          // 使用nextTick确保DOM已更新
          nextTick(() => {
            showNotificationModal.value = true;
          });
        } else {
          console.log('没有未读消息，不显示弹窗');
        }
      } catch (e) {
        console.error('初始化消息提醒功能失败:', e);
      }
    }, 100); // 延迟100ms，确保Token已经设置
  } else {
    console.warn('用户ID为空，无法初始化消息提醒功能');
  }
}

// 跳转到消息页面
function goToMessage() {
  if (isMobile.value) {
    // 移动端消息功能暂时保留，但不在底部导航显示
    router.push('/doctor/message');
    // 进入消息页面后，清除未读数量（标记为已读）
    // 注意：这里只是清除显示，实际已读状态由消息页面处理
    nextTick(() => {
      fetchUnreadCount(); // 刷新未读数量
    });
  } else {
    router.push('/doctor/message');
  }
}

// 跳转到聊天页面
function goToChat() {
  router.push('/doctor/chat');
}

function goToMine() {
  // 检查用户角色
  const currentUserInfo = getUserInfo();
  if (currentUserInfo && currentUserInfo.role === ROLE_ADMIN) {
    // 管理员：跳转到管理员用户管理页面（个人中心功能在MainLayout的下拉菜单中）
    router.push('/admin/users');
  } else {
    // 医生：跳转到医生个人中心
    router.push('/doctor/mine');
  }
}

onMounted(() => {
  loadUserInfo();
  checkMobile();
  window.addEventListener('resize', checkMobile);
  
  // 移动端：检查初始路径，如果是主页面路径，显示主页面组件
  if (isMobile.value) {
    const currentPath = route.path;
    if (currentPath === '/doctor') {
      // 主页面路径，显示主页面视图
      showMainPageView.value = true;
      currentTab.value = 'home';
    } else if (currentPath.startsWith('/doctor/') && currentPath !== '/doctor') {
      // 子路由路径，显示路由视图
      showMainPageView.value = false;
    }
  }
  
  // 移动端：如果路径是 /doctor，确保显示主页视图
  nextTick(() => {
    if (isMobile.value && route.path === '/doctor') {
      showMainPageView.value = true;
      currentTab.value = 'home';
    }
  });
  
  // 定期刷新聊天未读消息数
  const chatUnreadInterval = setInterval(() => {
    if (userInfo.id) {
      fetchChatUnreadCount();
    }
  }, 30000); // 每30秒刷新一次
  
  onUnmounted(() => {
    clearInterval(chatUnreadInterval);
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
  // 清除新消息回调
  clearOnNewMessageCallback();
  // 断开WebSocket连接
  disconnectWebSocket();
});

// 监听路由变化，自动切换视图
watch(() => route.path, (newPath, oldPath) => {
  // 当从聊天窗口返回时，刷新未读数量
  if (oldPath && oldPath.startsWith('/doctor/chat/') && !newPath.startsWith('/doctor/chat/')) {
    fetchChatUnreadCount();
  }
  
  // 电脑端：如果路径是 /doctor，确保显示主页视图
  if (!isMobile.value) {
    if (newPath === '/doctor') {
      showMainPageView.value = true;
      currentTab.value = 'home';
    } else {
      showMainPageView.value = false;
    }
    return;
  }
  
  // 移动端：根据路由路径和导航类型自动切换视图
  if (isMobile.value) {
    // 如果路径正好是 /doctor，显示主页面视图
    if (newPath === '/doctor') {
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
    if (newPath.startsWith('/doctor/') && newPath !== '/doctor') {
      // 根据路径同步currentTab（用于底部导航栏高亮）
      if (newPath === '/doctor/apply') {
        currentTab.value = 'apply';
        showMainPageView.value = false;
      } else if (newPath === '/doctor/diagnosis') {
        currentTab.value = 'health';
        showMainPageView.value = false;
      } else if (newPath === '/doctor/schedule') {
        currentTab.value = 'schedule';
        showMainPageView.value = false;
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
        return;
      }
      
      // 其他情况：显示路由视图（用户直接访问子路由或从其他页面跳转）
      showMainPageView.value = false;
    }
  }
}, { immediate: true });
</script>

<style scoped>
.doctor-layout {
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

.logo .title {
  color: #72C1BB;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
}


.user-info {
  color: white;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
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
  border: 2px solid white;
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

/* 移动端用户信息样式 */
.user-info-mobile {
  color: white;
  font-size: 14px;
  display: flex;
  align-items: center;
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

.notification-icon {
  font-size: 22px;
  color: #72C1BB;
}

.chat-icon {
  font-size: 20px;
  color: #72C1BB;
}

.user-avatar-icon {
  font-size: 20px;
  color: #72C1BB;
}

.notification-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid white;
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
  border: 2px solid white;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
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
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background-color: rgba(114, 193, 187, 0.1);
  color: #72C1BB;
}

.menu-item.active {
  background-color: rgba(114, 193, 187, 0.15);
  color: #72C1BB;
  border-left-color: #72C1BB;
  font-weight: 500;
}

.menu-icon {
  font-size: 20px;
  display: inline-block;
  width: 24px;
  text-align: center;
}

.menu-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
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
