package com.phms.mapper;

import com.phms.pojo.DoctorServiceType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DoctorServiceTypeMapper {

    int insert(DoctorServiceType record);

    int insertSelective(DoctorServiceType record);

    int deleteByPrimaryKey(Long id);

    int deleteByDoctorId(@Param("doctorId") Long doctorId);

    List<DoctorServiceType> selectByDoctorId(@Param("doctorId") Long doctorId);
}

