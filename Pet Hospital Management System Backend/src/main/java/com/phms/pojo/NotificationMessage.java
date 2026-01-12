package com.phms.pojo;

import java.util.Date;

/**
 * 预约消息提醒实体类
 */
public class NotificationMessage extends BaseBean {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 接收者ID（医生ID）
     */
    private Long receiverId;

    /**
     * 发送者ID（用户ID）
     */
    private Long senderId;

    /**
     * 预约ID（关联appointment表）
     */
    private Long appointmentId;

    /**
     * 消息类型（如"APPOINTMENT"）
     */
    private String type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容（JSON格式，包含用户姓名、时间段、预约类型等）
     */
    private String content;

    /**
     * 是否已读（0=未读，1=已读）
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
