<template>
  <div class="login-container" @mousemove="handleMouseMove" ref="containerRef">
    <div class="overlay"></div>
    <!-- 粒子特效 -->
    <div class="particles">
      <div 
        v-for="particle in particles" 
        :key="particle.id" 
        class="particle" 
        :style="getParticleStyle(particle)"
      ></div>
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
    <div class="login-box">
      <div class="user-image">
        <img src="/imgs/catFace.png" alt="logo" />
        <div class="system-title">宠物医院管理系统</div>
      </div>
      <el-form
        ref="loginFormRef"
        :model="form"
        :label-width="0"
        class="login-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :class="{ warning: errors.username }"
            @input="clearError('username')"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
          />
          <div class="error-tip" v-if="errors.username">
            <div class="error-msg">{{ errors.username }}</div>
            <div class="triangle"></div>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :class="{ warning: errors.password }"
            @input="clearError('password')"
            @keyup.enter="handleSubmit"
            class="input-field"
            size="large"
            :style="{ width: '100%' }"
          />
          <div class="error-tip" v-if="errors.password">
            <div class="error-msg">{{ errors.password }}</div>
            <div class="triangle"></div>
          </div>
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="form.captcha"
              placeholder="请输入验证码"
              :class="{ warning: errors.captcha }"
              @input="clearError('captcha')"
              @keyup.enter="handleSubmit"
              maxlength="4"
              class="input-field captcha-input"
              size="large"
              :style="{ width: '100%' }"
            />
            <div class="captcha-image-wrapper" @click="refreshCaptcha">
              <img 
                v-if="captchaImageUrl"
                :src="captchaImageUrl" 
                alt="验证码" 
                class="captcha-image"
                @error="handleCaptchaError"
              />
              <div v-else class="captcha-placeholder">
                点击获取验证码
              </div>
              <div class="captcha-refresh-hint">点击刷新</div>
            </div>
          </div>
          <div class="error-tip" v-if="errors.captcha">
            <div class="error-msg">{{ errors.captcha }}</div>
            <div class="triangle"></div>
          </div>
        </el-form-item>
        <el-space direction="vertical" :size="15" :style="{ width: '100%', display: 'flex', alignItems: 'center', flexDirection: 'column' }">
          <el-button
            type="primary"
            native-type="submit"
            class="btn-login"
            :disabled="loading"
            :style="{ width: '150px' }"
            size="large"
            @click="handleSubmit"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
          <el-button
            type="default"
            class="btn-register"
            @click="goRegister"
            :style="{ width: 'auto' }"
            size="large"
          >
            注册
          </el-button>
          <el-button
            link
            class="btn-forget-password"
            @click="goForgetPassword"
            :style="{ color: '#67c23a', padding: 0 }"
          >
            找回密码
          </el-button>
        </el-space>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import http from '../api/http';
import { saveUserInfo, type UserInfo, ROLE_ADMIN, ROLE_DOCTOR, ROLE_USER } from '../utils/user';
import { getCurrentUserInfo } from '../api/user';
import { setToken, getToken } from '../utils/token';
import type { FormInstance } from 'element-plus';
import { showMessage } from '../utils/message';

interface Particle {
  id: number;
  x: number;
  y: number;
  size: number;
  speed: number;
  angle: number;
  targetX: number;
  targetY: number;
  startX: number;
  startY: number;
  progress: number;
}

const containerRef = ref<HTMLElement>();
const mouseX = ref(0);
const mouseY = ref(0);
const particles = ref<Particle[]>([]);
let particleIdCounter = 0;
let animationFrameId: number | null = null;

// 从屏幕边缘随机生成粒子
function createParticle(): Particle {
  const side = Math.floor(Math.random() * 4); // 0: 上, 1: 右, 2: 下, 3: 左
  let startX = 0;
  let startY = 0;
  
  if (side === 0) { // 上边
    startX = Math.random() * 100;
    startY = -5;
  } else if (side === 1) { // 右边
    startX = 105;
    startY = Math.random() * 100;
  } else if (side === 2) { // 下边
    startX = Math.random() * 100;
    startY = 105;
  } else { // 左边
    startX = -5;
    startY = Math.random() * 100;
  }
  
  return {
    id: particleIdCounter++,
    x: startX,
    y: startY,
    size: Math.random() * 4 + 2,
    speed: Math.random() * 0.3 + 0.2,
    angle: 0,
    targetX: mouseX.value,
    targetY: mouseY.value,
    startX: startX,
    startY: startY,
    progress: 0
  };
}

// 初始化粒子
function initParticles() {
  particles.value = [];
  for (let i = 0; i < 50; i++) {
    particles.value.push(createParticle());
  }
}

// 处理鼠标移动
function handleMouseMove(event: MouseEvent) {
  if (!containerRef.value) return;
  const rect = containerRef.value.getBoundingClientRect();
  mouseX.value = ((event.clientX - rect.left) / rect.width) * 100;
  mouseY.value = ((event.clientY - rect.top) / rect.height) * 100;
  
  // 更新所有粒子的目标位置
  particles.value.forEach(particle => {
    particle.targetX = mouseX.value;
    particle.targetY = mouseY.value;
  });
}

// 更新粒子位置
function updateParticles() {
  particles.value.forEach((particle, index) => {
    // 计算到鼠标的距离
    const dx = particle.targetX - particle.x;
    const dy = particle.targetY - particle.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    // 如果粒子到达鼠标附近，重新生成
    if (distance < 5) {
      const newParticle = createParticle();
      particles.value[index] = newParticle;
      return;
    }
    
    // 向鼠标方向移动
    const angle = Math.atan2(dy, dx);
    particle.x += Math.cos(angle) * particle.speed;
    particle.y += Math.sin(angle) * particle.speed;
    
    // 如果粒子移出屏幕，重新生成
    if (particle.x < -10 || particle.x > 110 || particle.y < -10 || particle.y > 110) {
      const newParticle = createParticle();
      particles.value[index] = newParticle;
    }
  });
  
  animationFrameId = requestAnimationFrame(updateParticles);
}

// 生成粒子样式
function getParticleStyle(particle: Particle) {
  return {
    left: `${particle.x}%`,
    top: `${particle.y}%`,
    width: `${particle.size}px`,
    height: `${particle.size}px`,
    transform: 'translate(-50%, -50%)'
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
const loginFormRef = ref<FormInstance>();

const form = reactive({
  username: '',
  password: '',
  captcha: ''
});

const errors = reactive({
  username: '',
  password: '',
  captcha: ''
});

const loading = ref(false);
const captchaImageUrl = ref(''); // 初始化为空，避免自动加载错误URL
const captchaId = ref('');
const captchaLoading = ref(false); // 添加加载状态，避免重复请求

// 刷新验证码
async function refreshCaptcha() {
  // 如果正在加载，避免重复请求
  if (captchaLoading.value) {
    return;
  }
  
  captchaLoading.value = true;
  try {
    const resp = await http.get('/captcha');
    const data = resp.data;
    
    if (data.result === 'success' && data.captchaId && data.image) {
      captchaId.value = data.captchaId;
      captchaImageUrl.value = data.image; // base64编码的图片，格式为 "data:image/jpeg;base64,..."
      form.captcha = '';
      clearError('captcha');
    } else {
      console.error('获取验证码失败:', data.message || '未知错误');
      showMessage('获取验证码失败，请刷新页面重试', 'error');
      captchaImageUrl.value = ''; // 失败时清空图片URL
    }
  } catch (e: any) {
    console.error('获取验证码异常:', e);
    const errorMsg = e?.response?.data?.message || e?.message || '网络错误';
    showMessage(`获取验证码失败: ${errorMsg}，请刷新页面重试`, 'error');
    captchaImageUrl.value = ''; // 失败时清空图片URL
  } finally {
    captchaLoading.value = false;
  }
}

// 处理验证码图片加载错误
function handleCaptchaError() {
  console.error('验证码图片加载失败');
  // 如果图片URL存在但加载失败，可能是base64数据损坏，重新获取
  if (captchaImageUrl.value) {
    captchaImageUrl.value = ''; // 清空当前图片
    // 延迟后重试，避免频繁请求
    setTimeout(() => {
      if (!captchaLoading.value) {
        refreshCaptcha();
      }
    }, 1000);
  }
}

// 页面加载时获取验证码（这是正常行为，登录页应该自动显示验证码）
onMounted(() => {
  // 延迟一点加载验证码，确保页面已完全渲染
  setTimeout(() => {
    refreshCaptcha();
  }, 100);
  initParticles();
  // 初始化鼠标位置在屏幕中心
  if (containerRef.value) {
    const rect = containerRef.value.getBoundingClientRect();
    mouseX.value = 50;
    mouseY.value = 50;
  }
  animationFrameId = requestAnimationFrame(updateParticles);
  
  // 定期生成新粒子
  setInterval(() => {
    if (particles.value.length < 50) {
      particles.value.push(createParticle());
    }
  }, 500);
});

onUnmounted(() => {
  if (animationFrameId !== null) {
    cancelAnimationFrame(animationFrameId);
  }
});

function clearError(field: 'username' | 'password' | 'captcha') {
  errors[field] = '';
}

function validate(): boolean {
  let valid = true;
  
  if (!form.username.trim()) {
    errors.username = '用户名不能为空';
    valid = false;
  }
  
  if (form.password.length < 5) {
    errors.password = '密码长度不能小于5个字符';
    valid = false;
  }
  
  if (!form.captcha.trim()) {
    errors.captcha = '请输入验证码';
    valid = false;
  } else if (form.captcha.trim().length !== 4) {
    errors.captcha = '验证码必须为4位';
    valid = false;
  }
  
  return valid;
}

async function handleSubmit() {
  if (!validate()) return;
  
  // 检查验证码ID是否存在
  if (!captchaId.value) {
    showMessage('验证码已过期，请刷新验证码', 'error');
    await refreshCaptcha();
    return;
  }
  
  loading.value = true;
  try {
    const resp = await http.post('/login', {
      username: form.username,
      password: form.password,
      captcha: form.captcha.trim(),
      captchaId: captchaId.value
    });
    const data = resp.data;
    
    if (data.result === 'success' || data.status === 'SUCCESS') {
      console.log('登录响应数据:', data);
      
      // 尝试从登录响应中直接获取用户信息和角色
      let role: any = null;
      let userId: string | null = null;
      let userName = form.username;
      
      // 先保存Token，确保后续API请求能携带token
      const token = data.token || data.data?.token;
      if (token) {
        console.log('保存Token:', token.substring(0, 20) + '...');
        setToken(token);
        // 验证token是否保存成功
        const savedToken = getToken();
        if (savedToken) {
          console.log('Token保存成功，已保存的Token:', savedToken.substring(0, 20) + '...');
        } else {
          console.error('Token保存失败！');
        }
      } else {
        console.warn('登录响应中没有找到token字段，响应数据:', data);
      }
      
      // 检查登录响应中是否包含用户信息
      if (data.data) {
        role = data.data.role;
        userId = data.data.id || data.data.userId;
        userName = data.data.name || data.data.username || form.username;
      } else if (data.role !== undefined) {
        role = data.role;
        userId = data.id || data.userId;
        userName = data.name || data.username || form.username;
      }
      
      // 如果登录响应中没有角色信息，尝试获取用户信息（此时token已经保存）
      if (role === null || role === undefined) {
        try {
          const userInfo = await getCurrentUserInfo();
          if (userInfo) {
            role = userInfo.role;
            userId = userInfo.id;
            userName = userInfo.name || userName;
          }
        } catch (e) {
          console.error('获取用户信息失败:', e);
          // 如果获取用户信息失败，使用默认值
        }
      }
      
      // 确保role是数字类型
      let roleNum = ROLE_USER; // 默认值
      if (role !== null && role !== undefined) {
        if (typeof role === 'string') {
          if (role === 'admin' || role === '1') {
            roleNum = ROLE_ADMIN;
          } else if (role === 'doctor' || role === '2') {
            roleNum = ROLE_DOCTOR;
          } else if (role === 'user' || role === '3') {
            roleNum = ROLE_USER;
          } else {
            roleNum = parseInt(role) || ROLE_USER;
          }
        } else {
          roleNum = Number(role) || ROLE_USER;
        }
      }
      
      // 验证role值是否有效
      if (roleNum !== ROLE_ADMIN && roleNum !== ROLE_DOCTOR && roleNum !== ROLE_USER) {
        console.warn('无效的角色值:', role, '，使用默认值3（用户）');
        roleNum = ROLE_USER;
      }
      
      console.log('登录成功 - 原始角色值:', role, '类型:', typeof role);
      console.log('登录成功 - 转换后角色值:', roleNum, '类型:', typeof roleNum);
      console.log('角色判断 - ROLE_ADMIN:', ROLE_ADMIN, 'ROLE_DOCTOR:', ROLE_DOCTOR, 'ROLE_USER:', ROLE_USER);
      console.log('角色比较 - roleNum === ROLE_ADMIN:', roleNum === ROLE_ADMIN);
      console.log('角色比较 - roleNum === ROLE_DOCTOR:', roleNum === ROLE_DOCTOR);
      console.log('角色比较 - roleNum === ROLE_USER:', roleNum === ROLE_USER);
      
      // 保存用户信息到localStorage
      const userInfo: UserInfo = {
        id: userId || '',
        username: userName,
        name: userName,
        role: roleNum
      };
      saveUserInfo(userInfo);
      
      // 检测是否为移动端
      const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) || 
                       (window.innerWidth <= 768);
      
      // 根据角色跳转到不同的首页
      // Layout组件会根据设备类型自动显示移动端或桌面端布局
      // role: 1=管理员, 2=医生, 3=用户
      if (roleNum === ROLE_ADMIN) {
        console.log('跳转到管理员页面', isMobile ? '(移动端)' : '(桌面端)');
        router.push('/admin/users'); // 管理员主页（Layout会自动适配移动端）
      } else if (roleNum === ROLE_DOCTOR) {
        console.log('跳转到医生页面', isMobile ? '(移动端)' : '(桌面端)');
        // 移动端直接跳转到 /doctor 显示主页，电脑端会在DoctorLayout中重定向到 /doctor/apply
        router.push('/doctor');
      } else {
        console.log('跳转到用户页面', isMobile ? '(移动端)' : '(桌面端)');
        // 移动端直接跳转到 /user 显示主页，电脑端会在UserLayout中重定向到 /user/pets
        router.push('/user');
      }
    } else {
      // 处理各种错误情况
      const message = data.message || '';
      if (message.includes('验证码')) {
        // 验证码错误，刷新验证码
        errors.captcha = message;
        refreshCaptcha();
        form.captcha = '';
      } else if (message === 'PASSWORD_ERR' || message === 'USERNAME_NOT_EXIST') {
        showMessage('用户名或密码错误', 'error');
        form.username = '';
        form.password = '';
        refreshCaptcha();
        form.captcha = '';
      } else {
        showMessage(message || '登陆失败！请找管理员授权!', 'error');
        refreshCaptcha();
        form.captcha = '';
      }
    }
  } catch (e) {
    showMessage('系统错误,服务器正忙！', 'error');
    refreshCaptcha();
    form.captcha = '';
  } finally {
    loading.value = false;
  }
}

function goRegister() {
  router.push('/register');
}

function goForgetPassword() {
  router.push('/forget-password');
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  margin: 0;
  font-family: "Arial", sans-serif;
  font-weight: lighter;
  color: #626262;
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
.login-container::before {
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
.login-container::after {
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
  background: rgba(103, 194, 58, 0.7);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.9), 0 0 15px rgba(133, 206, 97, 0.5);
  transition: opacity 0.3s ease;
  pointer-events: none;
  will-change: transform;
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

.login-box {
  position: relative;
  width: 336px;
  max-width: 90vw;
  background-color: #3c3f41;
  box-shadow: 0px 0px 43px -2px wheat;
  border-radius: 8px;
  padding: 30px;
  color: white;
  opacity: 0.95;
  z-index: 1;
  margin-left: 320px;
  margin-right: 320px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 移动端登录框样式 */
@media (max-width: 767px) {
  .login-box {
    width: 90vw;
    padding: 25px 20px;
  }
  
  .user-image img {
    width: 80px;
    height: 80px;
  }
  
  .system-title {
    font-size: 16px;
    margin-top: 6px;
  }
  
  .login-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  .input-field {
    padding: 12px 10px;
    font-size: 16px; /* 防止iOS自动缩放 */
  }
  
  .captcha-wrapper {
    flex-direction: column;
    gap: 10px;
  }
  
  .captcha-image-wrapper {
    width: 100%;
    height: 45px;
  }
}

.user-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 10px auto;
}

.user-image img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border-style: none;
  border-width: 0px;
  border-color: rgba(0, 0, 0, 0);
  border-image: none;
  margin-bottom: 10px;
}

.system-title {
  color: white;
  font-size: 18px;
  font-weight: 500;
  text-align: center;
  margin-top: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.login-form {
  width: 250px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__label) {
  color: white;
  font-size: 14px;
  padding-right: 12px;
  text-align: right;
}

.login-form :deep(.el-form-item__content) {
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
  display: flex;
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
  width: 50px;
}

.input-field.warning :deep(.el-input__wrapper) {
  border-color: #f56c6c;
}

.input-field.warning :deep(.el-input__wrapper.is-focus) {
  border-color: #f56c6c;
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
  color: #67c23a;
}

.error-tip {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 5px;
  padding: 6px 12px;
  color: white;
  background-color: #f56c6c;
  border-radius: 4px;
  font-size: 12px;
  z-index: 10;
  white-space: nowrap;
}

.triangle {
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid #f56c6c;
  position: absolute;
  left: 20px;
  top: -6px;
}

.btn-login {
  border-radius: 6px;
  background-color: #67c23a;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
  font-family: "Noto Sans SC";
  overflow: hidden;
}

.btn-login:hover {
  background-color: #5daf34;
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
  transform: translateY(-1px);
}

.btn-login:active {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-register {
  border-radius: 6px;
  background-color: #85ce61;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(133, 206, 97, 0.2);
  padding-left: 60px;
  padding-right: 60px;
}

.btn-register:hover {
  background-color: #73c04d;
  box-shadow: 0 4px 8px rgba(133, 206, 97, 0.3);
  transform: translateY(-1px);
}

.btn-register:active {
  transform: translateY(0);
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

/* 验证码输入框和图片的总宽度等于其他输入框的宽度 */
.captcha-input {
  flex: 1;
  min-width: 0;
  width: 0; /* 让flex生效 */
}

.captcha-input :deep(.el-input) {
  width: 90%;
}

.captcha-input :deep(.el-input__wrapper) {
  width: 100%;
  box-sizing: border-box;
}

.captcha-input {
  flex: 1;
}

.captcha-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  background-color: #fff;
  width: 100% !important;
  box-sizing: border-box;
}

.captcha-input :deep(.el-input__wrapper:hover) {
  border-color: #67c23a;
}

.captcha-input :deep(.el-input__wrapper.is-focus) {
  border-color: #67c23a;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.1);
}

.captcha-input.warning :deep(.el-input__wrapper) {
  border-color: #f56c6c;
}

.captcha-image-wrapper {
  position: relative;
  width: 120px;
  height: 40px;
  min-height: 40px;
  min-width: 120px;
  max-width: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background-color: #f5f5f5;
  transition: all 0.3s;
  flex-shrink: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-image-wrapper:hover {
  border-color: #67c23a;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.1);
}

.captcha-image-wrapper.loading {
  cursor: wait;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  text-align: center;
  padding: 0 4px;
}

.captcha-refresh-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  text-align: center;
  padding: 2px 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.captcha-image-wrapper:hover .captcha-refresh-hint {
  opacity: 1;
}
</style>
