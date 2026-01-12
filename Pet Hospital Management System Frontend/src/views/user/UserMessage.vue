<template>
  <div class="user-message">
    <!-- 消息列表 -->
    <div class="message-list">
      <div class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-title">暂无消息</div>
        <div class="empty-desc">您还没有收到任何消息</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 消息页面（待实现）
</script>

<style scoped>
.user-message {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  height: 100%;
}

.message-list {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #666;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .user-message {
    padding: 15px;
  }

  .message-list {
    padding: 30px 20px;
    min-height: 300px;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-title {
    font-size: 18px;
  }
}
</style>
