<template>
  <div class="free-time-modern">
    <!-- 日历视图 -->
    <div v-if="!selectedDay" class="calendar-view">
      <div class="modern-page-header">
        <h1 class="modern-page-title">
          <span>📅</span>
          我的工作时间
        </h1>
        <p class="modern-page-subtitle">点击日期查看当天的可预约时间</p>
      </div>

      <div class="calendar-container modern-card">
        <div class="calendar-header">
          <div class="calendar-nav">
            <button class="nav-btn" @click="prevMonth" :disabled="isCurrentMonth">‹</button>
            <span class="month-year">{{ currentMonthYear }}</span>
            <button class="nav-btn" @click="nextMonth">›</button>
          </div>
        </div>

        <div class="calendar-grid">
          <div class="calendar-weekdays">
            <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
          </div>
          <div class="calendar-days">
            <div
              v-for="(day, index) in calendarDays"
              :key="index"
              class="calendar-day"
              :class="{
                'other-month': day.otherMonth,
                'today': day.isToday,
                'past': day.isPast,
                'selectable': !day.isPast
              }"
              @click="selectDay(day)"
            >
              <div class="day-number">{{ day.date }}</div>
              <div v-if="day.isToday" class="today-badge">今天</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 日期详情视图 -->
    <div v-else class="day-detail-view">
      <div class="detail-header">
        <button class="back-btn" @click="goBackToCalendar">
          <span><</span> 返回日历
        </button>
        <h2 class="detail-title">{{ selectedDayText }}</h2>
      </div>

      <div v-if="loading" class="modern-loading">加载中...</div>
      <div v-else class="doctor-card modern-card">
        <div class="doctor-header">
          <div class="doctor-avatar">👨‍⚕️</div>
          <div class="doctor-info">
            <h3 class="doctor-name">{{ currentDoctorName }}</h3>
          </div>
        </div>

        <div class="time-slots-container">
          <div v-if="timeSlots.length === 0" class="no-time-slots">
            当天无排班
          </div>
          <div v-else class="time-slots-grid">
            <div
              v-for="slot in timeSlots"
              :key="slot.timeSlot"
              class="time-slot-item"
              :class="{
                  'available': slot.canBook && !slot.isExpired,
                  'full': !slot.canBook && !slot.isExpired,
                'expired': slot.isExpired
              }"
            >
              <div class="time-slot-time">{{ slot.timeSlot }}</div>
              <div class="time-slot-info">
                <span class="slot-status">
                  <span v-if="slot.isExpired" class="status-expired">
                    ✗ 已过期
                  </span>
                  <span v-else-if="slot.canBook" class="status-available">
                    ✓ 可预约
                  </span>
                  <span v-else class="status-full">✗ 已满</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import http from '../../api/http';
import { getUserInfo } from '../../utils/user';

interface TimeSlot {
  timeSlot: string;
  used: number;
  canBook: boolean;
  isExpired?: boolean;
}

interface CalendarDay {
  date: number;
  fullDate: string;
  isToday: boolean;
  isPast: boolean;
  otherMonth: boolean;
}

const selectedDay = ref<string | null>(null);
const loading = ref(false);
const timeSlots = ref<TimeSlot[]>([]);
const currentDate = ref(new Date());
const currentDoctorId = ref<string>('');
const currentDoctorName = ref<string>('');

const weekdays = ['日', '一', '二', '三', '四', '五', '六'];

const currentMonthYear = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth() + 1;
  return `${year}年${month}月`;
});

const isCurrentMonth = computed(() => {
  const today = new Date();
  return (
    currentDate.value.getFullYear() === today.getFullYear() &&
    currentDate.value.getMonth() === today.getMonth()
  );
});

const selectedDayText = computed(() => {
  if (!selectedDay.value) return '';
  const date = new Date(selectedDay.value);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekday = weekdays[date.getDay()];
  return `${year}年${month}月${day}日 星期${weekday}`;
});

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const firstDay = new Date(year, month, 1);
  const firstDayWeek = firstDay.getDay();
  const lastDay = new Date(year, month + 1, 0);
  const lastDate = lastDay.getDate();
  const prevMonthLastDay = new Date(year, month, 0);
  const prevMonthLastDate = prevMonthLastDay.getDate();

  const days: CalendarDay[] = [];

  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const date = prevMonthLastDate - i;
    const fullDate = new Date(year, month - 1, date);
    days.push({
      date,
      fullDate: formatDate(fullDate),
      isToday: false,
      isPast: fullDate < today,
      otherMonth: true
    });
  }

  for (let date = 1; date <= lastDate; date++) {
    const fullDate = new Date(year, month, date);
    const isToday = fullDate.getTime() === today.getTime();
    days.push({
      date,
      fullDate: formatDate(fullDate),
      isToday,
      isPast: fullDate < today,
      otherMonth: false
    });
  }

  const remainingDays = 42 - days.length;
  for (let date = 1; date <= remainingDays; date++) {
    const fullDate = new Date(year, month + 1, date);
    days.push({
      date,
      fullDate: formatDate(fullDate),
      isToday: false,
      isPast: fullDate < today,
      otherMonth: true
    });
  }

  return days;
});

function formatDate(date: Date): string {
  // 使用本地时区，避免时区转换问题
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function prevMonth() {
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() - 1);
  currentDate.value = newDate;
}

function nextMonth() {
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + 1);
  currentDate.value = newDate;
}

function selectDay(day: CalendarDay) {
  if (day.isPast) return;
  selectedDay.value = day.fullDate;
  fetchDayDetails(day.fullDate);
}

function goBackToCalendar() {
  selectedDay.value = null;
  timeSlots.value = [];
}

// 判断时间段是否已过期
function isTimeSlotExpired(dateStr: string, timeSlot: string): boolean {
  if (!dateStr || !timeSlot) return false;
  
  try {
    // 解析时间段，格式如 "09:00-10:00"
    const timeMatch = timeSlot.match(/^(\d{2}):(\d{2})-/);
    if (!timeMatch) return false;
    
    const hour = parseInt(timeMatch[1], 10);
    const minute = parseInt(timeMatch[2], 10);
    
    // 获取选择的日期
    const selectedDate = new Date(dateStr + 'T00:00:00');
    const selectedYear = selectedDate.getFullYear();
    const selectedMonth = selectedDate.getMonth();
    const selectedDay = selectedDate.getDate();
    
    // 获取当前日期和时间
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const selected = new Date(selectedYear, selectedMonth, selectedDay);
    
    // 如果选择的日期是过去的日期，所有时间段都过期
    if (selected < today) {
      return true;
    }
    
    // 如果选择的日期是今天，检查时间段是否已过期
    if (selected.getTime() === today.getTime()) {
      const slotTime = new Date(selectedYear, selectedMonth, selectedDay, hour, minute);
      return slotTime < now;
    }
    
    // 未来的日期，时间段未过期
    return false;
  } catch (e) {
    console.error('判断时间段是否过期失败:', e);
    return false;
  }
}

async function fetchDayDetails(date: string) {
  if (!currentDoctorId.value) {
    console.error('医生ID未获取');
    return;
  }

  loading.value = true;
  try {
    const resp = await http.get('/user/apply/getAvailableSlots', {
      params: {
        doctorId: currentDoctorId.value,
        appDate: date
      }
    });

    if (Array.isArray(resp.data)) {
      // 处理时间段，标记已过期的时间段
      timeSlots.value = resp.data.map((slot: any) => {
        const isExpired = isTimeSlotExpired(date, slot.timeSlot);
        return {
          ...slot,
          isExpired,
          canBook: slot.canBook && !isExpired
        };
      });
    } else if (resp.data && typeof resp.data === 'object' && resp.data.timeSlots) {
      timeSlots.value = resp.data.timeSlots.map((slot: any) => {
        const isExpired = isTimeSlotExpired(date, slot.timeSlot);
        return {
          ...slot,
          isExpired,
          canBook: slot.canBook && !isExpired
        };
      });
    } else {
      timeSlots.value = [];
    }
  } catch (e) {
    console.error('获取时间段失败:', e);
    timeSlots.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  const userInfo = getUserInfo();
  if (userInfo) {
    currentDoctorId.value = String(userInfo.id || '');
    currentDoctorName.value = userInfo.name || userInfo.username || '医生';
  }
});
</script>

<style scoped>
@import '../../assets/modern-ui.css';

.free-time-modern {
  padding: 20px;
}

.calendar-container {
  padding: 24px;
}

.calendar-header {
  margin-bottom: 20px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #72C1BB;
  border-radius: 8px;
  background: white;
  color: #72C1BB;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: #72C1BB;
  color: white;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.month-year {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  min-width: 150px;
  text-align: center;
}

.calendar-grid {
  width: 100%;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-weight: 600;
  color: #666;
  padding: 12px;
  font-size: 14px;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  aspect-ratio: 1;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  background: white;
}

.calendar-day.other-month {
  opacity: 0.3;
  cursor: default;
}

.calendar-day.past {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f9fafb;
}

.calendar-day.selectable:hover {
  border-color: #72C1BB;
  background: #f0f9f8;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(114, 193, 187, 0.2);
}

.calendar-day.today {
  border-color: #72C1BB;
  background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
  font-weight: 600;
}

@media (max-width: 768px) {
  .calendar-day.today {
    background: #10b981;
    border-color: #10b981;
  }
  
  .calendar-day.today .day-number {
    color: white;
  }
}

.day-number {
  font-size: 16px;
  color: #333;
}

.today-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #72C1BB;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 2px solid #72C1BB;
  border-radius: 8px;
  background: white;
  color: #72C1BB;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #72C1BB;
  color: white;
}

.back-btn span {
  font-size: 18px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.doctor-card {
  padding: 20px;
}

.doctor-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f9f8;
  margin-bottom: 16px;
}

.doctor-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.2);
}

.doctor-info {
  flex: 1;
}

.doctor-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.time-slots-container {
  margin-top: 16px;
}

.no-time-slots {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.time-slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.time-slot-item {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  background: white;
  transition: all 0.2s;
}

.time-slot-item.available {
  border-color: #10b981;
  background: #f0fdf4;
}

.time-slot-item.available:hover {
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.2);
  transform: translateY(-2px);
}

.time-slot-item.full {
  border-color: #ef4444;
  background: #fef2f2;
  opacity: 0.7;
}

.time-slot-item.limited {
  border-color: #f59e0b;
  background: #fffbeb;
}

.time-slot-item.expired {
  border-color: #ef4444;
  background: #fee2e2;
  opacity: 0.7;
}

.time-slot-item.expired .time-slot-time {
  color: #dc2626;
}

.status-expired {
  color: #dc2626;
  font-weight: 500;
}

.time-slot-time {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.time-slot-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.slot-status {
  font-weight: 500;
}

.status-available {
  color: #10b981;
}

.status-full {
  color: #ef4444;
}

.slot-count {
  color: #6b7280;
  font-size: 12px;
}

@media (max-width: 768px) {
  .calendar-weekdays {
    gap: 4px;
  }

  .weekday {
    padding: 8px 4px;
    font-size: 12px;
  }

  .calendar-days {
    gap: 4px;
  }

  .calendar-day {
    padding: 4px;
    min-height: 40px;
  }

  .day-number {
    font-size: 14px;
  }

  .today-badge {
    display: none;
  }

  .calendar-day.today {
    background: #10b981;
    border-color: #10b981;
  }
  
  .calendar-day.today .day-number {
    color: white;
  }

  .time-slots-grid {
    grid-template-columns: 1fr;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .calendar-container {
    padding: 16px;
  }

  .month-year {
    font-size: 16px;
    min-width: 120px;
  }

  .nav-btn {
    width: 32px;
    height: 32px;
    font-size: 20px;
  }
}
</style>
