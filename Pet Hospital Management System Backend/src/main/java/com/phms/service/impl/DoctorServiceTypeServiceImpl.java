package com.phms.service.impl;

import com.phms.mapper.DoctorServiceTypeMapper;
import com.phms.pojo.DoctorServiceType;
import com.phms.service.DoctorServiceTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class DoctorServiceTypeServiceImpl implements DoctorServiceTypeService {
    @Resource
    DoctorServiceTypeMapper doctorServiceTypeMapper;

    @Override
    public List<DoctorServiceType> getByDoctorId(Long doctorId) {
        return doctorServiceTypeMapper.selectByDoctorId(doctorId);
    }

    @Override
    @Transactional
    public void setDoctorServiceTypes(Long doctorId, List<Long> typeIds) {
        // 先删除该医生的所有服务类型
        doctorServiceTypeMapper.deleteByDoctorId(doctorId);
        
        // 再添加新的服务类型
        if (typeIds != null && !typeIds.isEmpty()) {
            for (Long typeId : typeIds) {
                DoctorServiceType dst = new DoctorServiceType();
                dst.setDoctorId(doctorId);
                dst.setTypeId(typeId);
                dst.setCreateTime(new Date());
                doctorServiceTypeMapper.insertSelective(dst);
            }
        }
    }

    @Override
    public List<Long> getDoctorIdsByTypeId(Long typeId) {
        // 这个方法需要扩展Mapper，暂时返回空列表
        // 可以通过查询所有DoctorServiceType然后过滤实现
        return Collections.emptyList();
    }
}
