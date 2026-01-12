package com.phms.mapper;

import com.phms.pojo.NotificationMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMessageMapper {
    /**
     * 插入消息记录
     */
    int insert(NotificationMessage record);

    /**
     * 根据接收者ID查询消息列表（分页）
     */
    List<NotificationMessage> selectByReceiverId(NotificationMessage notificationMessage);

    /**
     * 统计接收者的未读消息数量
     */
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 根据ID更新消息已读状态
     */
    int updateReadStatus(@Param("id") Long id, @Param("isRead") Integer isRead);

    /**
     * 批量标记消息为已读
     */
    int batchMarkAsRead(@Param("ids") List<Long> ids, @Param("receiverId") Long receiverId);

    /**
     * 统计接收者的消息总数（用于分页）
     */
    int countByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 根据ID查询消息
     */
    NotificationMessage selectByPrimaryKey(Long id);
}
