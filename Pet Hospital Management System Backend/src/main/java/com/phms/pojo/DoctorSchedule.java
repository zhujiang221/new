package com.phms.pojo;

import java.util.Date;

/**
 * 医生每周时间段排班与最大预约数
 */
public class DoctorSchedule extends BaseBean {

    private Long id;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 星期几（1-7，1=周一）
     */
    private Integer weekDay;

    /**
     * 时间段，如 09:00-10:00
     */
    private String timeSlot;

    /**
     * 最大预约数量
     */
    private Integer maxAppointments;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(Integer weekDay) {
        this.weekDay = weekDay;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot == null ? null : timeSlot.trim();
    }

    public Integer getMaxAppointments() {
        return maxAppointments;
    }

    public void setMaxAppointments(Integer maxAppointments) {
        this.maxAppointments = maxAppointments;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

