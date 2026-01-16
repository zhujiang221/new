package com.phms.mapper;

import com.phms.pojo.ChatSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatSessionMapper {
    /**
     * 插入聊天会话
     */
    int insert(ChatSession record);

    /**
     * 根据ID查询会话
     */
    ChatSession selectByPrimaryKey(Long id);

    /**
     * 根据用户ID查询会话列表
     */
    List<ChatSession> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据医生ID查询会话列表
     */
    List<ChatSession> selectByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 根据用户ID和医生ID查询会话
     */
    ChatSession selectByUserAndDoctor(@Param("userId") Long userId, @Param("doctorId") Long doctorId);

    /**
     * 更新会话状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新最后消息时间
     */
    int updateLastMessageTime(@Param("id") Long id);

    /**
     * 查询所有会话（管理员）
     */
    List<ChatSession> selectAll(ChatSession chatSession);

    /**
     * 统计会话数量
     */
    int countByCondition(ChatSession chatSession);
}
