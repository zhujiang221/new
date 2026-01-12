import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { getUserInfo, ROLE_ADMIN, ROLE_DOCTOR, ROLE_USER, roleToString } from '../utils/user';

// Static import components
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import ForgetPasswordPage from '../views/ForgetPasswordPage.vue';
import MainLayout from '../views/MainLayout.vue';
import UserLayout from '../views/user/UserLayout.vue';
import DoctorLayout from '../views/doctor/DoctorLayout.vue';

// User components
import PetList from '../views/user/PetList.vue';
import ApplyList from '../views/user/ApplyList.vue';
import ApplyFlow from '../views/user/ApplyFlow.vue';
import StandardList from '../views/user/StandardList.vue';
import PetDailyList from '../views/user/PetDailyList.vue';
import NoticeList from '../views/user/NoticeList.vue';
import DiagnosisList from '../views/user/DiagnosisList.vue';
import TjApply from '../views/user/TjApply.vue';
import TjDaily from '../views/user/TjDaily.vue';
import Assess from '../views/user/Assess.vue';
import UserProfile from '../views/user/UserProfile.vue';
import ChangePassword from '../views/user/ChangePassword.vue';
import UserMore from '../views/user/UserMore.vue';
import UserMessage from '../views/user/UserMessage.vue';
import UserFreeTime from '../views/user/FreeTime.vue';

// Doctor components
import ApplyListDoctor from '../views/doctor/ApplyListDoctor.vue';
import DiagnosisListDoctor from '../views/doctor/DiagnosisListDoctor.vue';
import PetDailyListDoctor from '../views/doctor/PetDailyListDoctor.vue';
import NoticeDoctorList from '../views/doctor/NoticeDoctorList.vue';
import StandardDoctorList from '../views/doctor/StandardDoctorList.vue';
import TjApplyDoctor from '../views/doctor/TjApplyDoctor.vue';
import TjDailyDoctor from '../views/doctor/TjDailyDoctor.vue';
import FreeTime from '../views/doctor/FreeTime.vue';
import MedicineList from '../views/doctor/MedicineList.vue';
import MedicineRecordList from '../views/doctor/MedicineRecordList.vue';
import PrescribeMedicine from '../views/doctor/PrescribeMedicine.vue';
import ScheduleManage from '../views/doctor/ScheduleManage.vue';
import ServiceTypeManage from '../views/doctor/ServiceTypeManage.vue';
import DoctorMore from '../views/doctor/DoctorMore.vue';
import DoctorMessage from '../views/doctor/DoctorMessage.vue';

// Admin components
import UserList from '../views/admin/UserList.vue';
import AppointmentTypeManage from '../views/admin/AppointmentTypeManage.vue';
import AdminPetList from '../views/admin/PetList.vue';
import AdminDiagnosisList from '../views/admin/DiagnosisList.vue';
import AdminApplyList from '../views/admin/ApplyList.vue';
import AdminFreeTime from '../views/admin/FreeTime.vue';
import AdminScheduleManage from '../views/admin/ScheduleManage.vue';
import AdminServiceTypeManage from '../views/admin/ServiceTypeManage.vue';
import AdminNoticeList from '../views/admin/NoticeList.vue';
import AdminAssess from '../views/admin/Assess.vue';
import AdminStandardList from '../views/admin/StandardList.vue';
import AdminPetDailyList from '../views/admin/PetDailyList.vue';
import AdminTjApply from '../views/admin/TjApply.vue';
import AdminTjDaily from '../views/admin/TjDaily.vue';
import AdminMedicineList from '../views/admin/MedicineList.vue';
import AdminMedicineRecordList from '../views/admin/MedicineRecordList.vue';

// Error pages
import NotFound from '../views/NotFound.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'login',
    component: LoginPage
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterPage
  },
  {
    path: '/forget-password',
    name: 'forget-password',
    component: ForgetPasswordPage
  },
  // ========== 用户端路由 ==========
  {
    path: '/user',
    component: UserLayout,
    // 移除 redirect，让移动端显示主页面组件，电脑端通过 Layout 组件处理默认显示
    children: [
      {
        path: 'pets',
        name: 'user-pet-list',
        component: PetList,
        meta: { title: '宠物管理', role: 'user' }
      },
      {
        path: 'apply',
        name: 'user-apply-list',
        component: ApplyList, // 用户专用预约页面，只显示当前用户的预约
        meta: { title: '我的预约', role: 'user' }
      },
      {
        path: 'apply-flow',
        name: 'user-apply-flow',
        component: ApplyFlow,
        meta: { title: '预约就诊', role: 'user' }
      },
      {
        path: 'standards',
        name: 'user-standard-list',
        component: StandardList,
        meta: { title: '健康标准', role: 'user' }
      },
      {
        path: 'pet-daily',
        name: 'user-pet-daily',
        component: PetDailyList,
        meta: { title: '宠物日志', role: 'user' }
      },
      {
        path: 'notices',
        name: 'user-notice-list',
        component: NoticeList,
        meta: { title: '健康指南', role: 'user' }
      },
      {
        path: 'diagnosis',
        name: 'user-diagnosis-list',
        component: DiagnosisList,
        meta: { title: '诊断记录', role: 'user' }
      },
      {
        path: 'tj-apply',
        name: 'user-tj-apply',
        component: TjApply,
        meta: { title: '预约统计', role: 'user' }
      },
      {
        path: 'tj-daily',
        name: 'user-tj-daily',
        component: TjDaily,
        meta: { title: '日志统计', role: 'user' }
      },
      {
        path: 'assess',
        name: 'user-assess',
        component: Assess,
        meta: { title: '健康评估', role: 'user' }
      },
      {
        path: 'free-time',
        name: 'user-free-time',
        component: UserFreeTime,
        meta: { title: '医生时间', role: 'user' }
      },
      {
        path: 'profile',
        name: 'user-profile',
        component: UserProfile,
        meta: { title: '个人信息', role: 'user' }
      },
      {
        path: 'change-password',
        name: 'user-change-password',
        component: ChangePassword,
        meta: { title: '修改密码', role: 'user' }
      },
      {
        path: 'more',
        name: 'user-more',
        component: UserMore,
        meta: { title: '更多功能', role: 'user' }
      }
    ]
  },
  // ========== 医生端路由 ==========
  {
    path: '/doctor',
    component: DoctorLayout,
    // 移除 redirect，让移动端显示主页面组件，电脑端通过 Layout 组件处理默认显示
    children: [
      {
        path: 'apply',
        name: 'doctor-apply-list',
        component: ApplyListDoctor,
        meta: { title: '预约管理', role: 'doctor' }
      },
      {
        path: 'diagnosis',
        name: 'doctor-diagnosis-list',
        component: DiagnosisListDoctor,
        meta: { title: '宠物健康史', role: 'doctor' }
      },
      {
        path: 'pet-daily',
        name: 'doctor-pet-daily',
        component: PetDailyListDoctor,
        meta: { title: '宠物日志', role: 'doctor' }
      },
      {
        path: 'notices',
        name: 'doctor-notice-list',
        component: NoticeDoctorList,
        meta: { title: '健康指南管理', role: 'doctor' }
      },
      {
        path: 'standards',
        name: 'doctor-standard-list',
        component: StandardDoctorList,
        meta: { title: '健康标准管理', role: 'doctor' }
      },
      {
        path: 'tj-apply',
        name: 'doctor-tj-apply',
        component: TjApplyDoctor,
        meta: { title: '预约统计', role: 'doctor' }
      },
      {
        path: 'tj-daily',
        name: 'doctor-tj-daily',
        component: TjDailyDoctor,
        meta: { title: '日志统计', role: 'doctor' }
      },
      {
        path: 'free-time',
        name: 'doctor-free-time',
        component: FreeTime,
        meta: { title: '时间管理', role: 'doctor' }
      },
      {
        path: 'medicine',
        name: 'doctor-medicine',
        component: MedicineList,
        meta: { title: '药品管理', role: 'doctor|admin' }
      },
      {
        path: 'medicine-record',
        name: 'doctor-medicine-record',
        component: MedicineRecordList,
        meta: { title: '开药记录', role: 'doctor|admin' }
      },
      {
        path: 'prescribe-medicine',
        name: 'doctor-prescribe-medicine',
        component: PrescribeMedicine,
        meta: { title: '开具药品', role: 'doctor' }
      },
      {
        path: 'schedule',
        name: 'doctor-schedule',
        component: ScheduleManage,
        meta: { title: '排班管理', role: 'doctor|admin' }
      },
      {
        path: 'more',
        name: 'doctor-more',
        component: DoctorMore,
        meta: { title: '更多功能', role: 'doctor' }
      },
      {
        path: 'service-type',
        name: 'doctor-service-type',
        component: ServiceTypeManage,
        meta: { title: '服务类型管理', role: 'doctor|admin' }
      },
      {
        path: 'message',
        name: 'doctor-message',
        component: DoctorMessage,
        meta: { title: '消息中心', role: 'doctor' }
      }
    ]
  },
  {
    path: '/home',
    name: 'home',
    component: MainLayout,
    redirect: (to) => {
      // 根据用户角色重定向到不同的首页
      const userInfo = getUserInfo();
      const role = userInfo?.role || ROLE_USER;
      if (role === ROLE_ADMIN) {
        return '/admin/users';
      } else if (role === ROLE_DOCTOR) {
        return '/doctor'; // 重定向到医生主页面
      } else {
        return '/user'; // 重定向到用户主页面
      }
    },
    children: [
      // ========== 管理员路由 ==========
      {
        path: '/admin/users',
        name: 'admin-user-list',
        component: UserList,
        meta: { title: '用户管理', role: 'admin' }
      },
      {
        path: '/admin/appointment-type',
        name: 'admin-appointment-type',
        component: AppointmentTypeManage,
        meta: { title: '预约类型管理', role: 'admin' }
      },
      {
        path: '/admin/pets',
        name: 'admin-pet-list',
        component: AdminPetList,
        meta: { title: '宠物管理', role: 'admin' }
      },
      {
        path: '/admin/diagnosis',
        name: 'admin-diagnosis-list',
        component: AdminDiagnosisList,
        meta: { title: '诊断记录', role: 'admin' }
      },
      {
        path: '/admin/apply',
        name: 'admin-apply-list',
        component: AdminApplyList,
        meta: { title: '预约管理', role: 'admin' }
      },
      {
        path: '/admin/free-time',
        name: 'admin-free-time',
        component: AdminFreeTime,
        meta: { title: '医生空闲时间', role: 'admin' }
      },
      {
        path: '/admin/schedule',
        name: 'admin-schedule',
        component: AdminScheduleManage,
        meta: { title: '排班管理', role: 'admin' }
      },
      {
        path: '/admin/service-type',
        name: 'admin-service-type',
        component: AdminServiceTypeManage,
        meta: { title: '服务类型管理', role: 'admin' }
      },
      {
        path: '/admin/notices',
        name: 'admin-notice-list',
        component: AdminNoticeList,
        meta: { title: '健康指南管理', role: 'admin' }
      },
      {
        path: '/admin/assess',
        name: 'admin-assess',
        component: AdminAssess,
        meta: { title: '健康评估', role: 'admin' }
      },
      {
        path: '/admin/standards',
        name: 'admin-standard-list',
        component: AdminStandardList,
        meta: { title: '健康标准管理', role: 'admin' }
      },
      {
        path: '/admin/pet-daily',
        name: 'admin-pet-daily',
        component: AdminPetDailyList,
        meta: { title: '宠物日志管理', role: 'admin' }
      },
      {
        path: '/admin/tj-apply',
        name: 'admin-tj-apply',
        component: AdminTjApply,
        meta: { title: '预约统计', role: 'admin' }
      },
      {
        path: '/admin/tj-daily',
        name: 'admin-tj-daily',
        component: AdminTjDaily,
        meta: { title: '日志统计', role: 'admin' }
      },
      {
        path: '/admin/medicine',
        name: 'admin-medicine',
        component: AdminMedicineList,
        meta: { title: '药品管理', role: 'admin' }
      },
      {
        path: '/admin/medicine-record',
        name: 'admin-medicine-record',
        component: AdminMedicineRecordList,
        meta: { title: '开药记录管理', role: 'admin' }
      }
    ]
  },
  // 404 catch-all route
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFound
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

  // 路由守卫：根据用户角色进行访问控制
router.beforeEach((to, from, next) => {
  // 登录、注册和找回密码页面不需要角色检查
  if (to.path === '/' || to.path === '/register' || to.path === '/forget-password') {
    next();
    return;
  }
  
  // 获取用户信息（根据路由路径匹配角色）
  const routeRole = to.meta.role as string | undefined;
  let expectedRole: number | undefined;
  if (routeRole) {
    if (routeRole === 'admin' || routeRole.includes('admin')) {
      expectedRole = ROLE_ADMIN;
    } else if (routeRole === 'doctor' || routeRole.includes('doctor')) {
      expectedRole = ROLE_DOCTOR;
    } else {
      expectedRole = ROLE_USER;
    }
  } else {
    // 根据路径推断角色
    if (to.path.startsWith('/admin')) {
      expectedRole = ROLE_ADMIN;
    } else if (to.path.startsWith('/doctor')) {
      expectedRole = ROLE_DOCTOR;
    } else {
      expectedRole = ROLE_USER;
    }
  }
  const userInfo = getUserInfo(expectedRole);
  
  // 如果没有用户信息，重定向到登录页
  if (!userInfo) {
    next('/');
    return;
  }
  
  const userRole = userInfo.role || ROLE_USER;
  
  // 如果路由需要特定角色，检查权限
  if (routeRole) {
    // 将数字角色转换为字符串用于匹配
    const userRoleStr = roleToString(userRole);
    
    // 管理员可以访问所有路由
    if (userRole === ROLE_ADMIN) {
      next();
      return;
    }
    
    // 支持多角色（用|分隔），例如 'doctor|admin'
    const allowedRoles = routeRole.split('|');
    if (allowedRoles.includes(userRoleStr)) {
      next();
      return;
    }
    
    // 检查角色是否匹配（兼容单角色格式）
    if (userRoleStr !== routeRole) {
      // 角色不匹配，重定向到对应角色的首页
      if (userRole === ROLE_DOCTOR) {
        next('/doctor');
      } else if (userRole === ROLE_ADMIN) {
        next('/admin/users');
      } else {
        next('/user');
      }
      return;
    }
  }
  
  next();
});

export default router;
