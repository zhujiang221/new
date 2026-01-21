<template>
  <div class="api-log-list">
    <div class="page-header">
      <h2>API日志管理</h2>
      <p class="page-description">查看系统所有API调用记录，了解用户操作行为</p>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-grid">
        <div class="search-item">
          <label>用户ID：</label>
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            class="search-input"
          />
        </div>
        <div class="search-item">
          <label>请求URL：</label>
          <el-input
            v-model="searchForm.requestUrl"
            placeholder="请输入URL关键词"
            clearable
            class="search-input"
          />
        </div>
        <div class="search-item">
          <label>请求方法：</label>
          <el-select v-model="searchForm.requestMethod" clearable placeholder="全部" class="search-select">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </div>
        <div class="search-item">
          <label>IP地址：</label>
          <el-input
            v-model="searchForm.ipAddress"
            placeholder="请输入IP地址"
            clearable
            class="search-input"
          />
        </div>
        <div class="search-item">
          <label>状态：</label>
          <el-select v-model="searchForm.status" clearable placeholder="全部" class="search-select">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </div>
        <div class="search-item search-item-time">
          <label>开始时间：</label>
          <el-date-picker
            v-model="searchForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="search-date-picker"
          />
        </div>
        <div class="search-item search-item-time">
          <label>结束时间：</label>
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="search-date-picker"
          />
        </div>
        <div class="search-item search-item-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :default-sort="{ prop: 'createTime', order: 'descending' }"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="userName" label="用户名" width="120">
          <template #default="scope">
            {{ scope.row.userName || (scope.row.userId ? `用户${scope.row.userId}` : '未登录') }}
          </template>
        </el-table-column>
        <el-table-column prop="userRealName" label="真实姓名" width="120">
          <template #default="scope">
            {{ scope.row.userRealName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="80" align="center">
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.requestMethod)" size="small">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="responseStatus" label="状态码" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.responseStatus)" size="small">
              {{ scope.row.responseStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="调用状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="100" align="center">
          <template #default="scope">
            {{ scope.row.executeTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="modern-pagination">
      <span class="modern-pagination-info">共 {{ pagination.total }} 条</span>
      <button 
        :disabled="pagination.page <= 1" 
        @click="handlePageChange(pagination.page - 1)"
        class="pagination-btn"
      >
        上一页
      </button>
      <span class="modern-pagination-info">第 {{ pagination.page }} / {{ totalPages }} 页</span>
      <button 
        :disabled="pagination.page >= totalPages" 
        @click="handlePageChange(pagination.page + 1)"
        class="pagination-btn"
      >
        下一页
      </button>
      <select 
        v-model="pagination.limit" 
        @change="handleSizeChange(pagination.limit)" 
        class="modern-input pagination-select"
      >
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
        <option :value="100">100条/页</option>
      </select>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="API日志详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentLog.userId || '未登录' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentLog.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentLog.userRealName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodType(currentLog.requestMethod)" size="small">
              {{ currentLog.requestMethod }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="2">
            {{ currentLog.requestUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="User-Agent" :span="2">
            {{ currentLog.userAgent || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="响应状态码">
            <el-tag :type="getStatusType(currentLog.responseStatus)" size="small">
              {{ currentLog.responseStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调用状态">
            <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentLog.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行时间">{{ currentLog.executeTime }}ms</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentLog.createTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="detail-section">
          <h4>请求参数</h4>
          <el-input
            v-model="currentLog.requestParams"
            type="textarea"
            :rows="4"
            readonly
            placeholder="无"
          />
        </div>

        <div class="detail-section">
          <h4>请求体</h4>
          <el-input
            v-model="currentLog.requestBody"
            type="textarea"
            :rows="6"
            readonly
            placeholder="无"
          />
        </div>

        <div class="detail-section">
          <h4>响应体</h4>
          <el-input
            v-model="currentLog.responseBody"
            type="textarea"
            :rows="8"
            readonly
            placeholder="无"
          />
        </div>

        <div v-if="currentLog.errorMessage" class="detail-section">
          <h4>错误信息</h4>
          <el-alert
            :title="currentLog.errorMessage"
            type="error"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { getToken } from '@/utils/token'

// 搜索表单
const searchForm = reactive({
  userId: null as number | null,
  requestUrl: '',
  requestMethod: '',
  ipAddress: '',
  status: null as number | null,
  startTime: '',
  endTime: ''
})

// 数据列表
const list = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  limit: 20,
  total: 0
})

// 计算总页数
const totalPages = computed(() => Math.ceil(pagination.total / pagination.limit) || 1)

// 详情对话框
const showDetailDialog = ref(false)
const currentLog = ref<any>(null)

// 获取日志列表
const fetchList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      limit: pagination.limit
    }

    // 添加搜索条件
    if (searchForm.userId) {
      params.userId = searchForm.userId
    }
    if (searchForm.requestUrl) {
      params.requestUrl = searchForm.requestUrl
    }
    if (searchForm.requestMethod) {
      params.requestMethod = searchForm.requestMethod
    }
    if (searchForm.ipAddress) {
      params.ipAddress = searchForm.ipAddress
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }

    const response = await http.get('/admin/apiLog/getAllByLimit', { params })
    
    // 检查响应结果
    if (response.data.result === 'success') {
      const data = response.data.data
      list.value = data.rows || []
      pagination.total = data.total || 0
    } else if (response.data.result === 'fail') {
      // 如果返回 fail，检查是否有数据（某些情况下后端可能返回数据但标记为 fail）
      if (response.data.data && response.data.data.rows) {
        // 有数据但标记为 fail，可能是认证问题，但仍然显示数据
        console.warn('API返回fail但包含数据，可能是认证问题:', response.data.message)
        const data = response.data.data
        list.value = data.rows || []
        pagination.total = data.total || 0
        // 如果消息是"请先登录"，说明token有问题
        if (response.data.message && response.data.message.includes('登录')) {
          ElMessage.error('登录已过期，请重新登录')
          // 延迟跳转到登录页
          setTimeout(() => {
            window.location.href = '/'
          }, 2000)
        }
      } else {
        // 没有数据，显示错误
        ElMessage.error(response.data.message || '查询失败')
      }
    } else {
      // 其他情况，尝试直接使用 data
      if (response.data.data) {
      const data = response.data.data
      list.value = data.rows || []
      pagination.total = data.total || 0
    } else {
      ElMessage.error(response.data.message || '查询失败')
      }
    }
  } catch (error: any) {
    console.error('获取API日志列表失败:', error)
    ElMessage.error(error.response?.data?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

// 重置搜索
const handleReset = () => {
  searchForm.userId = null
  searchForm.requestUrl = ''
  searchForm.requestMethod = ''
  searchForm.ipAddress = ''
  searchForm.status = null
  searchForm.startTime = ''
  searchForm.endTime = ''
  handleSearch()
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    const response = await http.get('/admin/apiLog/getById', {
      params: { id: row.id }
    })
    
    if (response.data.result === 'success') {
      currentLog.value = response.data.data
      showDetailDialog.value = true
    } else {
      ElMessage.error(response.data.message || '获取详情失败')
    }
  } catch (error: any) {
    console.error('获取API日志详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取详情失败')
  }
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pagination.limit = val
  pagination.page = 1
  fetchList()
}

// 页码改变
const handlePageChange = (val: number) => {
  pagination.page = val
  fetchList()
}

// 获取请求方法类型
const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || ''
}

// 获取状态码类型
const getStatusType = (status: number) => {
  if (status >= 200 && status < 300) {
    return 'success'
  } else if (status >= 300 && status < 400) {
    return 'info'
  } else if (status >= 400 && status < 500) {
    return 'warning'
  } else {
    return 'danger'
  }
}

// 初始化
onMounted(() => {
  // 检查 token 是否存在
  const token = getToken()
  if (!token) {
    ElMessage.error('登录已过期，请重新登录')
    setTimeout(() => {
      window.location.href = '/'
    }, 2000)
    return
  }
  
  // 确保 token 已加载后再请求数据
  fetchList()
})
</script>

<style scoped>
.api-log-list {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  align-items: center;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.search-item label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.search-input {
  width: 100%;
}

/* 统一输入框样式，去除双重边框 */
:deep(.search-input) {
  width: 100%;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dcdfe6 inset !important;
  border: none !important;
  outline: none !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fff;
  padding: 0 11px;
  min-height: 32px;
}

:deep(.search-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #72C1BB inset !important;
}

:deep(.search-input.is-focus .el-input__wrapper),
:deep(.search-input .el-input__wrapper.is-focus),
:deep(.search-input:focus-within .el-input__wrapper) {
  box-shadow: 0 0 0 1px #72C1BB inset !important;
  background-color: #fff;
  border: none !important;
  outline: none !important;
}

:deep(.search-input .el-input__inner) {
  font-size: 13px;
  color: #333;
  height: 32px;
  line-height: 32px;
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
  background: transparent !important;
}

:deep(.search-input .el-input__inner:focus) {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}

.search-select {
  width: 100%;
}

/* 统一选择框样式 */
:deep(.search-select) {
  width: 100%;
}

:deep(.search-select .el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dcdfe6 inset !important;
  border: none !important;
  outline: none !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fff;
  padding: 0 11px;
  min-height: 32px;
}

:deep(.search-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #72C1BB inset !important;
}

:deep(.search-select.is-focus .el-input__wrapper),
:deep(.search-select .el-input__wrapper.is-focus),
:deep(.search-select:focus-within .el-input__wrapper) {
  box-shadow: 0 0 0 1px #72C1BB inset !important;
  background-color: #fff;
  border: none !important;
  outline: none !important;
}

:deep(.search-select .el-input__inner) {
  font-size: 13px;
  color: #333;
  height: 32px;
  line-height: 32px;
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
  background: transparent !important;
}

:deep(.search-select .el-input__inner:focus) {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}

.search-item-time {
  grid-column: span 1;
}

.search-item-actions {
  grid-column: span 1;
  flex-direction: row;
  justify-content: flex-start;
  align-items: flex-end;
  gap: 10px;
}

.search-item-actions .el-button {
  flex: 1;
  max-width: 100px;
}

.search-date-picker {
  width: 100%;
}

/* 优化日期选择器样式 - 更美观的设计 */
:deep(.search-date-picker) {
  width: 100%;
}

:deep(.search-date-picker .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(to bottom, #ffffff, #fafafa);
  padding: 0 12px;
  min-height: 36px;
  border: none;
}

:deep(.search-date-picker .el-input__wrapper:hover) {
  box-shadow: 0 0 0 2px rgba(114, 193, 187, 0.3) inset;
  background: #fff;
  transform: translateY(-1px);
}

:deep(.search-date-picker.is-focus .el-input__wrapper),
:deep(.search-date-picker .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #72C1BB inset, 0 2px 8px rgba(114, 193, 187, 0.15);
  background: #fff;
  border: none;
}

:deep(.search-date-picker .el-input__inner) {
  font-size: 14px;
  color: #333;
  height: 36px;
  line-height: 36px;
  font-weight: 500;
}

:deep(.search-date-picker .el-input__prefix) {
  color: #72C1BB;
  display: flex;
  align-items: center;
  font-size: 16px;
}

:deep(.search-date-picker .el-input__suffix) {
  color: #72C1BB;
  display: flex;
  align-items: center;
}

:deep(.search-date-picker .el-input__suffix-inner) {
  cursor: pointer;
  transition: all 0.3s;
}

:deep(.search-date-picker .el-input__suffix-inner:hover) {
  color: #5aa9a3;
  transform: scale(1.1);
}

/* 优化日期选择器弹窗样式 */
:deep(.el-picker-panel) {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

:deep(.el-picker-panel__header) {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
  padding: 16px;
}

:deep(.el-picker-panel__header button) {
  color: white;
}

:deep(.el-picker-panel__header button:hover) {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

:deep(.el-picker-panel__body) {
  padding: 16px;
}

:deep(.el-date-table td.available:hover) {
  color: #72C1BB;
}

:deep(.el-date-table td.current:not(.disabled)) {
  color: #72C1BB;
  font-weight: 600;
}

:deep(.el-date-table td.today span) {
  color: #72C1BB;
  font-weight: 600;
}

:deep(.el-date-table td.selected span) {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  color: white;
}

:deep(.el-picker-panel__footer) {
  background: #f5f5f5;
  padding: 12px 16px;
  border-top: 1px solid #e0e0e0;
}

:deep(.el-picker-panel__footer button) {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s;
}

:deep(.el-picker-panel__footer .el-button--text) {
  color: #666;
}

:deep(.el-picker-panel__footer .el-button--text:hover) {
  color: #72C1BB;
  background: rgba(114, 193, 187, 0.1);
}

:deep(.el-picker-panel__footer .el-button--primary) {
  background: linear-gradient(135deg, #72C1BB 0%, #5aa9a3 100%);
  border: none;
  color: white;
}

:deep(.el-picker-panel__footer .el-button--primary:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(114, 193, 187, 0.3);
}

/* 响应式布局 */
@media (min-width: 1200px) {
  .search-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .search-item-time {
    grid-column: span 1;
  }
  
  .search-item-actions {
    grid-column: span 1;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .search-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .search-item-time {
    grid-column: span 1;
  }
  
  .search-item-actions {
    grid-column: span 1;
  }
}

@media (max-width: 767px) {
  .search-grid {
    grid-template-columns: 1fr;
  }
  
  .search-item-time {
    grid-column: span 1;
  }
  
  .search-item-actions {
    grid-column: span 1;
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-item-actions .el-button {
    max-width: 100%;
  }
}

.table-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 现代化分页样式 */
.modern-pagination {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.pagination-btn {
  padding: 8px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #72C1BB;
  color: #72C1BB;
  background: #f0f9f8;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(114, 193, 187, 0.2);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.modern-pagination-info {
  color: #666;
  font-size: 14px;
  margin: 0 4px;
  font-weight: 500;
}

.pagination-select {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #333;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 100px;
}

.pagination-select:hover {
  border-color: #72C1BB;
}

.pagination-select:focus {
  outline: none;
  border-color: #72C1BB;
  box-shadow: 0 0 0 3px rgba(114, 193, 187, 0.1);
}

.log-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.detail-section:last-child {
  margin-bottom: 0;
}
</style>
