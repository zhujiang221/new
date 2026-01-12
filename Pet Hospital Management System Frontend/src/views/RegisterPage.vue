<template>
  <div class="register-container">
    <div class="overlay"></div>
    <!-- 粒子特效 -->
    <div class="particles">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    <!-- 宠物元素特效 -->
    <div class="pet-elements">
      <div 
        v-for="i in 8" 
        :key="'pet-' + i" 
        class="pet-element"
        :class="['pet-type-' + (i % 3)]"
        :style="getPetElementStyle(i)"
      ></div>
    </div>
    <div class="register-box">
      <!-- <div class="user-image">
        <img src="/imgs/catFace.png" alt="logo" />
      </div> -->
      <h3>用户注册</h3>
      <el-form
        ref="registerFormRef"
        :model="form"
        :label-width="0"
        class="register-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-form-item prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入姓名"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            type="tel"
            placeholder="请输入11位手机号码"
            maxlength="11"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="请输入邮箱地址"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input
              v-model="form.emailCode"
              placeholder="请输入邮箱验证码"
              class="input-field"
              size="large"
              :style="{ flex: 1 }"
              maxlength="6"
              required
            />
            <el-button
              type="primary"
              :disabled="codeButtonDisabled"
              :loading="codeSending"
              size="large"
              @click="sendEmailCode"
              style="min-width: 120px;"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码(至少6位)"
            show-password
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-form-item prop="password2">
          <el-input
            v-model="form.password2"
            type="password"
            placeholder="请再次输入密码"
            show-password
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
            required
          />
        </el-form-item>
        <el-space direction="vertical" :size="15" :style="{ width: '100%', display: 'flex', alignItems: 'center', flexDirection: 'column' }">
          <el-button
            type="primary"
            native-type="submit"
            class="btn-register"
            :disabled="loading"
            :style="{ width: '100px' }"
            size="large"
            @click="handleSubmit"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
          <el-button
            type="default"
            class="btn-login"
            @click="goLogin"
            :style="{ width: '60%' }"
            size="large"
          >
            返回登录
          </el-button>
        </el-space>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import http from '../api/http';
import type { FormInstance } from 'element-plus';
import { showMessage } from '../utils/message';

// 生成粒子样式
function getParticleStyle(index: number) {
  const size = Math.random() * 4 + 2; // 2-6px
  const left = Math.random() * 100; // 0-100%
  const animationDelay = Math.random() * 20; // 0-20s
  const animationDuration = Math.random() * 10 + 15; // 15-25s
  return {
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${animationDelay}s`,
    animationDuration: `${animationDuration}s`
  };
}

// 生成宠物元素样式（爱心、圆形等）
function getPetElementStyle(index: number) {
  const left = Math.random() * 100;
  const top = Math.random() * 100;
  const size = Math.random() * 20 + 15; // 15-35px
  const animationDelay = Math.random() * 15;
  const animationDuration = Math.random() * 8 + 12; // 12-20s
  return {
    left: `${left}%`,
    top: `${top}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${animationDelay}s`,
    animationDuration: `${animationDuration}s`
  };
}

const router = useRouter();
const registerFormRef = ref<FormInstance>();

const form = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  emailCode: '',
  password: '',
  password2: ''
});

const loading = ref(false);
const codeSending = ref(false);
const codeCountdown = ref(0);
const countdownTimer = ref<number | null>(null);

// 计算验证码按钮状态
const codeButtonDisabled = computed(() => {
  return codeSending.value || codeCountdown.value > 0 || !form.email.trim();
});

// 计算验证码按钮文本
const codeButtonText = computed(() => {
  if (codeSending.value) {
    return '发送中...';
  }
  if (codeCountdown.value > 0) {
    return `${codeCountdown.value}秒后重试`;
  }
  return '发送验证码';
});

async function handleSubmit() {
  // 前端验证
  if (!form.username.trim()) {
    showMessage('用户名不能为空', 'error');
    return;
  }
  if (!form.name.trim()) {
    showMessage('姓名不能为空', 'error');
    return;
  }
  if (!form.phone.trim()) {
    showMessage('电话号不能为空', 'error');
    return;
  }
  // 验证电话号格式（11位手机号）
  const phonePattern = /^1[3-9]\d{9}$/;
  if (!phonePattern.test(form.phone.trim())) {
    showMessage('电话号格式不正确，请输入11位手机号码', 'error');
    return;
  }
  if (!form.email.trim()) {
    showMessage('邮箱不能为空', 'error');
    return;
  }
  // 验证邮箱格式
  const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  if (!emailPattern.test(form.email.trim())) {
    showMessage('邮箱格式不正确，请检查后重新输入', 'error');
    return;
  }
  if (!form.password.trim()) {
    showMessage('密码不能为空', 'error');
    return;
  }
  if (form.password.length < 6) {
    showMessage('密码长度不能小于6位', 'error');
    return;
  }
  if (!form.password2.trim()) {
    showMessage('确认密码不能为空', 'error');
    return;
  }
  if (form.password !== form.password2) {
    showMessage('两次输入的密码不一致，请重新输入', 'error');
    return;
  }
  if (!form.emailCode.trim()) {
    showMessage('请输入邮箱验证码', 'error');
    return;
  }
  
  loading.value = true;
  try {
    // 后端注册接口为 /doRegist
    const resp = await http.post('/doRegist', {
      username: form.username.trim(),
      name: form.name.trim(),
      phone: form.phone.trim(),
      email: form.email.trim(),
      emailCode: form.emailCode.trim(),
      password: form.password,
      password2: form.password2
    });
    const data = resp.data;
    
    if (data.result === 'success') {
      showMessage('注册成功，请登录', 'success');
      // 清空表单
      form.username = '';
      form.name = '';
      form.phone = '';
      form.email = '';
      form.emailCode = '';
      form.password = '';
      form.password2 = '';
      // 清除倒计时
      if (countdownTimer.value) {
        clearInterval(countdownTimer.value);
        countdownTimer.value = null;
      }
      codeCountdown.value = 0;
      router.push('/');
    } else {
      // 显示后端返回的错误信息
      showMessage(data.message || '注册失败', 'error');
    }
  } catch (e: any) {
    console.error('注册错误:', e);
    console.error('错误详情:', {
      status: e.response?.status,
      statusText: e.response?.statusText,
      data: e.response?.data,
      message: e.message
    });
    
    let errorMsg = '系统错误，请稍后重试';
    if (e.response) {
      // 有响应但状态码不是2xx
      if (e.response.status === 404) {
        errorMsg = '注册接口不存在，请检查服务器配置';
      } else if (e.response.status === 500) {
        errorMsg = e.response.data?.message || '服务器内部错误';
      } else {
        errorMsg = e.response.data?.message || `请求失败 (${e.response.status})`;
      }
    } else if (e.request) {
      // 请求已发出但没有收到响应
      errorMsg = '无法连接到服务器，请检查网络连接';
    } else {
      // 请求配置出错
      errorMsg = e.message || '请求配置错误';
    }
    showMessage(errorMsg, 'error');
  } finally {
    loading.value = false;
  }
}

function goLogin() {
  router.push('/');
}

// 发送邮箱验证码
async function sendEmailCode() {
  // 验证邮箱格式
  if (!form.email.trim()) {
    showMessage('请先输入邮箱地址', 'error');
    return;
  }
  const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  if (!emailPattern.test(form.email.trim())) {
    showMessage('邮箱格式不正确，请检查后重新输入', 'error');
    return;
  }
  
  codeSending.value = true;
  try {
    const resp = await http.post('/sendEmailCode', {
      email: form.email.trim()
    });
    const data = resp.data;
    
    if (data.result === 'success') {
      showMessage('验证码已发送，请查收邮箱', 'success');
      // 开始倒计时
      startCountdown();
    } else {
      showMessage(data.message || '验证码发送失败', 'error');
    }
  } catch (e: any) {
    console.error('发送验证码错误:', e);
    let errorMsg = '验证码发送失败，请稍后重试';
    if (e.response?.data?.message) {
      errorMsg = e.response.data.message;
    }
    showMessage(errorMsg, 'error');
  } finally {
    codeSending.value = false;
  }
}

// 开始倒计时
function startCountdown() {
  codeCountdown.value = 60;
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
  }
  countdownTimer.value = window.setInterval(() => {
    codeCountdown.value--;
    if (codeCountdown.value <= 0) {
      if (countdownTimer.value) {
        clearInterval(countdownTimer.value);
        countdownTimer.value = null;
      }
    }
  }, 1000);
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
    countdownTimer.value = null;
  }
});
</script>

<style scoped>
.register-container {
  height: 100vh;
  margin: 0;
  font-family: "Arial", sans-serif;
  background: linear-gradient(135deg, #56ab2f 0%, #a8e063 25%, #67c23a 50%, #85ce61 75%, #48c1bb 100%);
  background-size: 400% 400%;
  animation: gradientFlow 20s ease infinite;
  /* background-image: url('/src/main/resources/static/imgs/bg.jpg'); */
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 动态背景层 */
.register-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    rgba(103, 194, 58, 0.25) 0%,
    rgba(133, 206, 97, 0.3) 25%,
    rgba(72, 193, 187, 0.25) 50%,
    rgba(133, 206, 97, 0.3) 75%,
    rgba(103, 194, 58, 0.25) 100%
  );
  background-size: 400% 400%;
  animation: gradientFlow 15s ease infinite;
  z-index: 0;
}

/* 粒子效果背景 */
.register-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20% 30%, rgba(255, 255, 255, 0.4), transparent),
    radial-gradient(2px 2px at 60% 70%, rgba(103, 194, 58, 0.4), transparent),
    radial-gradient(1px 1px at 50% 50%, rgba(133, 206, 97, 0.5), transparent),
    radial-gradient(1px 1px at 80% 10%, rgba(72, 193, 187, 0.4), transparent),
    radial-gradient(2px 2px at 40% 80%, rgba(255, 255, 255, 0.3), transparent),
    radial-gradient(1px 1px at 10% 60%, rgba(103, 194, 58, 0.5), transparent);
  background-size: 200% 200%;
  background-position: 0% 0%, 100% 100%, 50% 50%, 0% 100%, 100% 0%, 50% 0%;
  animation: particleMove 20s ease infinite;
  z-index: 0;
  opacity: 0.8;
}

/* 渐变流动动画 */
@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 粒子移动动画 */
@keyframes particleMove {
  0% {
    background-position: 0% 0%, 100% 100%, 50% 50%, 0% 100%, 100% 0%, 50% 0%;
  }
  50% {
    background-position: 100% 100%, 0% 0%, 0% 100%, 100% 0%, 0% 50%, 100% 50%;
  }
  100% {
    background-position: 0% 0%, 100% 100%, 50% 50%, 0% 100%, 100% 0%, 50% 0%;
  }
}

/* 漂浮粒子特效 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: rgba(103, 194, 58, 0.6);
  border-radius: 50%;
  animation: floatParticle linear infinite;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.8);
}

@keyframes floatParticle {
  0% {
    transform: translateY(100vh) translateX(0) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) translateX(100px) scale(1);
    opacity: 0;
  }
}

/* 宠物元素特效 */
.pet-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.pet-element {
  position: absolute;
  animation: floatPetElement ease-in-out infinite;
  opacity: 0.4;
}

.pet-element::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(103, 194, 58, 0.5);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.6);
}

/* 爱心形状 (type-0) */
.pet-element.pet-type-0::before {
  background: transparent;
  border-radius: 0;
}

.pet-element.pet-type-0::before,
.pet-element.pet-type-0::after {
  content: '';
  position: absolute;
  width: 50%;
  height: 80%;
  background: rgba(133, 206, 97, 0.6);
  border-radius: 50% 50% 0 0;
  box-shadow: 0 0 8px rgba(133, 206, 97, 0.8);
}

.pet-element.pet-type-0::before {
  left: 0;
  transform: rotate(-45deg);
  transform-origin: 100% 100%;
}

.pet-element.pet-type-0::after {
  right: 0;
  transform: rotate(45deg);
  transform-origin: 0% 100%;
}

/* 星形 (type-2) */
.pet-element.pet-type-2::before {
  background: transparent;
  border-radius: 0;
  clip-path: polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%);
  background: rgba(72, 193, 187, 0.5);
  box-shadow: 0 0 10px rgba(72, 193, 187, 0.6);
}

@keyframes floatPetElement {
  0%, 100% {
    transform: translateY(0) translateX(0) rotate(0deg) scale(1);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-20px) translateX(10px) rotate(90deg) scale(1.1);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-40px) translateX(-10px) rotate(180deg) scale(0.9);
    opacity: 0.4;
  }
  75% {
    transform: translateY(-20px) translateX(-20px) rotate(270deg) scale(1.05);
    opacity: 0.5;
  }
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  z-index: 0;
}

.register-box {
  position: relative;
  width: 480px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  background-color: #3c3f41;
  box-shadow: 0px 0px 43px -2px wheat;
  border-radius: 8px;
  padding: 30px;
  color: white;
  opacity: 0.95;
  z-index: 1;
  margin: 0 auto;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.register-box h3 {
  text-align: center;
  margin: 10px 0 25px;
  font-size: 20px;
  font-weight: 500;
  color: white;
}

/* 移动端注册框样式 */
@media (max-width: 767px) {
  .register-box {
    width: 90vw;
    padding: 25px 20px;
  }
  
  .user-image {
    width: 70px;
    height: 70px;
  }
  
  .register-form :deep(.el-form-item) {
    margin-bottom: 18px;
  }
  
  .register-form :deep(.el-form-item__label) {
    font-size: 13px;
  }
}

.user-image {
  border-radius: 50%;
  overflow: hidden;
  width: 80px;
  height: 80px;
  margin: 0 auto;
}

.user-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.register-form {
  width: 100%;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-form-item__label) {
  color: white;
  font-size: 14px;
  padding-right: 12px;
  text-align: right;
}

.register-form :deep(.el-form-item__content) {
  position: relative;
  width: 100%;
}

.input-field {
  display: block;
  width: 100%;
  box-sizing: border-box;
}

/* 确保输入框宽度固定，即使有后缀图标也不变 */
.input-field :deep(.el-input) {
  width: 100%;
}

.input-field :deep(.el-input__wrapper) {
  width: 100%;
  box-sizing: border-box;
}

.input-field :deep(.el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  background-color: #fff;
  width: 100% !important;
  box-sizing: border-box;
}

.input-field :deep(.el-input__wrapper:hover) {
  border-color: #67c23a;
}

.input-field :deep(.el-input__wrapper.is-focus) {
  border-color: #67c23a;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.1);
}

.input-field :deep(.el-input__inner) {
  font-size: 14px;
  color: #606266;
  width: 100%;
  box-sizing: border-box;
}

.input-field :deep(.el-input__suffix) {
  padding-right: 8px;
  flex-shrink: 0;
}

/* 确保输入框内部布局不影响总宽度 */
.input-field :deep(.el-input__inner-wrapper) {
  width: 100%;
  box-sizing: border-box;
}

.input-field :deep(.el-input__suffix-inner) {
  color: #909399;
  cursor: pointer;
}

.input-field :deep(.el-input__suffix-inner:hover) {
  color: #01AAED;
}

.btn-register {
  border-radius: 6px;
  background-color: #67c23a;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
}

.btn-register:hover {
  background-color: #5daf34;
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
  transform: translateY(-1px);
}

.btn-register:active {
  transform: translateY(0);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-login {
  border-radius: 6px;
  background-color: #85ce61;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(133, 206, 97, 0.2);
}

.btn-login:hover {
  background-color: #73c04d;
  box-shadow: 0 4px 8px rgba(133, 206, 97, 0.3);
  transform: translateY(-1px);
}

.btn-login:active {
  transform: translateY(0);
}
</style>

