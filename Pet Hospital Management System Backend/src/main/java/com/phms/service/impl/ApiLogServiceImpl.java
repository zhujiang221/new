package com.phms.service.impl;

import com.phms.mapper.ApiLogMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.ApiLog;
import com.phms.service.ApiLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ApiLogServiceImpl implements ApiLogService {

    @Autowired
    private ApiLogMapper apiLogMapper;

    private final Logger logger = LoggerFactory.getLogger(ApiLogServiceImpl.class);

    @Override
    public void saveApiLog(ApiLog apiLog) {
        try {
            if (apiLog.getCreateTime() == null) {
                apiLog.setCreateTime(new Date());
            }
            apiLogMapper.insert(apiLog);
        } catch (Exception e) {
            logger.error("保存API日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public MMGridPageVoBean<ApiLog> getApiLogList(
            Long userId,
            String requestUrl,
            String requestMethod,
            String ipAddress,
            Integer status,
            Date startTime,
            Date endTime,
            Integer page,
            Integer limit
    ) {
        try {
            // 计算分页参数
            int begin = (page - 1) * limit;
            
            // 查询数据
            List<ApiLog> list = apiLogMapper.selectByCondition(
                    userId, requestUrl, requestMethod, ipAddress, status,
                    startTime, endTime, begin, limit
            );
            
            // 查询总数
            int count = apiLogMapper.countByCondition(
                    userId, requestUrl, requestMethod, ipAddress, status,
                    startTime, endTime
            );
            
            MMGridPageVoBean<ApiLog> vo = new MMGridPageVoBean<>();
            vo.setTotal(count);
            vo.setRows(list);
            return vo;
        } catch (Exception e) {
            logger.error("查询API日志列表失败: {}", e.getMessage(), e);
            MMGridPageVoBean<ApiLog> vo = new MMGridPageVoBean<>();
            vo.setTotal(0);
            vo.setRows(new ArrayList<>());
            return vo;
        }
    }

    @Override
    public ApiLog getApiLogById(Long id) {
        try {
            return apiLogMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("查询API日志详情失败: id={}, error={}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int deleteLogsBeforeDate(Date date) {
        try {
            return apiLogMapper.deleteByDateBefore(date);
        } catch (Exception e) {
            logger.error("删除API日志失败: date={}, error={}", date, e.getMessage(), e);
            return 0;
        }
    }
}
