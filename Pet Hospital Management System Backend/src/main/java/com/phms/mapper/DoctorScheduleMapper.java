package com.phms.mapper;

import com.phms.pojo.DoctorSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DoctorScheduleMapper {

    int insert(DoctorSchedule record);

    int insertSelective(DoctorSchedule record);

    int updateByPrimaryKeySelective(DoctorSchedule record);

    int deleteByPrimaryKey(Long id);

    DoctorSchedule selectByPrimaryKey(Long id);

    List<DoctorSchedule> selectByDoctorId(@Param("doctorId") Long doctorId);

    List<DoctorSchedule> selectByDoctorAndWeek(@Param("doctorId") Long doctorId, @Param("weekDay") Integer weekDay);

    /**
     * 根据医生ID、星期几和时间段查询单个排班记录
     */
    DoctorSchedule selectByDoctorWeekAndTimeSlot(@Param("doctorId") Long doctorId, 
                                                  @Param("weekDay") Integer weekDay,
                                                  @Param("timeSlot") String timeSlot);

    List<DoctorSchedule> selectByDoctorIdWithLimit(@Param("doctorId") Long doctorId, 
                                                     @Param("page") Integer page, 
                                                     @Param("limit") Integer limit);

    int countByDoctorId(@Param("doctorId") Long doctorId);
}

