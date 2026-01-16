package com.phms.service;

import com.phms.pojo.ChatRequest;

import java.util.List;

public interface ChatRequestService {
    /**
     * 创建聊天申请
     */
    void createRequest(ChatRequest chatRequest);

    /**
     * 根据ID查询申请
     */
    ChatRequest getById(Long id);

    /**
     * 根据用户ID查询申请列表
     */
    List<ChatRequest> getByUserId(Long userId, Integer status);

    /**
     * 根据医生ID查询申请列表
     */
    List<ChatRequest> getByDoctorId(Long doctorId, Integer status);

    /**
     * 医生同意申请
     */
    void approveRequest(Long id, Long doctorId);

    /**
     * 医生拒绝申请
     */
    void rejectRequest(Long id, Long doctorId);

    /**
     * 查询所有申请（管理员）
     */
    Object getAllRequests(ChatRequest chatRequest);
}
