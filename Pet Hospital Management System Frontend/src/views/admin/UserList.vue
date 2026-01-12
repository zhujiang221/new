<template>
  <div class="user-list-admin">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- Search bar -->
    <div class="search-bar">
      <label>用户名：</label>
      <input type="text" v-model="searchName" placeholder="请输入用户名" @keyup.enter="search" />
      <label>权限类型：</label>
      <select v-model="searchRole" class="form-control search-select" @change="search">
        <option :value="null">全部</option>
        <option :value="3">普通用户</option>
        <option :value="1">管理员</option>
        <option :value="2">医生</option>
      </select>
      <button class="btn btn-primary" @click="search">查询</button>
      <button class="btn btn-primary" @click="openAddModal">添加用户</button>
      <button 
        :class="['btn btn-sm', selectedIds.length > 0 ? 'btn-batch-delete-active' : 'btn-batch-delete']" 
        @click="handleDelete(selectedIds)" 
        :disabled="selectedIds.length === 0">
        批量删除
      </button>
    </div>

    <!-- Data table -->
    <table class="data-table">
      <thead>
        <tr>
          <th width="40">
            <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
          </th>
          <th width="50">#</th>
          <th>用户ID</th>
          <th>用户名</th>
          <th>姓名</th>
          <th>权限</th>
          <th>注册时间</th>
          <th width="150">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="7" class="loading">加载中...</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="7" class="empty-state">暂无数据</td>
        </tr>
        <tr v-for="(item, index) in list" :key="item.id">
          <td data-label="">
            <input type="checkbox" :value="item.id" v-model="selectedIds" />
          </td>
          <td data-label="序号">{{ (pagination.page - 1) * pagination.limit + index + 1 }}</td>
          <td data-label="用户ID">{{ item.id }}</td>
          <td data-label="用户名">{{ item.username || '-' }}</td>
          <td data-label="姓名">{{ item.name || '-' }}</td>
          <td data-label="权限">
            <span class="role-badge" :class="getRoleClass(item.role)">
              {{ getRoleName(item.role) }}
            </span>
          </td>
          <td data-label="注册时间">{{ item.createTime || '-' }}</td>
          <td data-label="操作">
            <div class="btn-group">
              <button class="btn btn-warning btn-sm" @click="handleResetPassword(item)">重置密码</button>
              <button class="btn btn-danger btn-sm" @click="handleDelete([item.id])">删除</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Pagination -->
    <div class="pagination">
      
      <span>共 {{ pagination.total }} 条</span>
      <button :disabled="pagination.page <= 1" @click="changePage(pagination.page - 1)">上一页</button>
      <span>第 {{ pagination.page }} / {{ totalPages }} 页</span>
      <button :disabled="pagination.page >= totalPages" @click="changePage(pagination.page + 1)">下一页</button>
      <select v-model="pagination.limit" @change="search">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- Add User Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>添加用户</h3>
          <button class="modal-close" @click="showAddModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名：</label>
            <input type="text" v-model="userForm.username" class="form-control" placeholder="请输入用户名" />
          </div>
          <div class="form-group">
            <label>密码：</label>
            <input type="password" v-model="userForm.password" class="form-control" placeholder="请输入密码" />
          </div>
          <div class="form-group">
            <label>姓名：</label>
            <input type="text" v-model="userForm.name" class="form-control" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label>电话：</label>
            <input type="text" v-model="userForm.phone" class="form-control" placeholder="请输入电话" />
          </div>
          <div class="form-group">
            <label>地址：</label>
            <input type="text" v-model="userForm.address" class="form-control" placeholder="请输入地址" />
          </div>
          <div class="form-group">
            <label>权限：</label>
            <select v-model.number="userForm.role" class="form-control">
              <option :value="3">普通用户</option>
              <option :value="2">医生</option>
              <option :value="1">管理员</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitAdd">保存</button>
          <button class="btn" @click="showAddModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- Deleted Users Modal -->
    <div v-if="showDeletedModal" class="modal-overlay" @click.self="showDeletedModal = false">
      <div class="modal large-modal">
        <div class="modal-header">
          <h3>已删除用户列表</h3>
          <button class="modal-close" @click="showDeletedModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <table class="data-table">
            <thead>
              <tr>
                <th width="50">#</th>
                <th>用户名</th>
                <th>电话</th>
                <th>删除时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="deletedLoading">
                <td colspan="4" class="loading">加载中...</td>
              </tr>
              <tr v-else-if="deletedList.length === 0">
                <td colspan="4" class="empty-state">暂无数据</td>
              </tr>
              <tr v-for="(item, index) in deletedList" :key="item.id">
                <td>{{ index + 1 }}</td>
                <td>{{ item.username || '-' }}</td>
                <td>{{ item.phone }}</td>
                <td>{{ item.updateTime }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import http from '../../api/http';
import { showMessage, showConfirm } from '../../utils/message';

interface User {
  id: number;
  username: string;
  name: string;
  phone: string;
  address: string;
  createTime: string;
  updateTime?: string;
  role?: number; // 1=管理员, 2=医生, 3=用户
}

interface UserDetail extends User {
  email?: string;
  idCard?: string;
  age?: number;
  qualification?: string;
  hospitalName?: string;
  hospitalAddress?: string;
  department?: string;
  info?: string;
}

const list = ref<User[]>([]);
const loading = ref(false);
const searchName = ref('');
const searchRole = ref<number | null>(null);

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1);

const selectedIds = ref<number[]>([]);
const selectAll = ref(false);

const showAddModal = ref(false);
const showDeletedModal = ref(false);

const userForm = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  address: '',
  role: 3 // 默认用户角色：3=用户
});

const deletedList = ref<User[]>([]);
const deletedLoading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      limit: pagination.limit
    };
    // 如果用户名不为空，添加name参数
    if (searchName.value && searchName.value.trim()) {
      params.name = searchName.value.trim();
    }
    // 如果权限不为空，添加role参数
    if (searchRole.value !== null && searchRole.value !== undefined) {
      params.role = searchRole.value;
    }
    const resp = await http.get('/admin/getAllUserByLimit', {
      params: params
    });
    const data = resp.data;
    const users = data.rows || [];
    
    // 为每个用户获取角色信息
    for (const user of users) {
      if (!user.role) {
        // 尝试从user_role表获取角色，或者根据其他信息推断
        try {
          // 这里可能需要调用后端API获取用户角色
          // 暂时先尝试从用户的其他字段推断，或者保持为空
          // 如果后端返回的数据中已经包含role字段，则不需要额外处理
        } catch (e) {
          console.error('获取用户角色失败:', e);
        }
      }
    }
    
    list.value = users;
    pagination.total = data.total || 0;
    selectedIds.value = [];
    selectAll.value = false;
  } catch (e) {
    console.error('获取数据失败:', e);
  } finally {
    loading.value = false;
  }
}

function search() {
  pagination.page = 1;
  fetchData();
}

function changePage(page: number) {
  pagination.page = page;
  fetchData();
}

function toggleSelectAll() {
  if (selectAll.value) {
    selectedIds.value = list.value.map(item => item.id);
  } else {
    selectedIds.value = [];
  }
}

function openAddModal() {
  Object.assign(userForm, {
    username: '',
    password: '',
    name: '',
    phone: '',
    address: '',
    role: 3 // 默认用户角色：3=用户
  });
  showAddModal.value = true;
}


function getRoleName(role?: number): string {
  if (!role) return '普通用户';
  // 1=管理员, 2=医生, 3=用户
  if (role === 1) return '管理员';
  if (role === 2) return '医生';
  if (role === 3) return '普通用户';
  return '未知';
}

function getRoleClass(role?: number): string {
  if (!role) return 'role-user';
  // 1=管理员, 2=医生, 3=用户
  if (role === 1) return 'role-admin';
  if (role === 2) return 'role-doctor';
  if (role === 3) return 'role-user';
  return 'role-user';
}


async function submitAdd() {
  if (!userForm.username) {
    showMessage('请输入用户名', 'error');
    return;
  }
  if (!userForm.password) {
    showMessage('请输入密码', 'error');
    return;
  }
  if (!userForm.username) {
    showMessage('请输入用户名', 'error');
    return;
  }
  if (!userForm.name) {
    showMessage('请输入姓名', 'error');
    return;
  }
  if (!userForm.phone) {
    showMessage('请输入电话', 'error');
    return;
  }
  if (!userForm.role) {
    showMessage('请选择权限', 'error');
    return;
  }
  
  try {
    // 添加用户，包含角色信息
    // 确保role是数字类型，并且同时传递role和roleId（兼容后端）
    const roleNum = Number(userForm.role) || 3;
    
    // 构建请求参数，确保所有字段都包含
    const requestData = {
      username: userForm.username,
      password: userForm.password,
      name: userForm.name,  // 姓名
      phone: userForm.phone,
      address: userForm.address || '',
      role: roleNum,
      roleId: roleNum  // 同时传递roleId，兼容后端可能使用的字段名
    };
    
    console.log('添加用户 - 提交的数据:', requestData);
    console.log('添加用户 - role值:', roleNum, '类型:', typeof roleNum);
    
    const resp = await http.post('/admin/addUser', requestData);
    
    // 解析后端返回的数据
    const responseData = resp.data;
    const responseStr = typeof responseData === 'string' ? responseData : String(responseData);
    
    if (responseStr === 'SUCCESS') {
      showMessage('添加成功', 'success');
      showAddModal.value = false;
      fetchData();
    } else if (responseStr.startsWith('ERR:')) {
      // 提取错误信息（去掉ERR:前缀）
      const errorMsg = responseStr.substring(4);
      showMessage(errorMsg || '添加失败', 'error');
    } else {
      showMessage(responseStr || '添加失败', 'error');
    }
  } catch (e: any) {
    console.error('添加用户失败:', e);
    // 尝试从响应中提取错误信息
    let errorMsg = '操作失败';
    if (e.response?.data) {
      const data = e.response.data;
      if (typeof data === 'string') {
        if (data.startsWith('ERR:')) {
          errorMsg = data.substring(4);
        } else {
          errorMsg = data;
        }
      } else if (data.message) {
        errorMsg = data.message;
      }
    } else if (e.message) {
      errorMsg = e.message;
    }
    showMessage(errorMsg, 'error');
  }
}


async function handleResetPassword(item: User) {
  const confirmed = await showConfirm(`确认将用户"${item.username}"的密码重置为邮箱账号的前6位吗？\n重置后用户需要使用邮箱前6位作为密码登录。`);
  if (!confirmed) {
    return;
  }
  
  try {
    const resp = await http.post('/admin/resetPassword', null, {
      params: {
        userId: item.id
      }
    });
    
    const responseData = resp.data;
    const responseStr = typeof responseData === 'string' ? responseData : String(responseData);
    
    if (responseStr === 'SUCCESS') {
      showMessage(`密码重置成功！用户"${item.username}"的密码已重置为邮箱账号的前6位`, 'success');
    } else if (responseStr.startsWith('ERR:')) {
      const errorMsg = responseStr.substring(4);
      showMessage('重置密码失败: ' + errorMsg, 'error');
    } else {
      showMessage('重置密码失败: ' + responseStr, 'error');
    }
  } catch (e: any) {
    console.error('重置密码失败:', e);
    let errorMsg = '操作失败';
    if (e.response?.data) {
      const data = e.response.data;
      if (typeof data === 'string') {
        if (data.startsWith('ERR:')) {
          errorMsg = data.substring(4);
        } else {
          errorMsg = data;
        }
      } else if (data.message) {
        errorMsg = data.message;
      }
    } else if (e.message) {
      errorMsg = e.message;
    }
    showMessage('重置密码失败: ' + errorMsg, 'error');
  }
}

async function handleDelete(ids: number[]) {
  if (ids.length === 0) {
    showMessage('请选择要删除的用户', 'error');
    return;
  }
  const confirmed = await showConfirm(`确认删除选中的 ${ids.length} 个用户吗？`);
  if (!confirmed) return;
  
  try {
    const formData = new URLSearchParams();
    ids.forEach(id => formData.append('ids', String(id)));
    
    const resp = await http.post('/admin/delUser', formData, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    
    // 处理不同的响应格式
    const responseData = resp.data;
    const responseStr = typeof responseData === 'string' ? responseData : String(responseData);
    
    // 先检查是否包含成功相关的关键词（包括中文和英文）
    if (responseStr.includes('成功') || responseStr.includes('success') || 
        responseStr === 'SUCCESS' || responseStr.includes('SUCCESS')) {
      showMessage('删除成功', 'success');
      fetchData();
    } else if (responseStr === 'DontOP' || responseStr.includes('DontOP') || 
               responseStr.includes('不能删除') || responseStr.includes('当前登录')) {
      showMessage('不能删除当前登录用户', 'error');
    } else {
      showMessage('删除失败: ' + responseStr, 'error');
    }
  } catch (e: any) {
    console.error('删除用户失败:', e);
    let errorMsg = '未知错误';
    if (e.response?.data) {
      const data = e.response.data;
      if (typeof data === 'string') {
        if (data.startsWith('ERR:')) {
          errorMsg = data.substring(4);
        } else {
          errorMsg = data;
        }
      } else if (data.message) {
        errorMsg = data.message;
      }
    } else if (e.message) {
      errorMsg = e.message;
    }
    showMessage('操作失败: ' + errorMsg, 'error');
  }
}

async function viewDeletedUsers() {
  showDeletedModal.value = true;
  deletedLoading.value = true;
  try {
    const resp = await http.get('/admin/getAllDelUserByLimit', {
      params: { 
        name: '', // 可以添加搜索参数
        page: 1, 
        limit: 100 
      }
    });
    const data = resp.data;
    if (data && data.rows) {
      deletedList.value = data.rows;
    } else if (Array.isArray(data)) {
      deletedList.value = data;
    } else {
      deletedList.value = [];
    }
  } catch (e: any) {
    console.error('获取已删除用户失败:', e);
    let errorMsg = '未知错误';
    if (e.response?.data) {
      const data = e.response.data;
      if (typeof data === 'string') {
        if (data.startsWith('ERR:')) {
          errorMsg = data.substring(4);
        } else {
          errorMsg = data;
        }
      } else if (data.message) {
        errorMsg = data.message;
      }
    } else if (e.message) {
      errorMsg = e.message;
    }
    showMessage('获取已删除用户失败: ' + errorMsg, 'error');
    deletedList.value = [];
  } finally {
    deletedLoading.value = false;
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.user-list-admin {
  padding: 0;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 8px;
  flex-wrap: wrap;
}

.search-bar label {
  font-weight: 500;
  color: #333;
  white-space: nowrap;
}

.search-bar input[type="text"] {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.search-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
  width: 120px;
  cursor: pointer;
  height: 32px;
}

.search-select:focus {
  outline: none;
  border-color: #01AAED;
}

.search-bar .btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  white-space: nowrap;
}

.search-bar .btn-primary {
  background-color: #01AAED;
  color: white;
}

.search-bar .btn-primary:hover {
  background-color: #0099d6;
}

/* 移动端搜索栏优化 */
@media (max-width: 767px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar label {
    margin-bottom: 5px;
  }
  
  .search-bar input[type="text"],
  .search-select {
    width: 100%;
    min-width: auto;
  }
  
  .search-bar .btn {
    width: 100%;
    margin-top: 5px;
  }
}

.large-modal {
  min-width: 700px;
  max-height: 80vh;
  overflow-y: auto;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .large-modal {
    min-width: auto;
    width: 95vw;
    max-width: 95vw;
  }
  
  .data-table {
    display: block;
  }
  
  .data-table thead {
    display: none;
  }
  
  .data-table tbody {
    display: block;
  }
  
  .data-table tr {
    display: block;
    margin-bottom: 15px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .data-table td {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border: none;
    border-bottom: 1px solid #f0f0f0;
    text-align: left !important;
  }
  
  .data-table td:first-child {
    justify-content: flex-start;
    gap: 10px;
  }
  
  .data-table td:last-child {
    border-bottom: none;
    padding-top: 10px;
  }
  
  .data-table td::before {
    content: attr(data-label);
    font-weight: bold;
    color: #666;
    margin-right: 10px;
    flex-shrink: 0;
    min-width: 80px;
  }
  
  .data-table td[data-label=""]::before {
    display: none;
  }
  
  .data-table td[data-label="操作"] {
    flex-direction: column;
    gap: 8px;
  }
  
  .data-table td[data-label="操作"]::before {
    display: none;
  }
  
  .btn-group {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }
  
  .btn-group .btn {
    width: 100%;
    margin: 0;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 5px;
  }
  
  .pagination button {
    flex: 1;
    min-width: auto;
  }
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
  border: none;
}

.btn-warning:hover {
  background-color: #e0a800;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
  border: none;
}

.btn-info:hover {
  background-color: #138496;
}

/* 批量删除按钮样式 */
.btn-batch-delete {
  background-color: #72C1BB;
  color: white;
  border: none;
}

.btn-batch-delete:hover:not(:disabled) {
  background-color: #5aa9a3;
}

.btn-batch-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-batch-delete-active {
  background-color: #007bff;
  color: white;
  border: none;
}

.btn-batch-delete-active:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-batch-delete-active:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 角色标签样式 */
.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.role-user {
  background-color: #e3f2fd;
  color: #1976d2;
}

.role-doctor {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.role-admin {
  background-color: #fff3e0;
  color: #e65100;
}

/* 详情模态框样式 */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e0e0;
  color: #333;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  font-size: 13px;
}

.detail-item span {
  color: #333;
  font-size: 14px;
  word-break: break-word;
}

/* 移动端详情样式 */
@media (max-width: 767px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .detail-section h4 {
    font-size: 14px;
  }
  
  .detail-item label {
    font-size: 12px;
  }
  
  .detail-item span {
    font-size: 13px;
  }
  
  .role-badge {
    font-size: 11px;
    padding: 3px 10px;
  }
}

</style>

