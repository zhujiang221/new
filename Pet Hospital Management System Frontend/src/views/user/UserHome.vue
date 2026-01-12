<template>
  <div class="user-home">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1 class="welcome-title">你好，{{ userInfo.name || '用户' }}！</h1>
      <p class="welcome-subtitle">欢迎使用宠物医院管理系统</p>
    </div>

    <!-- 功能卡片网格 -->
    <div class="function-grid">
      <!-- 宠物管理 -->
      <div class="function-card" @click="navigate('/user/pets')">
        <div class="card-icon">🐾</div>
        <div class="card-title">宠物管理</div>
        <div class="card-desc">管理您的宠物信息</div>
      </div>

      <!-- 预约管理 -->
      <div class="function-card" @click="navigate('/user/apply')">
        <div class="card-icon">📅</div>
        <div class="card-title">预约管理</div>
        <div class="card-desc">查看和管理预约</div>
      </div>

      <!-- 健康监测 -->
      <div class="function-card" @click="navigate('/user/assess')">
        <div class="card-icon">❤️</div>
        <div class="card-title">健康监测</div>
        <div class="card-desc">评估宠物健康状况</div>
      </div>

      <!-- 健康指南 -->
      <div class="function-card" @click="navigate('/user/notices')">
        <div class="card-icon">📖</div>
        <div class="card-title">健康指南</div>
        <div class="card-desc">查看健康知识</div>
      </div>

      <!-- 宠物日志 -->
      <div class="function-card" @click="navigate('/user/pet-daily')">
        <div class="card-icon">📝</div>
        <div class="card-title">宠物日志</div>
        <div class="card-desc">记录日常健康数据</div>
      </div>

      <!-- 诊断记录 -->
      <div class="function-card" @click="navigate('/user/diagnosis')">
        <div class="card-icon">🏥</div>
        <div class="card-title">诊断记录</div>
        <div class="card-desc">查看历史诊断</div>
      </div>

      <!-- 健康标准 -->
      <div class="function-card" @click="navigate('/user/standards')">
        <div class="card-icon">📊</div>
        <div class="card-title">健康标准</div>
        <div class="card-desc">了解健康标准</div>
      </div>

      <!-- 医生时间 -->
      <div class="function-card" @click="navigate('/user/free-time')">
        <div class="card-icon">⏰</div>
        <div class="card-title">医生时间</div>
        <div class="card-desc">查看医生可预约时间</div>
      </div>

      <!-- 更多功能 -->
      <div class="function-card" @click="navigate('/user/more')">
        <div class="card-icon">⋯</div>
        <div class="card-title">更多</div>
        <div class="card-desc">查看更多功能</div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <div class="stats-card" @click="navigate('/user/tj-apply')">
        <div class="stats-icon">📈</div>
        <div class="stats-content">
          <div class="stats-title">预约统计</div>
          <div class="stats-desc">查看预约数据分析</div>
        </div>
        <div class="stats-arrow">→</div>
      </div>
      <div class="stats-card" @click="navigate('/user/tj-daily')">
        <div class="stats-icon">📉</div>
        <div class="stats-content">
          <div class="stats-title">日志统计</div>
          <div class="stats-desc">查看健康数据图表</div>
        </div>
        <div class="stats-arrow">→</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '../../utils/user';

const router = useRouter();
const userInfo = reactive(getUserInfo() || { name: '用户' });

// 定义 emit
const emit = defineEmits<{
  navigate: [path: string];
}>();

function navigate(path: string) {
  // 先触发事件通知父组件
  emit('navigate', path);
  // 然后进行路由跳转
  router.push(path);
}
</script>

<style scoped>
.user-home {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  border-radius: 12px;
  color: white;
}

.welcome-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.function-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.function-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(114, 193, 187, 0.3);
  border-color: #72C1BB;
}

.card-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.card-desc {
  font-size: 14px;
  color: #666;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
}

.stats-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(114, 193, 187, 0.3);
  border-color: #72C1BB;
}

.stats-icon {
  font-size: 40px;
  margin-right: 16px;
}

.stats-content {
  flex: 1;
}

.stats-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stats-desc {
  font-size: 14px;
  color: #666;
}

.stats-arrow {
  font-size: 24px;
  color: #72C1BB;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .user-home {
    padding: 15px;
  }

  .welcome-section {
    padding: 16px;
  }

  .welcome-title {
    font-size: 24px;
  }

  .function-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .function-card {
    padding: 16px;
  }

  .card-icon {
    font-size: 36px;
    margin-bottom: 8px;
  }

  .card-title {
    font-size: 16px;
  }

  .card-desc {
    font-size: 12px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }
}
</style>
