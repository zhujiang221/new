/**
 * 药品相关API
 */
import http from './http';

export interface Medicine {
  id?: number;
  name: string;
  type?: string;
  description?: string;
  price?: number;
  stock?: number;
  status?: number; // 1-上架，0-下架
  createTime?: string;
  createBy?: number;
}

export interface MedicineListParams {
  name?: string;
  type?: string;
  status?: number;
  page: number;
  limit: number;
}

export interface MedicineListResponse {
  total: number;
  rows: Medicine[];
}

/**
 * 分页查询药品列表
 */
export async function getMedicineList(params: MedicineListParams): Promise<MedicineListResponse> {
  const resp = await http.get('/user/medicine/getAllByLimit', { params });
  return resp.data;
}

/**
 * 根据ID获取药品详情
 */
export async function getMedicineById(id: number): Promise<Medicine> {
  const resp = await http.get('/user/medicine/getById', { params: { id } });
  return resp.data;
}

/**
 * 添加药品
 */
export async function addMedicine(medicine: Medicine): Promise<string> {
  const resp = await http.post('/user/medicine/doAdd', medicine);
  return resp.data;
}

/**
 * 更新药品
 */
export async function updateMedicine(medicine: Medicine): Promise<string> {
  const resp = await http.post('/user/medicine/update', medicine);
  return resp.data;
}

/**
 * 删除药品
 */
export async function deleteMedicine(id: number): Promise<string> {
  const resp = await http.post('/user/medicine/del', { id });
  return resp.data;
}

