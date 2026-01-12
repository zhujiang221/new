package com.phms.pojo;

import java.util.Date;

/**
 * 医生可提供的服务类型
 */
public class DoctorServiceType extends BaseBean {

    private Long id;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 预约类型ID
     */
    private Long typeId;

    private Date createTime;

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

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

