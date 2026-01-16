<template>
  <div class="prescribe-medicine">
    <div class="page-header">
      <h2>开具药品</h2>
    </div>

    <div class="form-container">
      <!-- 诊断信息 -->
      <div class="form-section">
        <h3>诊断信息</h3>
        <div class="form-group">
          <label>诊断ID：<span class="required">*</span></label>
          <input type="number" v-model="recordForm.diagnosisId" class="form-control" placeholder="请输入诊断ID" />
          <button class="btn btn-sm" @click="loadDiagnosisInfo">加载诊断信息</button>
        </div>
        <div v-if="diagnosisInfo" class="info-box">
          <p><strong>宠物名：</strong>{{ diagnosisInfo.name }}</p>
          <p><strong>用户名：</strong>{{ diagnosisInfo.userName }}</p>
          <p><strong>诊疗建议：</strong>{{ diagnosisInfo.info }}</p>
        </div>
      </div>

      <!-- 药品选择 -->
      <div class="form-section">
        <h3>选择药品</h3>
        <div class="form-group">
          <label>搜索药品：</label>
          <input type="text" v-model="medicineSearch" placeholder="输入药品名称搜索" @input="searchMedicine" />
        </div>
        <div class="medicine-list-box">
          <table class="data-table">
            <thead>
              <tr>
                <th width="50">选择</th>
                <th>药品名称</th>
                <th>类型</th>
                <th>价格</th>
                <th>库存</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="medicineLoading">
                <td colspan="6" class="loading">加载中...</td>
              </tr>
              <tr v-else-if="medicineList.length === 0">
                <td colspan="6" class="empty-state">暂无药品</td>
              </tr>
              <tr v-for="item in medicineList" :key="item.id">
                <td>
                  <input type="radio" :value="item.id" v-model="recordForm.medicineId" />
                </td>
                <td>{{ item.name }}</td>
                <td>{{ item.type || '-' }}</td>
                <td>¥{{ item.price?.toFixed(2) || '0.00' }}</td>
                <td>{{ item.stock || 0 }}</td>
                <td>{{ item.status === 1 ? '上架' : '下架' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 用药信息 -->
      <div class="form-section">
        <h3>用药信息</h3>
        <div class="form-row">
          <div class="form-group">
            <label>数量：<span class="required">*</span></label>
            <input type="number" v-model="recordForm.quantity" class="form-control" placeholder="请输入数量" min="1" />
          </div>
          <div class="form-group">
            <label>用量：</label>
            <input type="text" v-model="recordForm.dosage" class="form-control" placeholder="例如：每次1片" />
          </div>
        </div>
        <div class="form-group">
          <label>用法：</label>
          <textarea v-model="recordForm.usage" class="form-control" rows="3" placeholder="例如：每日三次，饭后服用"></textarea>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <button class="btn btn-primary" @click="submitPrescription">提交开药</button>
        <button class="btn" @click="goBack">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import http from '../../api/http';
import { getMedicineList, type Medicine } from '../../api/medicine';
import { addMedicineRecord, type MedicineRecord } from '../../api/medicineRecord';
import { showMessage } from '../../utils/message';

const route = useRoute();
const router = useRouter();

const recordForm = reactive<MedicineRecord>({
  diagnosisId: undefined,
  petId: undefined,
  userId: undefined,
  medicineId: undefined,
  quantity: undefined,
  dosage: '',
  usage: ''
});

const diagnosisInfo = ref<any>(null);
const medicineList = ref<Medicine[]>([]);
const medicineLoading = ref(false);
const medicineSearch = ref('');

// 从路由参数获取诊断ID
onMounted(() => {
  const diagnosisId = route.query.diagnosisId;
  if (diagnosisId) {
    recordForm.diagnosisId = Number(diagnosisId);
    loadDiagnosisInfo();
  }
  loadMedicines();
});

async function loadDiagnosisInfo() {
  if (!recordForm.diagnosisId) {
    showMessage('请输入诊断ID', 'error');
    return;
  }
  
  try {
    const resp = await http.get('/user/diagnosis/getAllByLimitDoctor', {
      params: { page: 1, limit: 100 }
    });
    const diagnosisList = resp.data.rows || [];
    const diagnosis = diagnosisList.find((d: any) => d.id == recordForm.diagnosisId);
    
    if (diagnosis) {
      diagnosisInfo.value = diagnosis;
      recordForm.petId = diagnosis.petId;
      recordForm.userId = diagnosis.userId;
    } else {
      showMessage('未找到该诊断记录', 'error');
    }
  } catch (e) {
    console.error('加载诊断信息失败:', e);
    showMessage('加载诊断信息失败', 'error');
  }
}

async function loadMedicines() {
  medicineLoading.value = true;
  try {
    const params: any = {
      page: 1,
      limit: 100,
      status: 1 // 只显示上架的药品
    };
    if (medicineSearch.value) {
      params.name = medicineSearch.value;
    }
    const data = await getMedicineList(params);
    medicineList.value = data.rows || [];
  } catch (e) {
    console.error('加载药品列表失败:', e);
    showMessage('加载药品列表失败', 'error');
  } finally {
    medicineLoading.value = false;
  }
}

function searchMedicine() {
  loadMedicines();
}

async function submitPrescription() {
  if (!recordForm.diagnosisId) {
    showMessage('请选择诊断记录', 'error');
    return;
  }
  if (!recordForm.medicineId) {
    showMessage('请选择药品', 'error');
    return;
  }
  if (!recordForm.quantity || recordForm.quantity <= 0) {
    showMessage('请输入有效的数量', 'error');
    return;
  }

  try {
    const result = await addMedicineRecord(recordForm);
    if (result === 'SUCCESS') {
      showMessage('开药成功', 'success');
      router.push('/doctor/medicine-record');
    } else if (result === 'LGINOUT') {
      window.location.href = '/';
    } else {
      showMessage(result || '开药失败', 'error');
    }
  } catch (e) {
    showMessage('操作失败', 'error');
  }
}

function goBack() {
  // 直接返回到宠物健康史页面
  router.push('/doctor/diagnosis');
}
</script>

<style scoped>
.prescribe-medicine {
  padding: 0;
}

.form-container {
  max-width: 1000px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 5px;
}

.form-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.info-box {
  margin-top: 10px;
  padding: 10px;
  background: white;
  border-radius: 3px;
}

.info-box p {
  margin: 5px 0;
}

.medicine-list-box {
  max-height: 400px;
  overflow-y: auto;
  margin-top: 10px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .btn {
  margin: 0 10px;
}

.required {
  color: red;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}
</style>

