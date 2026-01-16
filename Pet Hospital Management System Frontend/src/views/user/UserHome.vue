<template>
  <div class="user-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来, {{ userInfo.name || '用户' }}！</h1>
        <p class="welcome-date">今天是 {{ currentDate }}</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" @click="navigate('/user/apply')">
        <div class="stat-icon stat-icon-appointment">📅</div>
        <div class="stat-content">
          <div class="stat-value stat-value-appointment">{{ statistics.pendingAppointments }}</div>
          <div class="stat-label">待就诊预约</div>
      </div>
      </div>
      <div class="stat-card" @click="navigate('/user/pet-daily')">
        <div class="stat-icon stat-icon-log">📝</div>
        <div class="stat-content">
          <div class="stat-value stat-value-log">{{ statistics.petLogs }}</div>
          <div class="stat-label">宠物日志</div>
        </div>
      </div>
      <div class="stat-card" @click="navigate('/user/message')">
        <div class="stat-icon stat-icon-message">💬</div>
        <div class="stat-content">
          <div class="stat-value stat-value-message">{{ statistics.unreadMessages }}</div>
          <div class="stat-label">未读消息</div>
        </div>
      </div>
      </div>

    <!-- 功能中心 -->
    <div class="function-center">
      <h2 class="section-title">功能中心</h2>
      <div class="function-grid">
        <div class="function-item" @click="navigate('/user/pets')">
          <div class="function-icon function-icon-pet">🐾</div>
          <div class="function-label">宠物管理</div>
        </div>
        <div class="function-item" @click="navigate('/user/apply')">
          <div class="function-icon function-icon-appointment">📅</div>
          <div class="function-label">预约管理</div>
        </div>
        <div class="function-item" @click="navigate('/user/assess')">
          <div class="function-icon function-icon-monitor">❤️</div>
          <div class="function-label">健康监测</div>
        </div>
        <div class="function-item" @click="navigate('/user/notices')">
          <div class="function-icon function-icon-guide">📖</div>
          <div class="function-label">健康指南</div>
        </div>
        <div class="function-item" @click="navigate('/user/pet-daily')">
          <div class="function-icon function-icon-log">📝</div>
          <div class="function-label">宠物日志</div>
        </div>
        <div class="function-item" @click="navigate('/user/diagnosis')">
          <div class="function-icon function-icon-diagnosis">🏥</div>
          <div class="function-label">诊断记录</div>
        </div>
        <div class="function-item" @click="navigate('/user/standards')">
          <div class="function-icon function-icon-standard">📊</div>
          <div class="function-label">健康标准</div>
      </div>
        <div class="function-item" @click="navigate('/user/free-time')">
          <div class="function-icon function-icon-time">⏰</div>
          <div class="function-label">医生时间</div>
      </div>
        <div class="function-item" @click="navigate('/user/more')">
          <div class="function-icon function-icon-more">⋯</div>
          <div class="function-label">更多功能</div>
      </div>
      </div>
    </div>

    <!-- 数据面板（桌面端） -->
    <div v-if="!isMobile" class="data-panels">
      <!-- 近期预约 -->
      <div class="data-panel">
        <h3 class="panel-title">近期预约</h3>
        <div v-if="recentAppointments.length === 0" class="empty-state">
          <p>暂无预约记录</p>
        </div>
        <div v-else class="appointment-list">
          <div 
            v-for="appointment in recentAppointments" 
            :key="appointment.id"
            class="appointment-item"
            @click="navigate('/user/apply')"
          >
            <div class="appointment-info">
              <div class="appointment-type">{{ appointment.appointmentTypeName }}</div>
              <div class="appointment-details">
                <span class="appointment-doctor">{{ appointment.doctorName }}</span>
                <span class="appointment-date">{{ appointment.appDate }} {{ appointment.timeSlot }}</span>
              </div>
            </div>
          </div>
        </div>
        <button class="panel-button" @click="navigate('/user/apply')">预约就诊</button>
      </div>

      <!-- 健康趋势 -->
      <div class="data-panel">
        <h3 class="panel-title">健康趋势</h3>
        <div class="health-trends">
          <div class="trend-item">
            <div class="trend-label">体重监测</div>
            <div class="trend-progress">
              <div 
                class="trend-bar" 
                :class="`trend-bar-${healthTrends.weightStatus}`"
                :style="{ width: `${healthTrends.weightProgress}%` }"
              ></div>
            </div>
            <div class="trend-status" :class="`status-${healthTrends.weightStatus}`">
              {{ getStatusText(healthTrends.weightStatus) }}
            </div>
          </div>
          <div class="trend-item">
            <div class="trend-label">活动量</div>
            <div class="trend-progress">
              <div 
                class="trend-bar" 
                :class="`trend-bar-${healthTrends.activityStatus}`"
                :style="{ width: `${healthTrends.activityProgress}%` }"
              ></div>
            </div>
            <div class="trend-status" :class="`status-${healthTrends.activityStatus}`">
              {{ getActivityStatusText(healthTrends.activityStatus) }}
            </div>
          </div>
          <div class="trend-item">
            <div class="trend-label">饮食规律</div>
            <div class="trend-progress">
              <div 
                class="trend-bar" 
                :class="`trend-bar-${healthTrends.dietStatus}`"
                :style="{ width: `${healthTrends.dietProgress}%` }"
              ></div>
            </div>
            <div class="trend-status" :class="`status-${healthTrends.dietStatus}`">
              {{ getDietStatusText(healthTrends.dietStatus) }}
            </div>
          </div>
        </div>
        <button class="panel-button" @click="navigate('/user/tj-daily')">查看详细报告</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '../../utils/user';
import { getHomeStatistics, getRecentAppointments, getHealthTrends } from '../../api/statistics';
import type { HealthTrend } from '../../api/statistics';

const router = useRouter();
const userInfo = reactive(getUserInfo() || { name: '用户' });
const isMobile = ref(false);

// 统计数据
const statistics = reactive({
  pendingAppointments: 0,
  petLogs: 0,
  unreadMessages: 0
});

const recentAppointments = ref<any[]>([]);
const healthTrends = reactive<HealthTrend>({
  weightStatus: 'normal',
  activityStatus: 'normal',
  dietStatus: 'normal',
  weightProgress: 0,
  activityProgress: 0,
  dietProgress: 0
});

// 当前日期
const currentDate = computed(() => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
});

// 定义 emit
const emit = defineEmits<{
  navigate: [path: string];
}>();

function navigate(path: string) {
  emit('navigate', path);
  router.push(path);
}

function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    normal: '正常',
    warning: '警告',
    danger: '危险'
  };
  return statusMap[status] || '正常';
}

function getActivityStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    excellent: '优秀',
    good: '良好',
    normal: '正常',
    poor: '较差'
  };
  return statusMap[status] || '正常';
}

function getDietStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    excellent: '优秀',
    good: '良好',
    normal: '一般',
    poor: '较差'
  };
  return statusMap[status] || '一般';
}

// 检测移动端
function checkMobile() {
  isMobile.value = window.innerWidth < 768;
}

// 加载数据
async function loadData() {
  try {
    // 加载统计数据
    const stats = await getHomeStatistics();
    Object.assign(statistics, stats);
    
    // 加载近期预约
    const appointments = await getRecentAppointments(3);
    recentAppointments.value = appointments;
    
    // 加载健康趋势
    const trends = await getHealthTrends();
    Object.assign(healthTrends, trends);
  } catch (e) {
    console.error('加载数据失败:', e);
  }
}

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
  loadData();
});
</script>

<style scoped>
.user-home {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  color: white;
}

.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.welcome-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.welcome-date {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(114, 193, 187, 0.2);
}

.stat-icon {
  font-size: 48px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-icon-appointment {
  background: rgba(114, 193, 187, 0.1);
}

.stat-icon-log {
  background: rgba(255, 152, 0, 0.1);
}

.stat-icon-message {
  background: rgba(244, 67, 54, 0.1);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-value-appointment {
  color: #72C1BB;
}

.stat-value-log {
  color: #ff9800;
}

.stat-value-message {
  color: #f44336;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 功能中心 */
.function-center {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.function-item:hover {
  transform: translateY(-4px);
}

.function-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  transition: all 0.3s ease;
}

.function-icon-pet {
  background: rgba(114, 193, 187, 0.15);
}

.function-icon-appointment {
  background: rgba(255, 152, 0, 0.15);
}

.function-icon-monitor {
  background: rgba(244, 67, 54, 0.15);
}

.function-icon-guide {
  background: rgba(156, 39, 176, 0.15);
}

.function-icon-log {
  background: rgba(33, 150, 243, 0.15);
}

.function-icon-diagnosis {
  background: rgba(233, 30, 99, 0.15);
}

.function-icon-standard {
  background: rgba(76, 175, 80, 0.15);
}

.function-icon-time {
  background: rgba(0, 188, 212, 0.15);
}

.function-icon-more {
  background: rgba(158, 158, 158, 0.15);
}

.function-item:hover .function-icon {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.function-label {
  font-size: 14px;
  color: #333;
  text-align: center;
}

/* 数据面板 */
.data-panels {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.data-panel {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
}

.appointment-list {
  margin-bottom: 20px;
}

.appointment-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.appointment-item:hover {
  background-color: #f9f9f9;
}

.appointment-item:last-child {
  border-bottom: none;
}

.appointment-type {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.appointment-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.appointment-doctor {
  color: #72C1BB;
}

.appointment-date {
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.health-trends {
  margin-bottom: 20px;
}

.trend-item {
  margin-bottom: 20px;
}

.trend-item:last-child {
  margin-bottom: 0;
}

.trend-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.trend-progress {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.trend-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.trend-bar-normal {
  background: #4caf50;
}

.trend-bar-warning {
  background: #ff9800;
}

.trend-bar-danger {
  background: #f44336;
}

.trend-bar-excellent {
  background: #4caf50;
}

.trend-bar-good {
  background: #8bc34a;
}

.trend-bar-poor {
  background: #f44336;
}

.trend-status {
  font-size: 12px;
  font-weight: 600;
}

.status-normal {
  color: #4caf50;
}

.status-warning {
  color: #ff9800;
}

.status-danger {
  color: #f44336;
}

.status-excellent {
  color: #4caf50;
}

.status-good {
  color: #8bc34a;
}

.status-poor {
  color: #f44336;
}

.panel-button {
  width: 100%;
  padding: 12px;
  background: #72C1BB;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.panel-button:hover {
  background: #5aa9a3;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .user-home {
    padding: 15px;
  }

  .welcome-banner {
    padding: 20px;
  }

  .welcome-title {
    font-size: 24px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 36px;
  }

  .stat-value {
    font-size: 28px;
  }

  .function-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .function-icon {
    width: 60px;
    height: 60px;
    font-size: 32px;
  }

  .function-label {
    font-size: 12px;
  }

  .data-panels {
    grid-template-columns: 1fr;
  }
}
</style>
