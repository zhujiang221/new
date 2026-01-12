/**
 * 开药记录相关API
 */
import http from './http';

export interface MedicineRecord {
  id?: number;
  petId?: number;
  userId?: number;
  doctorId?: number;
  medicineId?: number;
  quantity?: number;
  dosage?: string;
  usage?: string;
  createTime?: string;
  diagnosisId?: number;
  // 关联查询字段
  petName?: string;
  userName?: string;
  doctorName?: string;
  medicineName?: string;
  medicinePrice?: number;
}

export interface MedicineRecordListParams {
  petId?: number;
  userId?: number;
  doctorId?: number;
  diagnosisId?: number;
  page: number;
  limit: number;
}

export interface MedicineRecordListResponse {
  total: number;
  rows: MedicineRecord[];
}

/**
 * 分页查询开药记录（普通用户）
 */
export async function getMedicineRecordList(params: MedicineRecordListParams): Promise<MedicineRecordListResponse> {
  const resp = await http.get('/user/medicineRecord/getAllByLimit', { params });
  return resp.data;
}

/**
 * 分页查询开药记录（医生）
 */
export async function getMedicineRecordListDoctor(params: MedicineRecordListParams): Promise<MedicineRecordListResponse> {
  const resp = await http.get('/user/medicineRecord/getAllByLimitDoctor', { params });
  return resp.data;
}

/**
 * 根据诊断ID查询开药记录
 */
export async function getMedicineRecordByDiagnosisId(diagnosisId: number): Promise<MedicineRecord[]> {
  const resp = await http.get('/user/medicineRecord/getByDiagnosisId', { params: { diagnosisId } });
  return resp.data;
}

/**
 * 开具药品
 */
export async function addMedicineRecord(record: MedicineRecord): Promise<string> {
  const resp = await http.post('/user/medicineRecord/doAdd', record);
  return resp.data;
}

/**
 * 删除开药记录
 */
export async function deleteMedicineRecord(id: number): Promise<string> {
  const resp = await http.post('/user/medicineRecord/del', { id });
  return resp.data;
}

