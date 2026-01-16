<template>
  <div class="doctor-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">您好,{{ userInfo.name || '医生' }}！</h1>
        <p class="welcome-date">今天是 {{ currentDate }}</p>
        <p class="welcome-appointments">今日预约:{{ statistics.todayTotalAppointments }}个</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" @click="navigate('/doctor/apply')">
        <div class="stat-icon stat-icon-pending">📅</div>
        <div class="stat-content">
          <div class="stat-value stat-value-pending">{{ statistics.pendingAppointments }}</div>
          <div class="stat-label">待处理预约</div>
        </div>
      </div>
      <div class="stat-card" @click="navigate('/doctor/apply')">
        <div class="stat-icon stat-icon-completed">✓</div>
        <div class="stat-content">
          <div class="stat-value stat-value-completed">{{ statistics.todayCompleted }}</div>
          <div class="stat-label">今日已完成</div>
        </div>
      </div>
      <div class="stat-card" @click="navigate('/doctor/apply')">
        <div class="stat-icon stat-icon-patients">👥</div>
        <div class="stat-content">
          <div class="stat-value stat-value-patients">{{ statistics.inClinicPatients }}</div>
          <div class="stat-label">在诊患者</div>
        </div>
      </div>
      <div class="stat-card" @click="navigate('/doctor/medicine-record')">
        <div class="stat-icon stat-icon-prescriptions">💊</div>
        <div class="stat-content">
          <div class="stat-value stat-value-prescriptions">{{ statistics.weekPrescriptions }}</div>
          <div class="stat-label">本周开药</div>
        </div>
      </div>
    </div>

    <!-- 功能中心 -->
    <div class="function-center">
      <h2 class="section-title">功能中心</h2>
      <div class="function-grid">
        <div class="function-item" @click="navigate('/doctor/apply')">
          <div class="function-icon function-icon-appointment">📅</div>
          <div class="function-label">预约管理</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/diagnosis')">
          <div class="function-icon function-icon-health">❤️</div>
          <div class="function-label">宠物健康史</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/schedule')">
          <div class="function-icon function-icon-schedule">⏰</div>
          <div class="function-label">排班管理</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/free-time')">
          <div class="function-icon function-icon-time">⏰</div>
          <div class="function-label">工作时间</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/medicine')">
          <div class="function-icon function-icon-medicine">💊</div>
          <div class="function-label">药品管理</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/medicine-record')">
          <div class="function-icon function-icon-record">📝</div>
          <div class="function-label">开药记录</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/notices')">
          <div class="function-icon function-icon-guide">📖</div>
          <div class="function-label">健康指南</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/standards')">
          <div class="function-icon function-icon-standard">❤️</div>
          <div class="function-label">健康标准</div>
        </div>
        <div class="function-item" @click="navigate('/doctor/more')">
          <div class="function-icon function-icon-more">⋯</div>
          <div class="function-label">更多功能</div>
        </div>
      </div>
    </div>

    <!-- 数据面板（桌面端） -->
    <div v-if="!isMobile" class="data-panels">
      <!-- 今日排班 -->
      <div class="data-panel">
        <h3 class="panel-title">
          <span class="panel-icon">📅</span>
          今日排班
        </h3>
        <div v-if="todaySchedule.length === 0" class="empty-state">
          <p>今日暂无排班</p>
        </div>
        <div v-else class="schedule-list">
          <div 
            v-for="item in todaySchedule" 
            :key="item.id"
            class="schedule-item"
            :class="getStatusClass(item.status)"
            @click="navigate('/doctor/apply')"
          >
            <div class="schedule-info">
              <div class="schedule-pet">{{ item.petName }}{{ item.petBreed ? ` (${item.petBreed})` : '' }}</div>
              <div class="schedule-details">
                <span class="schedule-type">{{ item.appointmentTypeName }}</span>
                <span class="schedule-time">{{ item.timeSlot }}</span>
              </div>
            </div>
            <div class="schedule-status" :class="getStatusClass(item.status)">
              {{ item.statusText }}
            </div>
          </div>
        </div>
        <button class="panel-button" @click="navigate('/doctor/schedule')">管理排班</button>
      </div>

      <!-- 工作统计 -->
      <div class="data-panel">
        <h3 class="panel-title">
          <span class="panel-icon">📊</span>
          工作统计
        </h3>
        <div class="work-statistics">
          <div class="stat-item">
            <div class="stat-label">本周接诊量</div>
            <div class="stat-progress">
              <div 
                class="stat-progress-bar" 
                :style="{ width: `${getProgressPercentage(workStatistics.weekConsultations, workStatistics.weekConsultationsTarget)}%` }"
              ></div>
            </div>
            <div class="stat-value-text">
              {{ workStatistics.weekConsultations }}/{{ workStatistics.weekConsultationsTarget }}
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">患者满意度</div>
            <div class="stat-progress">
              <div 
                class="stat-progress-bar stat-progress-bar-success" 
                :style="{ width: `${workStatistics.patientSatisfaction}%` }"
              ></div>
            </div>
            <div class="stat-value-text">
              {{ workStatistics.patientSatisfaction }}%
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">本月开药量</div>
            <div class="stat-progress">
              <div 
                class="stat-progress-bar stat-progress-bar-info" 
                :style="{ width: `${getProgressPercentage(workStatistics.monthPrescriptions, 200)}%` }"
              ></div>
            </div>
            <div class="stat-value-text">
              {{ workStatistics.monthPrescriptions }} 次
            </div>
          </div>
        </div>
        <div class="panel-buttons">
          <button class="panel-button" @click="navigate('/doctor/medicine-record')">开具处方</button>
          <button class="panel-button" @click="navigate('/doctor/schedule')">管理排班</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '../../utils/user';
import { getDoctorHomeStatistics, getTodaySchedule, getWorkStatistics, type TodayScheduleItem, type WorkStatistics } from '../../api/statistics';

const router = useRouter();
const userInfo = reactive(getUserInfo() || { name: '医生' });
const isMobile = ref(false);

// 统计数据
const statistics = reactive({
  pendingAppointments: 0,
  todayTotalAppointments: 0,
  todayCompleted: 0,
  inClinicPatients: 0,
  weekPrescriptions: 0
});

const todaySchedule = ref<TodayScheduleItem[]>([]);
const workStatistics = reactive<WorkStatistics>({
  weekConsultations: 0,
  weekConsultationsTarget: 50,
  patientSatisfaction: 0,
  monthPrescriptions: 0
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

function getStatusClass(status: number): string {
  switch (status) {
    case 1:
      return 'status-pending';
    case 2:
      return 'status-in-progress';
    case 3:
      return 'status-completed';
    default:
      return '';
  }
}

function getProgressPercentage(current: number, target: number): number {
  if (target === 0) return 0;
  const percentage = (current / target) * 100;
  return Math.min(percentage, 100);
}

// 检测移动端
function checkMobile() {
  isMobile.value = window.innerWidth < 768;
}

// 加载数据
async function loadData() {
  try {
    // 加载统计数据
    const stats = await getDoctorHomeStatistics();
    Object.assign(statistics, stats);
    
    // 加载今日排班
    const schedule = await getTodaySchedule(10);
    todaySchedule.value = schedule;
    
    // 加载工作统计
    const workStats = await getWorkStatistics();
    Object.assign(workStatistics, workStats);
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
.doctor-home {
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

.welcome-appointments {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
  box-shadow: 0 4px 16px rgba(114, 193, 187, 0.3);
  border-color: #72C1BB;
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

.stat-icon-pending {
  background: #fff7e6;
}

.stat-icon-completed {
  background: #f6ffed;
}

.stat-icon-patients {
  background: #e6f7ff;
}

.stat-icon-prescriptions {
  background: #f9f0ff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-value-pending {
  color: #fa8c16;
}

.stat-value-completed {
  color: #52c41a;
}

.stat-value-patients {
  color: #1890ff;
}

.stat-value-prescriptions {
  color: #722ed1;
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
  margin-bottom: 16px;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.function-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.function-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(114, 193, 187, 0.3);
  border-color: #72C1BB;
}

.function-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.function-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-icon {
  font-size: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.schedule-item {
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.schedule-item:hover {
  background: #f5f5f5;
  border-color: #72C1BB;
}

.schedule-info {
  flex: 1;
}

.schedule-pet {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.schedule-details {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: #666;
}

.schedule-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-in-progress {
  background: #e6f7ff;
  color: #1890ff;
}

.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.panel-button {
  width: 100%;
  padding: 12px;
  background: #72C1BB;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.panel-button:hover {
  background: #5aa9a3;
}

.panel-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.work-statistics {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-progress {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.stat-progress-bar {
  height: 100%;
  background: #72C1BB;
  border-radius: 4px;
  transition: width 0.3s;
}

.stat-progress-bar-success {
  background: #52c41a;
}

.stat-progress-bar-info {
  background: #1890ff;
}

.stat-value-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .doctor-home {
    padding: 15px;
  }

  .welcome-banner {
    padding: 20px;
  }

  .welcome-title {
    font-size: 24px;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    font-size: 36px;
    width: 50px;
    height: 50px;
  }

  .stat-value {
    font-size: 24px;
  }

  .function-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .function-item {
    padding: 16px 12px;
  }

  .function-icon {
    font-size: 28px;
  }

  .function-label {
    font-size: 12px;
  }

  .data-panels {
    grid-template-columns: 1fr;
  }
}
</style>
