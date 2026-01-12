<template>
    <div class="apply-flow-modern">
      <!-- 页面标题 -->
      <div class="modern-page-header">
        <h1 class="modern-page-title">
          <span>📅</span>
          预约就诊
        </h1>
        <p class="modern-page-subtitle">请按步骤完成预约信息填写</p>
      </div>
  
      <!-- 步骤指示器 -->
      <div class="step-indicator">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="step-item"
          :class="{ 
            'active': currentStep === index + 1,
            'completed': currentStep > index + 1
          }"
        >
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-label">{{ step }}</div>
        </div>
      </div>
  
      <!-- 步骤1: 选择宠物(仅从预约页面进入时显示) -->
      <div v-if="currentStep === 1 && !petId" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">选择宠物</h3>
          <div v-if="petListLoading" class="modern-loading">加载中...</div>
          <div v-else-if="petList.length === 0" class="modern-empty-state">
            <div class="modern-empty-state-icon">🐾</div>
            <div class="modern-empty-state-text">您还没有添加宠物</div>
          </div>
          <div v-else class="pet-selection">
            <div 
              v-for="pet in petList" 
              :key="pet.id"
              class="pet-option"
              :class="{ 'selected': selectedPetId === pet.id }"
              @click="selectPet(pet.id)"
            >
              <div class="pet-option-avatar">
                {{ pet.type === '1' ? '🐱' : pet.type === '2' ? '🐶' : '🐾' }}
              </div>
              <div class="pet-option-info">
                <div class="pet-option-name">{{ pet.name }}</div>
                <div class="pet-option-type">{{ formatType(pet.type) }} · {{ formatSex(pet.sex) }}</div>
              </div>
            </div>
          </div>
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="goBack">返回</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="nextStep"
              :disabled="!selectedPetId"
            >
              下一步
            </button>
          </div>
        </div>
      </div>
  
      <!-- 步骤2: 选择预约类型 -->
      <div v-if="currentStep === (petId ? 1 : 2)" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">选择预约类型</h3>
          <div v-if="appointmentTypeLoading" class="modern-loading">加载中...</div>
          <div v-else-if="appointmentTypeList.length === 0" class="modern-empty-state">
            <div class="modern-empty-state-icon">📋</div>
            <div class="modern-empty-state-text">暂无可用的预约类型</div>
          </div>
          <div v-else class="type-selection">
            <div 
              v-for="type in appointmentTypeList" 
              :key="type.id"
              class="type-option"
              :class="{ 'selected': selectedAppointmentTypeId === type.id }"
              @click="selectAppointmentType(type.id)"
            >
              <div class="type-option-icon">📋</div>
              <div class="type-option-info">
                <div class="type-option-name">{{ type.name }}</div>
                <div v-if="type.description" class="type-option-desc">{{ type.description }}</div>
              </div>
            </div>
          </div>
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="prevStep">上一步</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="nextStep"
              :disabled="!selectedAppointmentTypeId"
            >
              下一步
            </button>
          </div>
        </div>
      </div>
  
      <!-- 步骤3: 选择日期 -->
      <div v-if="currentStep === (petId ? 2 : 3)" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">选择预约日期</h3>
          <!-- 快速选择: 今天到后三天 -->
          <div class="quick-date-selection">
            <div 
              v-for="date in quickDates" 
              :key="date.value"
              class="date-option"
              :class="{ 'selected': selectedDate === date.value }"
              @click="selectDate(date.value)"
            >
              <div class="date-option-day">{{ date.day }}</div>
              <div class="date-option-date">{{ date.date }}</div>
            </div>
          </div>
          
          <!-- 展开更多日期 -->
          <div class="more-date-section">
            <button 
              class="modern-btn modern-btn-outline modern-btn-sm" 
              @click="showMoreDates = !showMoreDates"
            >
              {{ showMoreDates ? '收起' : '展开选择更多日期' }}
            </button>
            <div v-if="showMoreDates" class="more-dates">
              <input 
                type="date" 
                v-model="customDate" 
                class="modern-input"
                :min="minDate"
                @change="selectDate(customDate)"
              />
            </div>
          </div>
  
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="prevStep">上一步</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="nextStep"
              :disabled="!selectedDate"
            >
              下一步
            </button>
          </div>
        </div>
      </div>
  
      <!-- 步骤4: 选择医生 -->
      <div v-if="currentStep === (petId ? 3 : 4)" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">选择医生</h3>
          <div v-if="doctorLoading" class="modern-loading">加载中...</div>
          <div v-else-if="availableDoctors.length === 0" class="modern-empty-state">
            <div class="modern-empty-state-icon">👨‍⚕️</div>
            <div class="modern-empty-state-text">该预约类型下暂无可用医生</div>
          </div>
          <div v-else class="doctor-selection">
            <div 
              v-for="doctor in availableDoctors" 
              :key="doctor.id"
              class="doctor-option"
              :class="{ 'selected': selectedDoctorId === doctor.id }"
              @click="selectDoctor(doctor.id)"
            >
              <div class="doctor-option-avatar">👨‍⚕️</div>
              <div class="doctor-option-info">
                <div class="doctor-option-name">{{ doctor.name }}</div>
              </div>
            </div>
          </div>
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="prevStep">上一步</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="nextStep"
              :disabled="!selectedDoctorId"
            >
              下一步
            </button>
          </div>
        </div>
      </div>
  
      <!-- 步骤5: 选择时间段 -->
      <div v-if="currentStep === (petId ? 4 : 5)" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">选择预约时间段</h3>
          <div v-if="timeSlotLoading" class="modern-loading">加载中...</div>
          <div v-else-if="availableTimeSlots.length === 0" class="modern-empty-state">
            <div class="modern-empty-state-icon">⏰</div>
            <div class="modern-empty-state-text">该医生在该日期暂无可用时间段</div>
          </div>
          <div v-else class="timeslot-selection">
            <div 
              v-for="slot in availableTimeSlots" 
              :key="slot.timeSlot"
              class="timeslot-option"
              :class="{ 
                'selected': selectedTimeSlot === slot.timeSlot,
                'disabled': !slot.canBook || slot.isExpired,
                'expired': slot.isExpired
              }"
              @click="slot.canBook && !slot.isExpired && selectTimeSlot(slot.timeSlot)"
            >
              <div class="timeslot-time">{{ slot.timeSlot }}</div>
              <div class="timeslot-info">
                <span v-if="slot.isExpired" class="timeslot-expired">已过期</span>
                <span v-else-if="slot.canBook">
                  可预约
                </span>
                <span v-else class="timeslot-full">已满</span>
              </div>
            </div>
          </div>
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="prevStep">上一步</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="nextStep"
              :disabled="!selectedTimeSlot"
            >
              下一步
            </button>
          </div>
        </div>
      </div>
  
      <!-- 步骤6: 填写其他信息并提交 -->
      <div v-if="currentStep === (petId ? 5 : 6)" class="step-content">
        <div class="modern-card">
          <h3 class="step-title">填写预约信息</h3>
          
          <!-- 预约信息摘要 -->
          <div class="appointment-summary">
            <div class="summary-item">
              <span class="summary-label">宠物:</span>
              <span class="summary-value">{{ selectedPetName }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">预约类型:</span>
              <span class="summary-value">{{ selectedAppointmentTypeName }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">医生:</span>
              <span class="summary-value">{{ selectedDoctorName }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">日期:</span>
              <span class="summary-value">{{ formatDate(selectedDate) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">时间段:</span>
              <span class="summary-value">{{ selectedTimeSlot }}</span>
            </div>
          </div>
  
          <!-- 表单 -->
          <div class="form-section">
            <div class="form-group">
              <label>预约内容：</label>
              <textarea 
                v-model="applyForm.info" 
                class="form-control" 
                rows="3" 
                placeholder="请描述您的预约需求，如看病、疫苗、洗澡等"
              ></textarea>
            </div>
            <div class="form-group">
              <label>联系电话：</label>
              <input 
                type="text" 
                v-model="applyForm.phone" 
                class="form-control" 
                placeholder="请输入联系电话"
              />
            </div>
            <div class="form-group">
              <label>地址：</label>
              <input 
                type="text" 
                v-model="applyForm.address" 
                class="form-control" 
                placeholder="请输入地址"
              />
            </div>
          </div>
  
          <div class="step-actions">
            <button class="modern-btn modern-btn-outline" @click="prevStep">上一步</button>
            <button 
              class="modern-btn modern-btn-primary" 
              @click="submitApply"
              :disabled="submitting"
            >
              {{ submitting ? '提交中...' : '提交预约' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
    import { ref, reactive, computed, onMounted, watch } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import http from '../../api/http';
    import { showMessage } from '../../utils/message';
    
    const route = useRoute();
    const router = useRouter();
    
    // 从路由参数获取宠物ID（从宠物管理页面进入时）
    const petId = computed(() => route.query.petId as string | undefined);
    
    // 步骤定义
    const steps = computed(() => {
      if (petId.value) {
        return ['选择预约类型', '选择日期', '选择医生', '选择时间段', '填写信息'];
      } else {
        return ['选择宠物', '选择预约类型', '选择日期', '选择医生', '选择时间段', '填写信息'];
      }
    });
    
    const currentStep = ref(1);
    const showMoreDates = ref(false);
    const customDate = ref('');
    
    // 宠物相关
    const petList = ref<any[]>([]);
    const petListLoading = ref(false);
    const selectedPetId = ref<string>('');
    
    // 预约类型相关
    const appointmentTypeList = ref<any[]>([]);
    const appointmentTypeLoading = ref(false);
    const selectedAppointmentTypeId = ref<string>('');
    
    // 日期相关
    const selectedDate = ref<string>('');
    
    // 格式化日期为 YYYY-MM-DD（使用本地时区）
    function formatDateToLocal(date: Date): string {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
    
    const quickDates = computed(() => {
      const dates = [];
      const today = new Date();
      // 设置为本地时区的 00:00:00，避免时区问题
      today.setHours(0, 0, 0, 0);
      const days = ['今天', '明天', '后天', '大后天'];
      
      for (let i = 0; i < 4; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);
        dates.push({
          day: days[i],
          date: `${date.getMonth() + 1}月${date.getDate()}日`,
          value: formatDateToLocal(date)
        });
      }
      return dates;
    });
    
    const minDate = computed(() => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return formatDateToLocal(today);
    });
    
    // 医生相关
    const allDoctors = ref<any[]>([]);
    const availableDoctors = ref<any[]>([]);
    const doctorLoading = ref(false);
    const selectedDoctorId = ref<string>('');
    
    // 时间段相关
    const availableTimeSlots = ref<any[]>([]);
    const timeSlotLoading = ref(false);
    const selectedTimeSlot = ref<string>('');
    
    // 表单数据
    const applyForm = reactive({
      info: '',
      phone: '',
      address: ''
    });
    
    const submitting = ref(false);
    
    // 计算属性：选中的宠物名称
    const selectedPetName = computed(() => {
      if (petId.value) {
        const pet = petList.value.find(p => p.id === petId.value);
        return pet?.name || '';
      } else {
        const pet = petList.value.find(p => p.id === selectedPetId.value);
        return pet?.name || '';
      }
    });
    
    // 计算属性：选中的预约类型名称
    const selectedAppointmentTypeName = computed(() => {
      const type = appointmentTypeList.value.find(t => t.id === selectedAppointmentTypeId.value);
      return type?.name || '';
    });
    
    // 计算属性：选中的医生名称
    const selectedDoctorName = computed(() => {
      const doctor = availableDoctors.value.find(d => d.id === selectedDoctorId.value);
      return doctor?.name || '';
    });
    
    // 格式化宠物类型
    function formatType(type: string) {
      if (type === '1') return '猫科';
      if (type === '2') return '犬科';
      if (type === '3') return '其他';
      return type;
    }
    
    // 格式化宠物性别
    function formatSex(sex: string) {
      if (sex === '1') return '公';
      if (sex === '2') return '母';
      if (sex === '3') return '未知';
      return sex || '未知';
    }
    
    // 格式化日期
    function formatDate(dateStr: string) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getMonth() + 1}月${date.getDate()}日`;
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
    
    // 加载宠物列表
    async function fetchPets() {
      petListLoading.value = true;
      try {
        const resp = await http.get('/user/pet/getAllByLimit', {
          params: { page: 1, limit: 100 }
        });
        const data = resp.data;
        if (data && data.rows) {
          petList.value = data.rows.map((pet: any) => ({
            id: String(pet.id),
            name: pet.name || '未知',
            type: String(pet.type || '1'),
            sex: String(pet.sex || '3')
          }));
        } else if (Array.isArray(data)) {
          petList.value = data.map((pet: any) => ({
            id: String(pet.id),
            name: pet.name || '未知',
            type: String(pet.type || '1'),
            sex: String(pet.sex || '3')
          }));
        }
        
        // 如果从路由参数传入宠物ID，自动选中
        if (petId.value) {
          const pet = petList.value.find(p => p.id === petId.value);
          if (pet) {
            selectedPetId.value = petId.value;
          }
        }
      } catch (e) {
        console.error('获取宠物列表失败:', e);
      } finally {
        petListLoading.value = false;
      }
    }
    
    // 加载预约类型列表
    async function fetchAppointmentTypes() {
      appointmentTypeLoading.value = true;
      try {
        const resp = await http.get('/appointmentType/list');
        const data = resp.data;
        if (Array.isArray(data)) {
          appointmentTypeList.value = data.map((t: any) => ({
            id: String(t.id),
            name: t.name || '未知',
            description: t.description
          }));
        } else {
          appointmentTypeList.value = [];
        }
      } catch (e) {
        console.error('获取预约类型失败:', e);
        appointmentTypeList.value = [];
      } finally {
        appointmentTypeLoading.value = false;
      }
    }
    
    // 加载所有医生列表
    async function fetchAllDoctors() {
      doctorLoading.value = true;
      try {
        const resp = await http.get('/admin/getAllUserByRoleId', {
          params: { roleId: 2, page: 1, limit: 100 }
        });
        const data = resp.data;
        let doctors: any[] = [];
        if (Array.isArray(data)) {
          doctors = data;
        } else if (data && data.rows) {
          doctors = data.rows;
        }
        allDoctors.value = doctors.map((d: any) => ({
          id: String(d.id || d.userId || ''),
          name: d.name || d.username || d.phone || '未知'
        }));
      } catch (e) {
        console.error('获取医生列表失败:', e);
        allDoctors.value = [];
      } finally {
        doctorLoading.value = false;
      }
    }
    
    // 根据预约类型筛选可用医生
    async function filterDoctorsByAppointmentType() {
      if (!selectedAppointmentTypeId.value) {
        availableDoctors.value = [];
        return;
      }
    
      doctorLoading.value = true;
      try {
        // 获取所有医生，然后检查每个医生的服务类型
        const doctorsWithServiceType: any[] = [];
        
        for (const doctor of allDoctors.value) {
          try {
            const resp = await http.get('/doctor/serviceType/list', {
              params: { doctorId: doctor.id }
            });
            if (resp.data && Array.isArray(resp.data)) {
              const serviceTypes = resp.data.map((st: any) => String(st.typeId));
              if (serviceTypes.includes(selectedAppointmentTypeId.value)) {
                doctorsWithServiceType.push(doctor);
              }
            }
          } catch (e) {
            console.error(`获取医生 ${doctor.id} 的服务类型失败:`, e);
          }
        }
        
        availableDoctors.value = doctorsWithServiceType;
      } catch (e) {
        console.error('筛选医生失败:', e);
        availableDoctors.value = [];
      } finally {
        doctorLoading.value = false;
      }
    }
    
    // 加载可用时间段
    async function loadAvailableTimeSlots() {
      if (!selectedDoctorId.value || !selectedDate.value) {
        availableTimeSlots.value = [];
        selectedTimeSlot.value = '';
        return;
      }
    
      timeSlotLoading.value = true;
      try {
        console.log('获取可用时间段 - doctorId:', selectedDoctorId.value, 'appDate:', selectedDate.value);
        const resp = await http.get('/user/apply/getAvailableSlots', {
          params: {
            doctorId: selectedDoctorId.value,
            appDate: selectedDate.value
          }
        });
        console.log('获取可用时间段响应:', resp.data);
        if (Array.isArray(resp.data)) {
          // 处理时间段，标记已过期的时间段
          availableTimeSlots.value = resp.data.map((slot: any) => {
            const isExpired = isTimeSlotExpired(selectedDate.value, slot.timeSlot);
            return {
              ...slot,
              isExpired,
              canBook: slot.canBook && !isExpired // 过期的时间段不能预约
            };
          });
          console.log('可用时间段数量:', resp.data.length);
          // 如果当前选择的时间段已不可用，清空选择
          if (selectedTimeSlot.value) {
            const currentSlot = availableTimeSlots.value.find(s => s.timeSlot === selectedTimeSlot.value);
            if (!currentSlot || !currentSlot.canBook || currentSlot.isExpired) {
              selectedTimeSlot.value = '';
            }
          }
        } else {
          console.warn('返回的数据不是数组:', resp.data);
          availableTimeSlots.value = [];
        }
      } catch (e: any) {
        console.error('获取可用时间段失败:', e);
        console.error('错误详情:', e.response?.data || e.message);
        availableTimeSlots.value = [];
      } finally {
        timeSlotLoading.value = false;
      }
    }
    
    // 选择宠物
    function selectPet(petId: string) {
      selectedPetId.value = petId;
    }
    
    // 选择预约类型
    function selectAppointmentType(typeId: string) {
      selectedAppointmentTypeId.value = typeId;
      // 清空后续选择
      selectedDoctorId.value = '';
      selectedTimeSlot.value = '';
      availableDoctors.value = [];
      availableTimeSlots.value = [];
      // 重新筛选医生
      filterDoctorsByAppointmentType();
    }
    
    // 选择日期
    function selectDate(date: string) {
      selectedDate.value = date;
      customDate.value = date;
      // 清空时间段选择
      selectedTimeSlot.value = '';
      availableTimeSlots.value = [];
      // 如果已选择医生，重新加载时间段
      if (selectedDoctorId.value) {
        loadAvailableTimeSlots();
      }
    }
    
    // 选择医生
    function selectDoctor(doctorId: string) {
      selectedDoctorId.value = doctorId;
      // 清空时间段选择
      selectedTimeSlot.value = '';
      availableTimeSlots.value = [];
      // 如果已选择日期，重新加载时间段
      if (selectedDate.value) {
        loadAvailableTimeSlots();
      }
    }
    
    // 选择时间段
    function selectTimeSlot(timeSlot: string) {
      selectedTimeSlot.value = timeSlot;
    }
    
    // 监听医生和日期的变化，自动加载可用时间段
    watch([() => selectedDoctorId.value, () => selectedDate.value], () => {
      if (selectedDoctorId.value && selectedDate.value) {
        loadAvailableTimeSlots();
      }
    });
    
    // 下一步
    function nextStep() {
      if (currentStep.value < steps.value.length) {
        currentStep.value++;
      }
    }
    
    // 上一步
    function prevStep() {
      if (currentStep.value > 1) {
        currentStep.value--;
      }
    }
    
    // 返回
    function goBack() {
      router.back();
    }
    
    // 提交预约
    async function submitApply() {
      const finalPetId = petId.value || selectedPetId.value;
      
      if (!finalPetId) {
        showMessage('请选择宠物', 'error');
        return;
      }
      if (!selectedAppointmentTypeId.value) {
        showMessage('请选择预约类型', 'error');
        return;
      }
      if (!selectedDoctorId.value) {
        showMessage('请选择医生', 'error');
        return;
      }
      if (!selectedDate.value) {
        showMessage('请选择预约日期', 'error');
        return;
      }
      if (!selectedTimeSlot.value) {
        showMessage('请选择预约时间段', 'error');
        return;
      }
      if (!applyForm.info || !applyForm.info.trim()) {
        showMessage('请输入预约内容', 'error');
        return;
      }
      if (!applyForm.phone || !applyForm.phone.trim()) {
        showMessage('请输入联系电话', 'error');
        return;
      }
      if (!applyForm.address || !applyForm.address.trim()) {
        showMessage('请输入地址', 'error');
        return;
      }
    
      submitting.value = true;
      try {
        const payload = {
          petId: finalPetId,
          appointmentTypeId: selectedAppointmentTypeId.value,
          doctorId: selectedDoctorId.value,
          timeSlot: selectedTimeSlot.value,
          appTime: selectedDate.value + ' 00:00:00',
          info: applyForm.info.trim(),
          phone: applyForm.phone.trim(),
          address: applyForm.address.trim()
        };
    
        const resp = await http.post('/user/apply/doAdd', payload);
        const respData = resp.data;
        if (respData === 'SUCCESS' || respData?.status === 'SUCCESS') {
          showMessage('预约成功', 'success');
          // 延迟跳转，让用户看到成功消息
          setTimeout(() => {
            router.push('/user/apply');
          }, 1000);
        } else if (respData === 'FULL') {
          showMessage('该时间段已约满，请选择其他时间段', 'error');
        } else if (respData === 'noDoctorOrTypeOrSlot') {
          showMessage('请选择医生、预约类型和预约时间段', 'error');
        } else if (respData === 'noPetId') {
          showMessage('请选择宠物', 'error');
        } else {
          showMessage('预约失败: ' + (respData || '未知错误'), 'error');
        }
      } catch (e: any) {
        console.error('提交预约失败:', e);
        showMessage('操作失败: ' + (e.response?.data?.message || e.message || '未知错误'), 'error');
      } finally {
        submitting.value = false;
      }
    }
    
    // 初始化
    onMounted(async () => {
      // 如果从宠物管理页面进入，宠物ID已确定，直接加载预约类型
      // 如果从预约页面进入，需要先加载宠物列表
      if (!petId.value) {
        await fetchPets();
      } else {
        await fetchPets(); // 仍然需要加载，用于显示宠物名称
      }
      
      await fetchAppointmentTypes();
      await fetchAllDoctors();
      
      // 如果已选择预约类型，自动筛选医生
      if (selectedAppointmentTypeId.value) {
        await filterDoctorsByAppointmentType();
      }
    });
    </script>
    
    <style scoped>
    @import '../../assets/modern-ui.css';
    
    .apply-flow-modern {
      padding: 0;
    }
    
    /* 步骤指示器 */
    .step-indicator {
      display: flex;
      justify-content: space-between;
      margin: 32px 0;
      padding: 0 20px;
      position: relative;
    }
    
    .step-indicator::before {
      content: '';
      position: absolute;
      top: 20px;
      left: 10%;
      right: 10%;
      height: 2px;
      background: #e5e7eb;
      z-index: 0;
    }
    
    .step-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      position: relative;
      z-index: 1;
    }
    
    .step-number {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #e5e7eb;
      color: #9ca3af;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 16px;
      margin-bottom: 8px;
      transition: all 0.3s;
    }
    
    .step-item.active .step-number {
      background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.3);
    }
    
    .step-item.completed .step-number {
      background: #10b981;
      color: white;
    }
    
    .step-label {
      font-size: 14px;
      color: #6b7280;
      text-align: center;
    }
    
    .step-item.active .step-label {
      color: #72C1BB;
      font-weight: 600;
    }
    
    .step-item.completed .step-label {
      color: #10b981;
    }
    
    /* 步骤内容 */
    .step-content {
      margin-top: 32px;
    }
    
    .step-title {
      margin: 0 0 24px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
    }
    
    /* 宠物选择 */
    .pet-selection {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .pet-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;
      background: white;
    }
    
    .pet-option:hover {
      border-color: #72C1BB;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.15);
    }
    
    .pet-option.selected {
      border-color: #72C1BB;
      background: #f0f9f8;
    }
    
    .pet-option-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;
    }
    
    .pet-option-info {
      flex: 1;
    }
    
    .pet-option-name {
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .pet-option-type {
      font-size: 12px;
      color: #6b7280;
    }
    
    /* 预约类型选择 */
    .type-selection {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .type-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;
      background: white;
    }
    
    .type-option:hover {
      border-color: #72C1BB;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.15);
    }
    
    .type-option.selected {
      border-color: #72C1BB;
      background: #f0f9f8;
    }
    
    .type-option-icon {
      font-size: 24px;
      flex-shrink: 0;
    }
    
    .type-option-info {
      flex: 1;
    }
    
    .type-option-name {
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .type-option-desc {
      font-size: 12px;
      color: #6b7280;
    }
    
    /* 日期选择 */
    .quick-date-selection {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .date-option {
      padding: 20px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      text-align: center;
      transition: all 0.3s;
      background: white;
    }
    
    .date-option:hover {
      border-color: #72C1BB;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.15);
    }
    
    .date-option.selected {
      border-color: #72C1BB;
      background: #f0f9f8;
    }
    
    .date-option-day {
      font-size: 14px;
      color: #6b7280;
      margin-bottom: 8px;
    }
    
    .date-option-date {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }
    
    .more-date-section {
      margin-bottom: 24px;
    }
    
    .more-dates {
      margin-top: 16px;
    }
    
    .more-dates .modern-input {
      width: 100%;
      max-width: 300px;
    }
    
    /* 医生选择 */
    .doctor-selection {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .doctor-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;
      background: white;
    }
    
    .doctor-option:hover {
      border-color: #72C1BB;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.15);
    }
    
    .doctor-option.selected {
      border-color: #72C1BB;
      background: #f0f9f8;
    }
    
    .doctor-option-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f0f9f8 0%, #e0f2f1 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;
    }
    
    .doctor-option-info {
      flex: 1;
    }
    
    .doctor-option-name {
      font-weight: 600;
      color: #1f2937;
    }
    
    /* 时间段选择 */
    .timeslot-selection {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 12px;
      margin-bottom: 24px;
    }
    
    .timeslot-option {
      padding: 16px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      text-align: center;
      transition: all 0.3s;
      background: white;
    }
    
    .timeslot-option:hover:not(.disabled) {
      border-color: #72C1BB;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(114, 193, 187, 0.15);
    }
    
    .timeslot-option.selected {
      border-color: #72C1BB;
      background: #f0f9f8;
    }
    
    .timeslot-option.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      background: #f3f4f6;
    }
    
    .timeslot-option.expired {
      border-color: #ef4444;
      background: #fee2e2;
      opacity: 0.7;
      cursor: not-allowed;
    }
    
    .timeslot-option.expired .timeslot-time {
      color: #dc2626;
    }
    
    .timeslot-expired {
      color: #dc2626;
      font-weight: 500;
    }
    
    .timeslot-time {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 8px;
    }
    
    .timeslot-info {
      font-size: 12px;
      color: #6b7280;
    }
    
    .timeslot-full {
      color: #ef4444;
    }
    
    /* 预约信息摘要 */
    .appointment-summary {
      background: #f9fafb;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 24px;
    }
    
    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #e5e7eb;
    }
    
    .summary-item:last-child {
      border-bottom: none;
    }
    
    .summary-label {
      font-weight: 500;
      color: #6b7280;
    }
    
    .summary-value {
      font-weight: 600;
      color: #1f2937;
    }
    
    /* 表单部分 */
    .form-section {
      margin-bottom: 24px;
    }
    
    .form-group {
      margin-bottom: 20px;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 8px;
      font-weight: 500;
      color: #374151;
      font-size: 14px;
    }
    
    .form-control {
      width: 100%;
      padding: 10px 14px;
      border: 2px solid #e5e7eb;
      border-radius: 8px;
      font-size: 14px;
      transition: all 0.2s;
      font-family: inherit;
    }
    
    .form-control:focus {
      outline: none;
      border-color: #72C1BB;
      box-shadow: 0 0 0 3px rgba(114, 193, 187, 0.1);
    }
    
    textarea.form-control {
      resize: vertical;
      min-height: 80px;
    }
    
    /* 步骤操作按钮 */
    .step-actions {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      margin-top: 32px;
      padding-top: 24px;
      border-top: 2px solid #f0f9f8;
    }
    
    .step-actions .modern-btn {
      flex: 1;
      max-width: 200px;
    }
    
    /* 响应式优化 */
    @media (max-width: 768px) {
      .step-indicator {
        flex-wrap: wrap;
        gap: 16px;
      }
      
      .step-indicator::before {
        display: none;
      }
      
      .quick-date-selection {
        grid-template-columns: repeat(2, 1fr);
      }
      
      .pet-selection,
      .type-selection,
      .doctor-selection,
      .timeslot-selection {
        grid-template-columns: 1fr;
      }
      
      .step-actions {
        flex-direction: column;
      }
      
      .step-actions .modern-btn {
        max-width: 100%;
      }
    }
    </style>