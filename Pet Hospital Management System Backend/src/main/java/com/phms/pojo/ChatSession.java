package com.phms.pojo;

import java.util.Date;

/**
 * 聊天会话实体类
 */
public class ChatSession extends BaseBean {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 关联的申请ID
     */
    private Long requestId;

    /**
     * 状态（0=已关闭, 1=进行中）
     */
    private Integer status;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

    /**
     * 创建时间
     */
    private Date createTime;

    // 关联的用户信息（用于查询）
    private String userName;
    private String userImg;
    
    // 关联的医生信息（用于查询）
    private String doctorName;
    private String doctorImg;
    
    // 未读消息数
    private Integer unreadCount;
    
    // 最后一条消息内容
    private String lastMessageContent;
    private String lastMessageType;
    private Integer lastMessageIsRevoked; // 最后一条消息是否撤回 0=未撤回, 1=已撤回
    private Long lastMessageSenderId; // 最后一条消息的发送者ID

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorImg() {
        return doctorImg;
    }

    public void setDoctorImg(String doctorImg) {
        this.doctorImg = doctorImg;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(String lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    public Integer getLastMessageIsRevoked() {
        return lastMessageIsRevoked;
    }

    public void setLastMessageIsRevoked(Integer lastMessageIsRevoked) {
        this.lastMessageIsRevoked = lastMessageIsRevoked;
    }

    public Long getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(Long lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
