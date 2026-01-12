package com.phms.mapper;

import com.phms.pojo.AppointmentType;

import java.util.List;

public interface AppointmentTypeMapper {

    int insert(AppointmentType record);

    int insertSelective(AppointmentType record);

    int updateByPrimaryKeySelective(AppointmentType record);

    int deleteByPrimaryKey(Long id);

    AppointmentType selectByPrimaryKey(Long id);

    List<AppointmentType> selectAll();
}

