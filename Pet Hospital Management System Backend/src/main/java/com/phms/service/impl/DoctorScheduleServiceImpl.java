package com.phms.service.impl;

import com.phms.mapper.DoctorScheduleMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.DoctorSchedule;
import com.phms.service.DoctorScheduleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl implements DoctorScheduleService {
    @Resource
    DoctorScheduleMapper doctorScheduleMapper;

    @Override
    public List<DoctorSchedule> getByDoctorId(Long doctorId) {
        return doctorScheduleMapper.selectByDoctorId(doctorId);
    }

    @Override
    public Object getByDoctorIdWithLimit(Long doctorId, Integer page, Integer limit) {
        if (doctorId == null) {
            return new MMGridPageVoBean<DoctorSchedule>();
        }
        
        Integer begin = (page - 1) * limit;
        List<DoctorSchedule> rows = doctorScheduleMapper.selectByDoctorIdWithLimit(doctorId, begin, limit);
        Integer total = doctorScheduleMapper.countByDoctorId(doctorId);
        
        MMGridPageVoBean<DoctorSchedule> vo = new MMGridPageVoBean<>();
        vo.setRows(rows);
        vo.setTotal(total);
        return vo;
    }

    @Override
    public void add(DoctorSchedule schedule) {
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        if (schedule.getStatus() == null) {
            schedule.setStatus(1); // 默认启用
        }
        doctorScheduleMapper.insertSelective(schedule);
    }

    @Override
    public void update(DoctorSchedule schedule) {
        schedule.setUpdateTime(new Date());
        doctorScheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    @Override
    public void deleteById(Long id) {
        doctorScheduleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public DoctorSchedule getById(Long id) {
        return doctorScheduleMapper.selectByPrimaryKey(id);
    }

    @Override
    public void batchSave(List<DoctorSchedule> schedules) {
        for (DoctorSchedule schedule : schedules) {
            if (schedule.getId() != null) {
                // 更新
                update(schedule);
            } else {
                // 新增
                add(schedule);
            }
        }
    }

    @Override
    public void applyTemplate(Long doctorId, List<DoctorSchedule> schedules) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId 不能为空");
        }
        
        // 1. 先删除该医生的所有排班
        List<DoctorSchedule> existingSchedules = getByDoctorId(doctorId);
        if (existingSchedules != null) {
            for (DoctorSchedule schedule : existingSchedules) {
                if (schedule != null && schedule.getId() != null) {
                    deleteById(schedule.getId());
                }
            }
        }
        
        // 2. 批量添加新排班
        if (schedules != null && !schedules.isEmpty()) {
            for (DoctorSchedule schedule : schedules) {
                if (schedule != null) {
                    schedule.setDoctorId(doctorId);
                    add(schedule);
                }
            }
        }
    }
}
