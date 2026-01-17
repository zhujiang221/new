package com.phms.service;

import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.ApiLog;

import java.util.Date;

public interface ApiLogService {
    /**
     * 保存API日志
     */
    void saveApiLog(ApiLog apiLog);

    /**
     * 分页查询API日志（带多条件搜索）
     */
    MMGridPageVoBean<ApiLog> getApiLogList(
            Long userId,
            String requestUrl,
            String requestMethod,
            String ipAddress,
            Integer status,
            Date startTime,
            Date endTime,
            Integer page,
            Integer limit
    );

    /**
     * 根据ID查询日志详情
     */
    ApiLog getApiLogById(Long id);

    /**
     * 删除指定日期之前的日志
     */
    int deleteLogsBeforeDate(Date date);
}
