<template>
  <div class="not-found">
    <div class="error-content">
      <h1>404</h1>
      <p>页面未找到</p>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.error-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.error-content h1 {
  font-size: 80px;
  color: #72C1BB;
  margin: 0 0 10px;
}

.error-content p {
  font-size: 18px;
  color: #666;
  margin: 0 0 30px;
}

.btn-primary {
  display: inline-block;
  padding: 12px 30px;
  background-color: #72C1BB;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #5aa9a3;
}
</style>

