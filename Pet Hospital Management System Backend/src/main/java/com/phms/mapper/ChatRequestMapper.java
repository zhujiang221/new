package com.phms.mapper;

import com.phms.pojo.ChatRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatRequestMapper {
    /**
     * 插入聊天申请
     */
    int insert(ChatRequest record);

    /**
     * 根据ID查询申请
     */
    ChatRequest selectByPrimaryKey(Long id);

    /**
     * 根据用户ID查询申请列表
     */
    List<ChatRequest> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 根据医生ID查询申请列表
     */
    List<ChatRequest> selectByDoctorId(@Param("doctorId") Long doctorId, @Param("status") Integer status);

    /**
     * 更新申请状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查是否已有待审核或已同意的申请
     */
    ChatRequest selectExistingRequest(@Param("userId") Long userId, @Param("doctorId") Long doctorId);

    /**
     * 查询所有申请（管理员）
     */
    List<ChatRequest> selectAll(ChatRequest chatRequest);

    /**
     * 统计申请数量
     */
    int countByCondition(ChatRequest chatRequest);
}
