/**
 * 统计数据相关API
 */
import http from './http';

export interface HomeStatistics {
  pendingAppointments: number; // 待就诊预约数
  petLogs: number; // 宠物日志数
  unreadMessages: number; // 未读消息数
}

export interface RecentAppointment {
  id: number;
  appointmentTypeName: string;
  doctorName: string;
  appDate: string;
  timeSlot: string;
  status: number;
}

export interface HealthTrend {
  weightStatus: 'normal' | 'warning' | 'danger';
  activityStatus: 'excellent' | 'good' | 'normal' | 'poor';
  dietStatus: 'excellent' | 'good' | 'normal' | 'poor';
  weightProgress: number; // 0-100
  activityProgress: number; // 0-100
  dietProgress: number; // 0-100
}

/**
 * 获取主页统计数据
 */
export async function getHomeStatistics(): Promise<HomeStatistics> {
  try {
    const resp = await http.get('/user/home/statistics');
    const data = resp.data;
    
    // 处理不同的响应格式
    if (data && typeof data === 'object') {
      return {
        pendingAppointments: data.pendingAppointments || data.pending_appointments || 0,
        petLogs: data.petLogs || data.pet_logs || 0,
        unreadMessages: data.unreadMessages || data.unread_messages || 0
      };
    }
    
    return {
      pendingAppointments: 0,
      petLogs: 0,
      unreadMessages: 0
    };
  } catch (e) {
    console.error('获取统计数据失败:', e);
    return {
      pendingAppointments: 0,
      petLogs: 0,
      unreadMessages: 0
    };
  }
}

/**
 * 获取近期预约列表
 */
export async function getRecentAppointments(limit: number = 3): Promise<RecentAppointment[]> {
  try {
    const resp = await http.get('/user/home/recentAppointments', {
      params: { limit }
    });
    const data = resp.data;
    
    if (data && Array.isArray(data)) {
      return data;
    }
    
    if (data && data.rows && Array.isArray(data.rows)) {
      return data.rows;
    }
    
    return [];
  } catch (e) {
    console.error('获取近期预约失败:', e);
    return [];
  }
}

/**
 * 获取健康趋势数据
 */
export async function getHealthTrends(): Promise<HealthTrend> {
  try {
    const resp = await http.get('/user/home/healthTrends');
    const data = resp.data;
    
    if (data && typeof data === 'object') {
      return {
        weightStatus: data.weightStatus || 'normal',
        activityStatus: data.activityStatus || 'normal',
        dietStatus: data.dietStatus || 'normal',
        weightProgress: data.weightProgress || data.weight_progress || 0,
        activityProgress: data.activityProgress || data.activity_progress || 0,
        dietProgress: data.dietProgress || data.diet_progress || 0
      };
    }
    
    return {
      weightStatus: 'normal',
      activityStatus: 'normal',
      dietStatus: 'normal',
      weightProgress: 0,
      activityProgress: 0,
      dietProgress: 0
    };
  } catch (e) {
    console.error('获取健康趋势失败:', e);
    return {
      weightStatus: 'normal',
      activityStatus: 'normal',
      dietStatus: 'normal',
      weightProgress: 0,
      activityProgress: 0,
      dietProgress: 0
    };
  }
}

// ========== 医生端接口 ==========

export interface DoctorHomeStatistics {
  pendingAppointments: number; // 待处理预约数
  todayTotalAppointments: number; // 今日预约总数
  todayCompleted: number; // 今日已完成
  inClinicPatients: number; // 在诊患者数
  weekPrescriptions: number; // 本周开药数
}

export interface TodayScheduleItem {
  id: number;
  petName: string;
  petBreed?: string;
  appointmentTypeName: string;
  timeSlot: string;
  status: number;
  statusText: string;
}

export interface WorkStatistics {
  weekConsultations: number; // 本周接诊量
  weekConsultationsTarget: number; // 本周接诊量目标
  patientSatisfaction: number; // 患者满意度
  monthPrescriptions: number; // 本月开药量
}

/**
 * 获取医生主页统计数据
 */
export async function getDoctorHomeStatistics(): Promise<DoctorHomeStatistics> {
  try {
    const resp = await http.get('/doctor/home/statistics');
    const data = resp.data;
    
    if (data && typeof data === 'object') {
      return {
        pendingAppointments: data.pendingAppointments || data.pending_appointments || 0,
        todayTotalAppointments: data.todayTotalAppointments || data.today_total_appointments || 0,
        todayCompleted: data.todayCompleted || data.today_completed || 0,
        inClinicPatients: data.inClinicPatients || data.in_clinic_patients || 0,
        weekPrescriptions: data.weekPrescriptions || data.week_prescriptions || 0
      };
    }
    
    return {
      pendingAppointments: 0,
      todayTotalAppointments: 0,
      todayCompleted: 0,
      inClinicPatients: 0,
      weekPrescriptions: 0
    };
  } catch (e) {
    console.error('获取医生统计数据失败:', e);
    return {
      pendingAppointments: 0,
      todayTotalAppointments: 0,
      todayCompleted: 0,
      inClinicPatients: 0,
      weekPrescriptions: 0
    };
  }
}

/**
 * 获取今日排班列表
 */
export async function getTodaySchedule(limit: number = 10): Promise<TodayScheduleItem[]> {
  try {
    const resp = await http.get('/doctor/home/todaySchedule', {
      params: { limit }
    });
    const data = resp.data;
    
    if (data && Array.isArray(data)) {
      return data;
    }
    
    if (data && data.rows && Array.isArray(data.rows)) {
      return data.rows;
    }
    
    return [];
  } catch (e) {
    console.error('获取今日排班失败:', e);
    return [];
  }
}

/**
 * 获取工作统计
 */
export async function getWorkStatistics(): Promise<WorkStatistics> {
  try {
    const resp = await http.get('/doctor/home/workStatistics');
    const data = resp.data;
    
    if (data && typeof data === 'object') {
      return {
        weekConsultations: data.weekConsultations || data.week_consultations || 0,
        weekConsultationsTarget: data.weekConsultationsTarget || data.week_consultations_target || 50,
        patientSatisfaction: data.patientSatisfaction || data.patient_satisfaction || 0,
        monthPrescriptions: data.monthPrescriptions || data.month_prescriptions || 0
      };
    }
    
    return {
      weekConsultations: 0,
      weekConsultationsTarget: 50,
      patientSatisfaction: 0,
      monthPrescriptions: 0
    };
  } catch (e) {
    console.error('获取工作统计失败:', e);
    return {
      weekConsultations: 0,
      weekConsultationsTarget: 50,
      patientSatisfaction: 0,
      monthPrescriptions: 0
    };
  }
}