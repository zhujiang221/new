/**
 * 用户信息管理工具
 * 用于存储和获取当前登录用户的信息，包括角色
 * role: 1=管理员, 2=医生, 3=用户
 */

export interface UserInfo {
  id: string;
  username: string;
  name: string;
  role: number; // 1=管理员, 2=医生, 3=用户
  phone?: string;
  address?: string;
  email?: string;
}

// 角色常量
export const ROLE_ADMIN = 1;
export const ROLE_DOCTOR = 2;
export const ROLE_USER = 3;

/**
 * 角色数字转字符串（用于兼容旧代码）
 */
export function roleToString(role: number): string {
  if (role === ROLE_ADMIN) return 'admin';
  if (role === ROLE_DOCTOR) return 'doctor';
  return 'user';
}

/**
 * 角色字符串转数字
 */
export function roleToNumber(role: string): number {
  if (role === 'admin') return ROLE_ADMIN;
  if (role === 'doctor') return ROLE_DOCTOR;
  return ROLE_USER;
}

// 获取基于主机名和用户ID+角色的唯一key，确保不同设备/用户/角色不会共享用户信息
function getUserInfoKey(userId?: string, role?: number): string {
  const hostname = window.location.hostname;
  // 如果提供了用户ID和角色，使用它们作为key的一部分，支持同一台电脑同时登录不同账号
  if (userId && role !== undefined) {
    return `phms_user_info_${hostname}_${userId}_${role}`;
  }
  // 如果没有提供，使用默认key（用于向后兼容）
  return `phms_user_info_${hostname}`;
}

// 获取所有已登录的用户信息key（用于支持多账号）
function getAllUserInfoKeys(): string[] {
  const hostname = window.location.hostname;
  const prefix = `phms_user_info_${hostname}_`;
  const keys: string[] = [];
  
  try {
    // 遍历localStorage，找到所有匹配的key
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith(prefix)) {
        keys.push(key);
      }
    }
  } catch (e) {
    console.error('获取所有用户信息key失败:', e);
  }
  
  return keys;
}

/**
 * 保存用户信息到localStorage（更持久，不会因为标签页关闭而丢失）
 * 使用主机名+用户ID+角色作为key的一部分，支持同一台电脑同时登录不同账号
 */
export function saveUserInfo(userInfo: UserInfo) {
  if (!userInfo.id || userInfo.role === undefined) {
    console.error('保存用户信息失败：用户ID或角色为空');
    return;
  }
  
  const USER_INFO_KEY = getUserInfoKey(userInfo.id, userInfo.role);
  try {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    // 同时保存到sessionStorage作为备份
    sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    console.log('保存用户信息成功，主机名:', window.location.hostname, '用户ID:', userInfo.id, '角色:', userInfo.role, 'key:', USER_INFO_KEY);
    
    // 保存当前活跃的用户信息key（用于快速查找）
    const hostname = window.location.hostname;
    const currentKey = `phms_current_user_${hostname}`;
    localStorage.setItem(currentKey, USER_INFO_KEY);
  } catch (e) {
    console.error('保存用户信息失败:', e);
    // 如果localStorage失败，尝试使用sessionStorage
    try {
      sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    } catch (e2) {
      console.error('保存用户信息到sessionStorage也失败:', e2);
    }
  }
}

/**
 * 从localStorage或sessionStorage获取用户信息
 * 优先根据当前路由路径匹配角色，如果找不到则返回最近保存的用户信息
 */
export function getUserInfo(expectedRole?: number): UserInfo | null {
  const hostname = window.location.hostname;
  
  // 如果指定了期望的角色，优先查找匹配该角色的用户信息
  if (expectedRole !== undefined) {
    const keys = getAllUserInfoKeys();
    for (const key of keys) {
      try {
        const data = localStorage.getItem(key) || sessionStorage.getItem(key);
        if (data) {
          const parsed = JSON.parse(data);
          if (parsed && parsed.role === expectedRole) {
            // 确保role是数字类型
            const role = normalizeRole(parsed.role);
            parsed.role = role;
            console.log('找到匹配角色的用户信息:', parsed.id, '角色:', role);
            return parsed;
          }
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
  }
  
  // 如果没有指定期望角色，尝试获取当前活跃的用户信息
  const currentKey = `phms_current_user_${hostname}`;
  const currentUserKey = localStorage.getItem(currentKey);
  if (currentUserKey) {
    try {
      const data = localStorage.getItem(currentUserKey) || sessionStorage.getItem(currentUserKey);
      if (data) {
        const parsed = JSON.parse(data);
        const role = normalizeRole(parsed.role);
        parsed.role = role;
        return parsed;
      }
    } catch (e) {
      console.error('解析当前用户信息失败:', e);
    }
  }
  
  // 向后兼容：尝试使用旧的key格式
  const oldKey = getUserInfoKey();
  let data = localStorage.getItem(oldKey) || sessionStorage.getItem(oldKey);
  if (data) {
    try {
      const parsed = JSON.parse(data);
      const role = normalizeRole(parsed.role);
      parsed.role = role;
      return parsed;
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  
  return null;
}

/**
 * 规范化角色值（确保是数字类型）
 */
function normalizeRole(role: any): number {
  if (role === undefined || role === null) {
    return ROLE_USER;
  }
  
  // 如果是字符串，转换为数字
  if (typeof role === 'string') {
    if (role === 'admin' || role === '1') {
      return ROLE_ADMIN;
    } else if (role === 'doctor' || role === '2') {
      return ROLE_DOCTOR;
    } else if (role === 'user' || role === '3') {
      return ROLE_USER;
    } else {
      return parseInt(role) || ROLE_USER;
    }
  }
  
  // 确保是数字类型
  const roleNum = Number(role);
  if (isNaN(roleNum) || (roleNum !== ROLE_ADMIN && roleNum !== ROLE_DOCTOR && roleNum !== ROLE_USER)) {
    console.warn('存储中的角色值无效:', role, '，使用默认值3（用户）');
    return ROLE_USER;
  }
  
  return roleNum;
}

/**
 * 清除用户信息
 * 如果提供了用户ID和角色，只清除该用户的信息；否则清除所有用户信息
 */
export function clearUserInfo(userId?: string, role?: number) {
  const hostname = window.location.hostname;
  
  if (userId && role !== undefined) {
    // 清除指定用户的信息
    const USER_INFO_KEY = getUserInfoKey(userId, role);
    localStorage.removeItem(USER_INFO_KEY);
    sessionStorage.removeItem(USER_INFO_KEY);
    console.log('清除用户信息，主机名:', hostname, '用户ID:', userId, '角色:', role);
  } else {
    // 清除所有用户信息（用于退出登录）
    const keys = getAllUserInfoKeys();
    keys.forEach(key => {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    });
    
    // 清除当前活跃用户key
    const currentKey = `phms_current_user_${hostname}`;
    localStorage.removeItem(currentKey);
    
    // 向后兼容：清除旧的key格式
    const oldKey = getUserInfoKey();
    localStorage.removeItem(oldKey);
    sessionStorage.removeItem(oldKey);
    
    const oldKey2 = 'phms_user_info';
    localStorage.removeItem(oldKey2);
    sessionStorage.removeItem(oldKey2);
    
    console.log('清除所有用户信息，主机名:', hostname);
  }
}

/**
 * 获取所有已登录的用户信息列表（用于支持多账号切换）
 */
export function getAllLoggedInUsers(): UserInfo[] {
  const keys = getAllUserInfoKeys();
  const users: UserInfo[] = [];
  
  keys.forEach(key => {
    try {
      const data = localStorage.getItem(key) || sessionStorage.getItem(key);
      if (data) {
        const parsed = JSON.parse(data);
        if (parsed && parsed.id) {
          parsed.role = normalizeRole(parsed.role);
          users.push(parsed);
        }
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  });
  
  return users;
}

/**
 * 获取用户角色（数字）
 */
export function getUserRole(): number {
  const userInfo = getUserInfo();
  return userInfo?.role || ROLE_USER;
}

/**
 * 检查用户是否有指定角色（数字）
 */
export function hasRole(role: number): boolean {
  const userRole = getUserRole();
  return userRole === role;
}

/**
 * 检查用户是否是管理员
 */
export function isAdmin(): boolean {
  return hasRole(ROLE_ADMIN);
}

/**
 * 检查用户是否是医生
 */
export function isDoctor(): boolean {
  return hasRole(ROLE_DOCTOR);
}

/**
 * 检查用户是否是普通用户
 */
export function isUser(): boolean {
  return hasRole(ROLE_USER);
}

/**
 * 生成UUID（用于设备ID）
 */
function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

/**
 * 获取或生成设备ID
 * 用于标识不同的设备，帮助区分不同设备的session
 */
export function getDeviceId(): string {
  const DEVICE_ID_KEY = 'phms_device_id';
  try {
    let deviceId = localStorage.getItem(DEVICE_ID_KEY);
    if (!deviceId) {
      deviceId = generateUUID();
      localStorage.setItem(DEVICE_ID_KEY, deviceId);
      console.log('生成新的设备ID:', deviceId);
    }
    return deviceId;
  } catch (e) {
    console.error('获取设备ID失败:', e);
    // 如果localStorage失败，生成一个临时ID（不会持久化）
    return generateUUID();
  }
}

/**
 * 清除设备ID（通常不需要，除非用户想要重置）
 */
export function clearDeviceId() {
  const DEVICE_ID_KEY = 'phms_device_id';
  localStorage.removeItem(DEVICE_ID_KEY);
  console.log('清除设备ID');
}
