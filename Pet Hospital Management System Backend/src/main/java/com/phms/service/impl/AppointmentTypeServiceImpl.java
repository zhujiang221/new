package com.phms.service.impl;

import com.phms.mapper.AppointmentTypeMapper;
import com.phms.pojo.AppointmentType;
import com.phms.service.AppointmentTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    @Resource
    private AppointmentTypeMapper appointmentTypeMapper;

    @Override
    public List<AppointmentType> listEnabledTypes() {
        List<AppointmentType> all = appointmentTypeMapper.selectAll();
        List<AppointmentType> enabled = new ArrayList<>();
        if (all != null) {
            for (AppointmentType t : all) {
                if (t.getStatus() == null || t.getStatus() == 1) {
                    enabled.add(t);
                }
            }
        }
        return enabled;
    }

    @Override
    public List<AppointmentType> listAll() {
        return appointmentTypeMapper.selectAll();
    }

    @Override
    public void add(AppointmentType type) {
        type.setCreateTime(new Date());
        appointmentTypeMapper.insertSelective(type);
    }

    @Override
    public void update(AppointmentType type) {
        appointmentTypeMapper.updateByPrimaryKeySelective(type);
    }

    @Override
    public void deleteById(Long id) {
        appointmentTypeMapper.deleteByPrimaryKey(id);
    }

    @Override
    public AppointmentType getById(Long id) {
        return appointmentTypeMapper.selectByPrimaryKey(id);
    }
}

