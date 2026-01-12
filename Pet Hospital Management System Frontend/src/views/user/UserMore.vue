<template>
  <div class="user-more">
    <!-- 更多功能列表 -->
    <div class="more-list">
      <div class="list-group">
        <div class="group-title">健康管理</div>
        <div class="list-item" @click="navigate('/user/standards')">
          <div class="item-icon">📊</div>
          <div class="item-content">
            <div class="item-title">健康标准</div>
            <div class="item-desc">了解宠物健康标准</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/user/assess')">
          <div class="item-icon">❤️</div>
          <div class="item-content">
            <div class="item-title">健康评估</div>
            <div class="item-desc">评估宠物健康状况</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
      </div>

      <div class="list-group">
        <div class="group-title">数据统计</div>
        <div class="list-item" @click="navigate('/user/tj-apply')">
          <div class="item-icon">📈</div>
          <div class="item-content">
            <div class="item-title">预约统计</div>
            <div class="item-desc">查看预约数据分析</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/user/tj-daily')">
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
        <div class="list-item" @click="navigate('/user/free-time')">
          <div class="item-icon">⏰</div>
          <div class="item-content">
            <div class="item-title">医生时间</div>
            <div class="item-desc">查看医生可预约时间</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/user/diagnosis')">
          <div class="item-icon">🏥</div>
          <div class="item-content">
            <div class="item-title">诊断记录</div>
            <div class="item-desc">查看历史诊断信息</div>
          </div>
          <div class="item-arrow">→</div>
        </div>
        <div class="list-item" @click="navigate('/user/pet-daily')">
          <div class="item-icon">📝</div>
          <div class="item-content">
            <div class="item-title">宠物日志</div>
            <div class="item-desc">记录日常健康数据</div>
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
.user-more {
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
  .user-more {
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
