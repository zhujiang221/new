package com.phms.service;

import com.phms.pojo.DoctorSchedule;

import java.util.List;

public interface DoctorScheduleService {
    /**
     * 获取医生的所有排班
     */
    List<DoctorSchedule> getByDoctorId(Long doctorId);

    /**
     * 分页获取医生的排班
     */
    Object getByDoctorIdWithLimit(Long doctorId, Integer page, Integer limit);

    /**
     * 添加排班
     */
    void add(DoctorSchedule schedule);

    /**
     * 更新排班
     */
    void update(DoctorSchedule schedule);

    /**
     * 删除排班
     */
    void deleteById(Long id);

    /**
     * 根据ID获取排班
     */
    DoctorSchedule getById(Long id);

    /**
     * 批量保存排班（用于一次性设置一周的排班）
     */
    void batchSave(List<DoctorSchedule> schedules);

    /**
     * 应用模板：先删除医生的所有排班，再批量添加新排班
     */
    void applyTemplate(Long doctorId, List<DoctorSchedule> schedules);
}
