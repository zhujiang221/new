package com.phms.service;

import com.phms.pojo.DoctorServiceType;

import java.util.List;

public interface DoctorServiceTypeService {
    /**
     * 获取医生提供的服务类型列表
     */
    List<DoctorServiceType> getByDoctorId(Long doctorId);

    /**
     * 设置医生的服务类型（先删除旧的，再添加新的）
     */
    void setDoctorServiceTypes(Long doctorId, List<Long> typeIds);

    /**
     * 根据预约类型ID获取提供该服务的医生列表
     */
    List<Long> getDoctorIdsByTypeId(Long typeId);
}
