package com.phms.service;

import com.phms.pojo.AppointmentType;

import java.util.List;

public interface AppointmentTypeService {

    /**
     * 获取所有启用的预约类型
     */
    List<AppointmentType> listEnabledTypes();

    /**
     * 获取所有预约类型（包括禁用的）
     */
    List<AppointmentType> listAll();

    /**
     * 添加预约类型
     */
    void add(AppointmentType type);

    /**
     * 更新预约类型
     */
    void update(AppointmentType type);

    /**
     * 删除预约类型
     */
    void deleteById(Long id);

    /**
     * 根据ID获取预约类型
     */
    AppointmentType getById(Long id);
}

