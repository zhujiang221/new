<template>
  <div class="chat-request-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <span class="search-icon">🔍</span>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索医生姓名..."
          @input="handleSearch"
        />
      </div>
    </div>

    <!-- 好友申请入口 -->
    <div class="friend-request-section" @click="showRequestList = true">
      <div class="section-icon">👥</div>
      <div class="section-content">
        <div class="section-title">好友申请</div>
        <div class="section-subtitle">查看自己申请的记录</div>
      </div>
      <div class="section-arrow">›</div>
    </div>

    <!-- 医生列表 -->
    <div class="doctor-list-container" ref="listContainer">
      <div
        v-for="(group, letter) in groupedDoctors"
        :key="letter"
        class="letter-section"
        :data-letter="letter"
      >
        <div class="letter-header">{{ letter }}</div>
        <div class="doctor-items">
          <div
            v-for="doctor in group"
            :key="doctor.id"
            class="doctor-item"
            @click="handleDoctorClick(doctor)"
          >
            <div class="doctor-avatar">
              {{ getAvatarText(doctor.name) }}
            </div>
            <div class="doctor-info">
              <div class="doctor-name">{{ doctor.name }}</div>
              <div class="doctor-phone" v-if="doctor.phone">{{ doctor.phone }}</div>
            </div>
            <div class="doctor-status" v-if="getDoctorStatus(doctor.id)">
              <span :class="['status-badge', getStatusClass(getDoctorStatus(doctor.id))]">
                {{ getStatusText(getDoctorStatus(doctor.id)) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredDoctors.length === 0" class="empty-state">
        <div class="empty-icon">👨‍⚕️</div>
        <div class="empty-text">暂无医生</div>
      </div>
      </div>

    <!-- 右侧字母索引 -->
    <div class="letter-index" v-if="filteredDoctors.length > 0">
      <div
        v-for="letter in sortedLetters"
        :key="letter"
        class="letter-item"
        :class="{ active: currentLetter === letter }"
        @click="scrollToLetter(letter)"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      >
        {{ letter }}
      </div>
    </div>

    <!-- 好友申请列表弹窗 -->
    <el-dialog
      v-model="showRequestList"
      title="好友申请"
      width="90%"
      :close-on-click-modal="true"
    >
      <div class="request-list-content">
      <div v-if="requestList.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无申请记录</div>
      </div>
        <div v-else class="request-items">
          <div
            v-for="request in requestList"
            :key="request.id"
            class="request-item"
          >
            <div class="request-avatar">
              {{ getAvatarText(request.doctorName || '医生') }}
            </div>
            <div class="request-info">
              <div class="request-doctor-name">{{ request.doctorName || '未知医生' }}</div>
              <div class="request-message" v-if="request.requestMessage">
                {{ request.requestMessage }}
              </div>
              <div class="request-time">{{ formatTime(request.createTime) }}</div>
            </div>
            <div class="request-status">
              <span :class="['status-badge', getStatusClass(request.status)]">
              {{ getStatusText(request.status) }}
              </span>
            </div>
          </div>
        </div>
          </div>
    </el-dialog>

    <!-- 申请留言弹窗 -->
    <el-dialog
      v-model="showRequestDialog"
      title="发起聊天申请"
      width="90%"
      :close-on-click-modal="false"
    >
      <div class="request-dialog-content">
        <div class="selected-doctor">
          <div class="doctor-avatar">{{ getAvatarText(selectedDoctor?.name || '') }}</div>
          <div class="doctor-name">{{ selectedDoctor?.name }}</div>
          </div>
        <div class="form-group">
          <label>申请留言（可选）：</label>
          <el-input
            v-model="requestMessage"
            type="textarea"
            :rows="4"
            placeholder="请输入您想对医生说的话..."
            maxlength="500"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showRequestDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRequest" :loading="submitting">
          {{ submitting ? '提交中...' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import http from '../../api/http';
import { createChatRequest, getChatRequestList, type ChatRequest } from '../../api/chat';
import { showMessage } from '../../utils/message';
import { groupByPinyin, sortByPinyin } from '../../utils/pinyin';

const router = useRouter();

interface Doctor {
  id: string;
  name: string;
  phone?: string;
}

const searchKeyword = ref('');
const doctorList = ref<Doctor[]>([]);
const requestList = ref<ChatRequest[]>([]);
const showRequestList = ref(false);
const showRequestDialog = ref(false);
const selectedDoctor = ref<Doctor | null>(null);
const requestMessage = ref('');
const submitting = ref(false);
const currentLetter = ref('');
const listContainer = ref<HTMLElement | null>(null);
const touchStartY = ref(0);

// 过滤后的医生列表
const filteredDoctors = computed(() => {
  if (!searchKeyword.value.trim()) {
    return doctorList.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return doctorList.value.filter(doctor =>
    doctor.name.toLowerCase().includes(keyword) ||
    (doctor.phone && doctor.phone.includes(keyword))
  );
});

// 按拼音分组的医生列表
const groupedDoctors = computed(() => {
  return groupByPinyin(filteredDoctors.value, (doctor) => doctor.name);
});

// 排序后的字母列表
const sortedLetters = computed(() => {
  const letters = Object.keys(groupedDoctors.value).sort((a, b) => {
    if (a === '#') return 1;
    if (b === '#') return -1;
    return a.localeCompare(b);
  });
  return letters;
});


onMounted(() => {
  fetchDoctors();
  fetchRequests();
});

async function fetchDoctors() {
  try {
    const resp = await http.get('/admin/getAllUserByRoleId', {
      params: { roleId: 2, page: 1, limit: 1000 }
    });
    const data = resp.data;
    let doctors: any[] = [];
    if (Array.isArray(data)) {
      doctors = data;
    } else if (data && data.rows) {
      doctors = data.rows;
    }
    doctorList.value = sortByPinyin(
      doctors.map((d: any) => ({
      id: String(d.id || d.userId || ''),
        name: d.name || d.username || d.phone || '未知',
        phone: d.phone
      })),
      (doctor) => doctor.name
    );
  } catch (e) {
    console.error('获取医生列表失败:', e);
    showMessage('获取医生列表失败', 'error');
  }
}

async function fetchRequests() {
  try {
    const list = await getChatRequestList();
    requestList.value = list || [];
  } catch (e) {
    console.error('获取申请列表失败:', e);
  }
}

function handleSearch() {
  // 搜索时重置当前字母
  currentLetter.value = '';
}

function handleDoctorClick(doctor: Doctor) {
  // 检查是否已有待审核或已同意的申请
  const existing = requestList.value.find(
    r => r.doctorId === Number(doctor.id) && (r.status === 0 || r.status === 1)
  );
  
  if (existing) {
    if (existing.status === 0) {
      showMessage('您已向该医生发起过申请，请等待审核', 'warning');
    } else if (existing.status === 1) {
      showMessage('您已与该医生建立聊天会话，可以直接开始聊天', 'success');
      router.push('/user/chat');
    }
    return;
  }
  
  selectedDoctor.value = doctor;
  requestMessage.value = '';
  showRequestDialog.value = true;
}

async function submitRequest() {
  if (!selectedDoctor.value) return;

  submitting.value = true;
  try {
    const result = await createChatRequest(Number(selectedDoctor.value.id), requestMessage.value);
    if (result === 'SUCCESS') {
      showMessage('申请提交成功，请等待医生审核', 'success');
      requestMessage.value = '';
      showRequestDialog.value = false;
      await fetchRequests();
    } else {
      showMessage(result.includes('ERROR') ? result : '申请提交失败', 'error');
    }
  } catch (e: any) {
    console.error('提交申请失败:', e);
    showMessage(e.message || '申请提交失败', 'error');
  } finally {
    submitting.value = false;
  }
}

function getDoctorStatus(doctorId: string): number | null {
  const request = requestList.value.find(r => r.doctorId === Number(doctorId));
  return request ? request.status : null;
}

function getStatusText(status: number): string {
  switch (status) {
    case 0: return '待审核';
    case 1: return '已同意';
    case 2: return '已拒绝';
    default: return '未知';
  }
}

function getStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending';
    case 1: return 'status-approved';
    case 2: return 'status-rejected';
    default: return '';
  }
}

function getAvatarText(name: string): string {
  if (!name) return '👨‍⚕️';
  // 取最后一个字符作为头像
  return name.slice(-1);
}

function formatTime(timeStr?: string): string {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days === 0) {
    return '今天 ' + String(date.getHours()).padStart(2, '0') + ':' + String(date.getMinutes()).padStart(2, '0');
  } else if (days === 1) {
    return '昨天 ' + String(date.getHours()).padStart(2, '0') + ':' + String(date.getMinutes()).padStart(2, '0');
  } else if (days < 7) {
    return days + '天前';
  } else {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}

function scrollToLetter(letter: string) {
  currentLetter.value = letter;
  if (!listContainer.value) return;
  
  const section = listContainer.value.querySelector(`[data-letter="${letter}"]`);
  if (section) {
    section.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

function handleTouchStart(e: TouchEvent) {
  touchStartY.value = e.touches[0].clientY;
  handleTouchMove(e);
}

function handleTouchMove(e: TouchEvent) {
  if (!listContainer.value) return;
  const y = e.touches[0].clientY;
  const containerRect = listContainer.value.getBoundingClientRect();
  const relativeY = y - containerRect.top;
  const letterHeight = containerRect.height / sortedLetters.value.length;
  const index = Math.floor(relativeY / letterHeight);
  const letter = sortedLetters.value[Math.max(0, Math.min(index, sortedLetters.value.length - 1))];
  
  if (letter && letter !== currentLetter.value) {
    scrollToLetter(letter);
  }
}

function handleTouchEnd() {
  // 触摸结束，可以添加反馈
}
</script>

<style scoped>
.chat-request-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  position: relative;
}

/* 搜索栏 */
.search-bar {
  background: white;
  padding: 10px 15px;
  border-bottom: 1px solid #e0e0e0;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  border-radius: 8px;
  padding: 8px 12px;
  width: 100%;
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
  color: #999;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

/* 好友申请入口 */
.friend-request-section {
  display: flex;
  align-items: center;
  background: white;
  padding: 15px;
  margin-top: 1px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.friend-request-section:active {
  background-color: #f0f0f0;
}

.section-icon {
  font-size: 24px;
  margin-right: 12px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 8px;
}

.section-content {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.section-subtitle {
  font-size: 12px;
  color: #999;
}

.section-badge {
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-right: 8px;
  min-width: 18px;
  text-align: center;
}

.section-arrow {
  font-size: 20px;
  color: #ccc;
}

/* 医生列表容器 */
.doctor-list-container {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 字母分组 */
.letter-section {
  margin-top: 1px;
}

.letter-header {
  background: #f0f0f0;
  padding: 4px 15px;
  font-size: 12px;
  color: #666;
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 10;
}

.doctor-items {
  background: white;
}

/* 医生项 */
.doctor-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.doctor-item:active {
  background-color: #f5f5f5;
}

.doctor-item:last-child {
  border-bottom: none;
}

.doctor-avatar {
  width: 45px;
  height: 45px;
  border-radius: 8px;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 500;
  margin-right: 12px;
  flex-shrink: 0;
}

.doctor-info {
  flex: 1;
  min-width: 0;
}

.doctor-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doctor-phone {
  font-size: 12px;
  color: #999;
}

.doctor-status {
  margin-left: 8px;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

/* 右侧字母索引 */
.letter-index {
  position: fixed;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  z-index: 100;
  padding: 5px 0;
}

.letter-item {
  font-size: 11px;
  color: #72C1BB;
  padding: 2px 4px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}

.letter-item.active {
  color: white;
  background: #72C1BB;
  border-radius: 4px;
  font-weight: bold;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
}

/* 申请列表弹窗内容 */
.request-list-content {
  max-height: 60vh;
  overflow-y: auto;
}

.request-items {
  display: flex;
  flex-direction: column;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.request-item:last-child {
  border-bottom: none;
}

.request-avatar {
  width: 45px;
  height: 45px;
  border-radius: 8px;
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 500;
  margin-right: 12px;
  flex-shrink: 0;
}

.request-info {
  flex: 1;
  min-width: 0;
}

.request-doctor-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
  font-weight: 500;
}

.request-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-time {
  font-size: 12px;
  color: #999;
}

/* 申请对话框内容 */
.request-dialog-content {
  padding: 10px 0;
}

.selected-doctor {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.selected-doctor .doctor-avatar {
  margin-right: 12px;
}

.selected-doctor .doctor-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.form-group {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .letter-index {
    right: 2px;
  }

  .letter-item {
    font-size: 10px;
    padding: 1px 3px;
  }
}
</style>
