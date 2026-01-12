/**
 * 用户相关API
 */
import http from './http';
import type { UserInfo } from '../utils/user';
import { ROLE_USER } from '../utils/user';

/**
 * 获取当前登录用户信息
 */
export async function getCurrentUserInfo(): Promise<UserInfo | null> {
  try {
    const resp = await http.get('/user/getCurrentUser');
    const data = resp.data;
    
    // 检查响应格式：可能是 { code, message, data } 或直接是数据
    let userData = data;
    if (data && data.data) {
      // ResultMap格式：{ code: 200, message: 'success', data: {...} }
      userData = data.data;
    }
    
    if (userData) {
      // 直接使用user表中的role字段（数字类型）
      // 1=管理员, 2=医生, 3=用户
      // 后端可能返回 roleId 或 role 字段，优先使用 role，如果没有则使用 roleId
      let role = userData.role;
      
      // 如果role不存在，尝试使用roleId
      if (role === undefined || role === null) {
        role = userData.roleId;
      }
      
      // 如果roleId也不存在，尝试其他可能的字段名
      if (role === undefined || role === null) {
        role = userData.role_id;
      }
      
      // 如果role是字符串，转换为数字
      if (typeof role === 'string') {
        if (role === 'admin' || role === '1') {
          role = 1;
        } else if (role === 'doctor' || role === '2') {
          role = 2;
        } else if (role === 'user' || role === '3') {
          role = 3;
        } else {
          role = parseInt(role) || ROLE_USER;
        }
      }
      
      // 确保role是数字类型
      role = Number(role);
      
      // 验证role值是否有效
      if (isNaN(role) || (role !== 1 && role !== 2 && role !== 3)) {
        console.warn('无效的角色值:', userData.role || userData.roleId, '，使用默认值3（用户）');
        console.warn('用户数据:', userData);
        role = ROLE_USER;
      }
      
      console.log('获取用户信息 - 原始数据:', userData);
      console.log('获取用户信息 - role字段:', userData.role, 'roleId字段:', userData.roleId);
      console.log('获取用户信息 - 最终角色值:', role, '类型:', typeof role);
      
      return {
        id: String(userData.id || userData.userId || ''),
        username: userData.username || userData.phone || '',
        name: userData.name || '',
        role: role, // 确保是数字类型
        phone: userData.phone || '',
        address: userData.address || ''
      };
    }
    return null;
  } catch (e) {
    console.error('获取用户信息失败:', e);
    return null;
  }
}

