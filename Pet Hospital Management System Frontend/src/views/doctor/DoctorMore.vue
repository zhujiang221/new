<template>
  <div class="doctor-more">
    <!-- 更多功能列表 -->
    <div class="more-list">
      <div class="list-group">
        <div class="group-title">宠物管理</div>
        <div class="list-item" @click="navigate('/doctor/pet-daily')">
          <div class="item-icon">📝</div>
          <div class="item-content">
            <div class="item-title">宠物日志</div>
            <div class="item-desc">查看宠物日常记录</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/doctor/diagnosis')">
          <div class="item-icon">🏥</div>
          <div class="item-content">
            <div class="item-title">宠物健康史</div>
            <div class="item-desc">查看宠物健康记录</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
      </div>

      <div class="list-group">
        <div class="group-title">服务管理</div>
        <div class="list-item" @click="navigate('/doctor/service-type')">
          <div class="item-icon">⚙️</div>
          <div class="item-content">
            <div class="item-title">服务类型管理</div>
            <div class="item-desc">管理服务类型</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/doctor/prescribe-medicine')">
          <div class="item-icon">💉</div>
          <div class="item-content">
            <div class="item-title">开具药品</div>
            <div class="item-desc">为宠物开具药品</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
      </div>

      <div class="list-group">
        <div class="group-title">数据统计</div>
        <div class="list-item" @click="navigate('/doctor/tj-apply')">
          <div class="item-icon">📈</div>
          <div class="item-content">
            <div class="item-title">预约统计</div>
            <div class="item-desc">查看预约数据分析</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/doctor/tj-daily')">
          <div class="item-icon">📉</div>
          <div class="item-content">
            <div class="item-title">日志统计</div>
            <div class="item-desc">查看健康数据图表</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
      </div>

      <div class="list-group">
        <div class="group-title">其他功能</div>
        <div class="list-item" @click="navigate('/doctor/notices')">
          <div class="item-icon">📖</div>
          <div class="item-content">
            <div class="item-title">健康指南</div>
            <div class="item-desc">发布健康指南</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/doctor/standards')">
          <div class="item-icon">📊</div>
          <div class="item-content">
            <div class="item-title">健康标准</div>
            <div class="item-desc">制定健康标准</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/doctor/medicine-record')">
          <div class="item-icon">📋</div>
          <div class="item-content">
            <div class="item-title">开药记录</div>
            <div class="item-desc">查看开药历史</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

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
.doctor-more {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.more-list {
  margin-bottom: 30px;
}

.list-group {
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.group-title {
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
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

/* 移动端适配 */
@media (max-width: 767px) {
  .doctor-more {
    padding: 15px;
  }

  .list-item {
    padding: 14px 16px;
  }

  .group-title {
    padding: 14px 16px;
  }
}
</style>
