package com.phms.mapper;

import com.phms.pojo.ApiLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ApiLogMapper {
    /**
     * 插入API日志
     */
    int insert(ApiLog apiLog);

    /**
     * 根据ID查询
     */
    ApiLog selectByPrimaryKey(Long id);

    /**
     * 分页查询API日志（带多条件搜索）
     */
    List<ApiLog> selectByCondition(
            @Param("userId") Long userId,
            @Param("requestUrl") String requestUrl,
            @Param("requestMethod") String requestMethod,
            @Param("ipAddress") String ipAddress,
            @Param("status") Integer status,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("begin") Integer begin,
            @Param("count") Integer count
    );

    /**
     * 统计符合条件的日志数量
     */
    int countByCondition(
            @Param("userId") Long userId,
            @Param("requestUrl") String requestUrl,
            @Param("requestMethod") String requestMethod,
            @Param("ipAddress") String ipAddress,
            @Param("status") Integer status,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime
    );

    /**
     * 删除指定日期之前的日志
     */
    int deleteByDateBefore(@Param("date") Date date);
}
