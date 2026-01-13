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
          {{ isMobile && !showMainPageView ? (route.meta?.title as string || '详情') : '宠物医院管理系统' }}
        </span>
      </div>
      <div class="user-info" v-if="!isMobile">
        <span class="username">{{ userInfo.name || '医生' }}</span>
        <!-- 消息按钮 -->
        <div class="message-button" @click="goToMessage" title="消息">
          <span class="message-icon">💬</span>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </div>
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="dropdown-trigger">▼</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">修改个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <!-- 移动端只显示用户名，不显示下拉菜单 -->
      <div class="user-info-mobile" v-if="isMobile">
        <span class="username">{{ userInfo.name || '医生' }}</span>
      </div>
    </div>

    <div class="main-area">
      <!-- 电脑端左侧导航栏 -->
      <div 
        v-if="!isMobile"
        class="left-menu" 
        :class="{ 'collapsed': isCollapsed }"
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
            <span class="menu-text" v-show="!isCollapsed">{{ group.title }}</span>
            <span class="arrow" v-show="!isCollapsed">{{ expandedGroup === group.title ? '▼' : '▶' }}</span>
          </div>
          <ul v-show="expandedGroup === group.title && !isCollapsed" class="menu-list">
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

      <!-- 移动端底部导航栏（仅在主页面显示） -->
      <div v-if="isMobile && showMainPageView" class="bottom-nav">
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'home' }"
          @click="switchTab('home')"
        >
          <div class="nav-icon">🏠</div>
          <div class="nav-label">主页</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'message' }"
          @click="switchTab('message')"
        >
          <div class="nav-icon">
            💬
            <span v-if="unreadCount > 0" class="notification-badge"></span>
          </div>
          <div class="nav-label">消息</div>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentTab === 'mine' }"
          @click="switchTab('mine')"
        >
          <div class="nav-icon">👤</div>
          <div class="nav-label">我的</div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area" :class="{ 'with-bottom-nav': isMobile && showMainPageView }">
        <!-- 移动端显示核心页面或路由内容 -->
        <template v-if="isMobile">
          <!-- 如果在主页面（home/mine/more），显示对应的主页面组件 -->
          <template v-if="showMainPageView">
            <DoctorHome v-if="currentTab === 'home'" @navigate="handleChildNavigate" />
            <DoctorMine v-else-if="currentTab === 'mine'" />
            <DoctorMessage v-else-if="currentTab === 'message'" />
          </template>
          <!-- 如果在子路由页面，显示路由内容 -->
          <router-view v-else />
        </template>
        <!-- 电脑端显示路由内容 -->
        <router-view v-else />
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

const router = useRouter();
const route = useRoute();

// 移动端检测 & 左侧导航折叠（默认展开）
const isMobile = ref(false);
const isCollapsed = ref(false); // 默认展开
const currentTab = ref<'home' | 'mine' | 'message'>('home');

// 用于跟踪是否应该显示主页面视图（而不是路由视图）
const showMainPageView = ref(true);

// 用于跟踪是否是用户主动点击tab（而不是点击功能卡片）
const isTabNavigation = ref(false);

// 用于跟踪是否是用户主动点击功能卡片（用于区分路由重定向和主动跳转）
const isFunctionCardClick = ref(false);

function checkMobile() {
  isMobile.value = window.innerWidth < 768;
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

const showNotificationModal = ref(false);
const notificationMessage = ref('您有新消息');

// 处理新消息到达（用于显示弹窗）
function handleNewMessage(message: any) {
  console.log('DoctorLayout收到新消息，准备显示弹窗:', message);
  console.log('消息类型:', typeof message);
  console.log('消息内容:', JSON.stringify(message, null, 2));
  
  try {
    if (!message) {
      console.warn('消息为空，使用默认消息');
      notificationMessage.value = '您有新预约消息';
      nextTick(() => {
        showNotificationModal.value = true;
      });
      return;
    }
    
    let messageText = '您有新预约消息';
    
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
        
        console.log('解析后的消息内容:', content);
        
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
    console.log('设置弹窗消息:', messageText);
    
    // 使用nextTick确保DOM已更新
    nextTick(() => {
      console.log('显示弹窗，消息:', messageText);
      showNotificationModal.value = true;
    });
  } catch (e) {
    console.error('处理新消息失败:', e, '消息对象:', message);
    notificationMessage.value = '您有新预约消息';
    nextTick(() => {
      showNotificationModal.value = true;
    });
  }
}

const expandedGroup = ref('');
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

// 医生端菜单组
const menuGroups: MenuGroup[] = [
  {
    title: '宠物管理',
    icon: '🐾',
    items: [
      { label: '宠物健康史', path: '/doctor/diagnosis' }
    ]
  },
  {
    title: '预约管理',
    icon: '📅',
    items: [
      { label: '预约列表', path: '/doctor/apply' },
      { label: '医生时间', path: '/doctor/free-time' },
      { label: '排班管理', path: '/doctor/schedule' },
      { label: '服务类型管理', path: '/doctor/service-type' }
    ]
  },
  {
    title: '宠物档案',
    icon: '📊',
    items: [
      { label: '宠物日志', path: '/doctor/pet-daily' },
      { label: '日志图表', path: '/doctor/tj-daily' }
    ]
  },
  {
    title: '医院管理',
    icon: '🏥',
    items: [
      { label: '预约统计', path: '/doctor/tj-apply' },
      { label: '发布指南', path: '/doctor/notices' },
      { label: '标准制定', path: '/doctor/standards' }
    ]
  },
  {
    title: '药品管理',
    icon: '💊',
    items: [
      { label: '药品列表', path: '/doctor/medicine' },
      { label: '开药记录', path: '/doctor/medicine-record' }
    ]
  }
];

const currentPath = computed(() => route.path);

function toggleGroup(title: string) {
  expandedGroup.value = expandedGroup.value === title ? '' : title;
}

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

function switchTab(tab: 'home' | 'mine' | 'message') {
  currentTab.value = tab;
  // 切换tab时，显示主页面视图，不显示路由视图
  if (isMobile.value) {
    // 设置标志，表示这是tab导航
    isTabNavigation.value = true;
    isFunctionCardClick.value = false; // 重置标志
    showMainPageView.value = true;
    // 导航到 /doctor 路径（虽然会被重定向，但我们通过标志来控制显示主页面）
    router.push('/doctor').catch(() => {
      // 忽略导航重复的错误
    });
  }
}

// 返回按钮点击事件
function goBack() {
  // 返回主页的home tab
  currentTab.value = 'home';
  isTabNavigation.value = true; // 设置标志，表示这是返回操作
  isFunctionCardClick.value = false; // 重置标志
  showMainPageView.value = true;
  router.push('/doctor').catch(() => {
    // 忽略导航重复的错误
  });
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
  
  // 默认展开第一个菜单组
  if (menuGroups.length > 0) {
    expandedGroup.value = menuGroups[0].title;
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
    switchTab('message');
  } else {
    router.push('/doctor/message');
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
  
  // 电脑端：如果路径是 /doctor，自动重定向到第一个子路由 /doctor/apply
  // 移动端：保持 /doctor 路径，显示主页（DoctorHome组件）
  // 使用 nextTick 确保移动端检测完成
  nextTick(() => {
    if (!isMobile.value && route.path === '/doctor') {
      router.replace('/doctor/apply');
    }
    // 移动端：如果路径是 /doctor，确保显示主页视图
    if (isMobile.value && route.path === '/doctor') {
      showMainPageView.value = true;
      currentTab.value = 'home';
    }
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
  // 清除新消息回调
  clearOnNewMessageCallback();
  // 断开WebSocket连接
  disconnectWebSocket();
});

// 监听路由变化，自动展开对应的菜单组和切换视图
watch(() => route.path, (newPath, oldPath) => {
  // 电脑端：自动展开对应的菜单组
  if (!isMobile.value) {
  for (const group of menuGroups) {
    for (const item of group.items) {
      if (item.path === newPath) {
        expandedGroup.value = group.title;
        return;
        }
      }
    }
  }
  
  // 移动端：根据路由路径和导航类型自动切换视图
  if (isMobile.value) {
    // 如果路径正好是 /doctor，显示主页面视图
    if (newPath === '/doctor') {
      showMainPageView.value = true;
      // 只有在确实是tab导航时才重置标志，否则保留功能卡片点击标志
      if (isTabNavigation.value) {
        isTabNavigation.value = false;
        isFunctionCardClick.value = false;
      }
      return;
    } 
    
    // 如果路径是子路由
    if (newPath.startsWith('/doctor/') && newPath !== '/doctor') {
      // 优先检查是否是功能卡片点击（功能卡片点击优先级更高）
      if (isFunctionCardClick.value) {
        showMainPageView.value = false;
        isFunctionCardClick.value = false; // 重置标志
        isTabNavigation.value = false; // 同时重置tab导航标志
        return;
      }
      
      // 如果是通过tab导航（点击底部导航或返回按钮），显示主页面视图
      if (isTabNavigation.value) {
        showMainPageView.value = true;
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
.doctor-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.top-bar {
  height: 51px;
  background-color: #2b2b2b;
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

.user-info .username {
  color: #72C1BB;
  margin: 0 5px;
}

.message-button {
  position: relative;
  cursor: pointer;
  margin: 0 10px;
  padding: 5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.message-button:hover {
  transform: scale(1.1);
}

.message-icon {
  font-size: 20px;
}

.dropdown-trigger {
  color: #72C1BB;
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
  width: 180px;
  background-color: #72C1BB;
  overflow-y: auto;
  flex-shrink: 0;
  transition: width 0.2s ease;
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

.menu-group {
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.menu-title {
  padding: 12px 15px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s;
}

.menu-title:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.menu-title.active {
  background-color: rgba(0, 0, 0, 0.15);
}

.arrow {
  font-size: 10px;
}

.menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
  background-color: rgba(0, 0, 0, 0.05);
}

.menu-list li {
  padding: 10px 15px 10px 25px;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
}

.menu-list li:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.menu-list li.selected {
  background-color: rgba(0, 0, 0, 0.2);
  border-left: 3px solid white;
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
